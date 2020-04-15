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
import ee.taltech.mmalia.model.NavigationData
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.SimpleLocation
import ee.taltech.mmalia.service.notification.ConfirmationNotification
import ee.taltech.mmalia.service.notification.NavigationNotification
import io.objectbox.Box
import io.objectbox.kotlin.boxFor

interface LocationServiceEventsListener {

    fun onServiceStart()
    fun onNewLocation(location: Location)
    fun onServiceStop()
}

class LocationServiceManager(val locationService: LocationService) :
    LocationServiceEventsListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val context: Context = locationService.applicationContext

    val broadcastReceiver = UserEventsBroadcastReceiver()
    val intentFilter = IntentFilter().apply {
        addAction(C.START_STOP_ACTION)
        addAction(C.CP_ACTION)
        addAction(C.WP_ACTION)
        addAction(C.SESSION_STOP_CONFIRM_ACTION)
        addAction(C.SESSION_STOP_CANCEL_ACTION)
    }

    var showingNavigationNotification = true

    lateinit var session: Session
    lateinit var navigationData: NavigationData

    private val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()

    override fun onServiceStart() {
        navigationData = NavigationData()
        session = Session()

        locationService.startForeground(
            C.NOTIFICATION_NAVIGATION_ID,
            NavigationNotification.create(context, navigationData)
        )
    }

    override fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")

        navigationData.apply {

            if (isFirstLocation()) {
                startLocation = location
                cpLocation = location
                wpLocation = location
                sessionStartTime = location.time
                cpStartTime = location.time
                wpStartTime = location.time
            } else {
                sessionDistance = distance(startLocation!!, location)
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

        session.locations.add(SimpleLocation.from(location))

        sessionBox.put(session)

        Log.d(TAG, "Session:\n${session}")

        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.NAVIGATION_DATA_UPDATE_KEY, navigationData)
        intent.putExtra(C.SESSION_UPDATE_KEY, session)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onServiceStop() {
        // remove notifications
        NotificationManagerCompat.from(context).cancel(C.NOTIFICATION_NAVIGATION_ID)
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

                    navigationData.apply {
                        cpStartTime = currentLocation!!.time
                        cpLocation = currentLocation
                        cpDistance = 0F
                        cpDistanceDirect = 0F
                        cpDuration = 0L
                        cpSpeed = 0F
                    }

                    SimpleLocation.from(navigationData.cpLocation!!)
                        .let { location -> session.checkpoints.add(location) }

                    sessionBox.put(session)
                }
                C.WP_ACTION -> {
                    Log.d(TAG, "WP clicked")

                    navigationData.apply {
                        wpStartTime = currentLocation!!.time
                        wpLocation = currentLocation
                        wpDistance = 0F
                        wpDistanceDirect = 0F
                        wpDuration = 0L
                        wpSpeed = 0F
                    }

                    SimpleLocation.from(navigationData.wpLocation!!)
                        .let { location -> session.waypoints.add(location) }

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
            }
        }
    }
}