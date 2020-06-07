package ee.taltech.mmalia.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ee.taltech.mmalia.C
import ee.taltech.mmalia.ObjectBox
import ee.taltech.mmalia.Utils.NavigationData.Companion.distance
import ee.taltech.mmalia.Utils.NavigationData.Companion.speed
import ee.taltech.mmalia.backend.BackendSync
import ee.taltech.mmalia.backend.NewSessionQuery
import ee.taltech.mmalia.model.NavigationData
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.SimpleLocation
import ee.taltech.mmalia.service.notification.ConfirmationNotification
import ee.taltech.mmalia.service.notification.NavigationNotification
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.util.*

interface LocationServiceEventsListener {

    fun onServiceStart(intent: Intent?)
    fun onNewLocation(location: Location)
    fun onServiceStop()
}

class LocationServiceManager(val locationService: LocationService) :
    LocationServiceEventsListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        private val DEFAULT_SYNC_INTERVAL: Long = 5 * 1000
    }

    private val context: Context = locationService.applicationContext

    val broadcastReceiver = UserEventsBroadcastReceiver()
    val intentFilter = IntentFilter().apply {
        addAction(C.START_STOP_ACTION)
        addAction(C.CP_ACTION)
        addAction(C.WP_ACTION)
        addAction(C.SESSION_STOP_CONFIRM_ACTION)
        addAction(C.SESSION_STOP_CANCEL_ACTION)
        addAction(C.GPS_UPDATE_FREQUENCY)
        addAction(C.SYNC_FREQUENCY)
    }

    var showingNavigationNotification = true

    lateinit var session: Session
    lateinit var navigationData: NavigationData

    private val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()
    private var syncState: BackendSync.State? = null
    private var timer: Timer? = null

    override fun onServiceStart(intent: Intent?) {
        navigationData = NavigationData()
        session = Session().apply {
            intent?.let {
                title = it.getStringExtra(C.IntentExtraKeys.SESSION_TITLE) ?: Session.DEFAULT.title
                description = it.getStringExtra(C.IntentExtraKeys.SESSION_DESCRIPTION)
                    ?: Session.DEFAULT.description
            }

        }

        locationService.startForeground(
            C.NOTIFICATION_NAVIGATION_ID,
            NavigationNotification.create(context, navigationData)
        )

        NewSessionQuery(
            session.title,
            session.description,
            Date(),
            { response ->
                session.backendId = response.id
                setUpSync(session.backendId, DEFAULT_SYNC_INTERVAL)
            },
            {}
        )
            .execute()
    }

    private fun isValidLocation(location: Location): Boolean {
        val accuracy = location.accuracy < 18
        val distance = {
            val currentLocation = navigationData.currentLocation
            if (currentLocation != null) location.distanceTo(currentLocation) < 25
            else false
        }()

        return accuracy && distance
    }

    override fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")

        if (!isValidLocation(location)) return

        navigationData.apply {

            if (isFirstLocation()) {
                startLocation = location
                cpLocation = location
                wpLocation = location
                sessionStartTime = location.time
                cpStartTime = location.time
                wpStartTime = location.time
            } else {
                sessionDistance += distance(currentLocation!!, location)
                sessionDuration = location.time - sessionStartTime
                sessionSpeed = speed(sessionDistance, sessionDuration)

                cpDistance += distance(currentLocation!!, location)
                cpDuration = location.time - cpStartTime
                cpDistanceDirect = distance(cpLocation!!, location)
                cpSpeed = speed(cpDistance, cpDuration)

                wpDistance += distance(currentLocation!!, location)
                wpDuration = location.time - wpStartTime
                wpDistanceDirect = distance(wpLocation!!, location)
                wpSpeed = speed(wpDistance, wpDuration)
            }

            currentLocation = location
        }

        if (showingNavigationNotification)
            locationService.startForeground(
                C.NOTIFICATION_NAVIGATION_ID,
                NavigationNotification.create(
                    context,
                    navigationData
                )
            )

        val simpleLocation = SimpleLocation.from(location)

        session.locations.add(simpleLocation)
        session.distance = navigationData.sessionDistance

        sessionBox.put(session)

        syncState?.locations?.add(simpleLocation)

        Log.d(TAG, "Session:\n${session}")

        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.IntentExtraKeys.NAVIGATION_DATA_UPDATE, navigationData)
        intent.putExtra(C.IntentExtraKeys.SESSION_UPDATE, session)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onServiceStop() {
        // remove notifications
        NotificationManagerCompat.from(context).cancel(C.NOTIFICATION_NAVIGATION_ID)
    }

    fun setUpSync(sessionId: String, syncInterval: Long) {
        syncState = BackendSync.State(sessionId, LinkedList(), LinkedList(), LinkedList())

        updateSyncInterval(syncInterval)
    }

    fun updateSyncInterval(interval: Long) {
        timer?.cancel()

        syncState?.let { state ->
            timer = Timer()
            val task = BackendSync(state)
            timer?.scheduleAtFixedRate(task, interval, interval)
        }
    }

    inner class UserEventsBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action) {
                C.START_STOP_ACTION -> {
                    Log.d(TAG, "Start/Stop clicked")

                    locationService.startForeground(
                        C.NOTIFICATION_CONFIRMATION_ID,
                        ConfirmationNotification.create(this@LocationServiceManager.context)
                    )
                    showingNavigationNotification = false

//                    locationService.stopSelf()
                }
                C.CP_ACTION -> {
                    Log.d(TAG, "CP clicked")

                    val location = navigationData.currentLocation!!

                    navigationData.apply {
                        cpStartTime = location.time
                        cpLocation = location
                        cpDistance = 0F
                        cpDistanceDirect = 0F
                        cpDuration = 0L
                        cpSpeed = 0F
                    }

                    SimpleLocation.from(location)
                        .let {
                            session.checkpoints.add(it)
                            syncState?.checkpoints?.add(it)
                        }

                    sessionBox.put(session)
                }
                C.WP_ACTION -> {
                    Log.d(TAG, "WP clicked")

                    val location = navigationData.currentLocation!!

                    navigationData.apply {
                        wpStartTime = location.time
                        wpLocation = location
                        wpDistance = 0F
                        wpDistanceDirect = 0F
                        wpDuration = 0L
                        wpSpeed = 0F
                    }

                    SimpleLocation.from(location)
                        .let {
                            session.waypoints.add(it)
                            syncState?.waypoints?.add(it)
                        }

                    sessionBox.put(session)
                }
                C.SESSION_STOP_CONFIRM_ACTION -> {
                    Log.d(TAG, "Stop confirmed")

                    LocalBroadcastManager.getInstance(this@LocationServiceManager.context)
                        .sendBroadcast(Intent(C.SESSION_STOP_CONFIRM_ACTION))

                    locationService.stopSelf()
                }
                C.SESSION_STOP_CANCEL_ACTION -> {
                    Log.d(TAG, "Stop cancelled")
                    showingNavigationNotification = true
                }
                C.GPS_UPDATE_FREQUENCY -> {
                    val frequency = intent.getLongExtra(
                        C.IntentExtraKeys.GPS_UPDATE_FREQUENCY,
                        LocationService.UPDATE_INTERVAL_MEDIUM
                    )

                    Log.d(TAG, "GPS frequency changed to: $frequency")

                    locationService.updateFrequency(frequency)
                }
                C.SYNC_FREQUENCY -> {
                    val option = intent.getIntExtra(C.IntentExtraKeys.SYNC_FREQUENCY, 0)

                    val secs = {
                        when (option) {
                            0 -> 5
                            1 -> 30
                            else -> 60
                        }
                    }()

                    Log.d(TAG, "Sync frequency changed to: $secs sec")

                    updateSyncInterval((secs * 1000).toLong())
                }
            }
        }
    }
}