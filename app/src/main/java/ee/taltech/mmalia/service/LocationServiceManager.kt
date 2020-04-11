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
import ee.taltech.mmalia.service.notification.LocationActiveNotification
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
    }

    lateinit var session: Session
    lateinit var navigationData: NavigationData

    private val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()
    //private val simpleLocationBox: Box<SimpleLocation> = ObjectBox.boxStore.boxFor()

    override fun onServiceStart() {
        locationService.startForeground(
            C.NOTIFICATION_LOCATION_ACTIVE_ID,
            LocationActiveNotification.create(context)
        )
        broadcastReceiver.onServiceStart()
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


        session.locations.add(SimpleLocation.from(location))

        sessionBox.put(session)

        Log.d(TAG, "Session:\n${session}")

        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.NAVIGATION_DATA_UPDATE_KEY, navigationData)
        intent.putExtra(C.LOCATION_UPDATE_KEY, location)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onServiceStop() {
        // remove notifications
        broadcastReceiver.onServiceStop()
        NotificationManagerCompat.from(context).cancel(C.NOTIFICATION_LOCATION_ACTIVE_ID)
    }

    inner class UserEventsBroadcastReceiver : BroadcastReceiver() {

        fun onServiceStart() {
            Log.d(TAG, "Location service is started")
            session = Session()
            navigationData = NavigationData()
        }

        fun onServiceStop() {
            Log.d(TAG, "Location service is stopped")
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action) {
                C.START_STOP_ACTION -> {
                    Log.d(TAG, "Start/Stop clicked")

                    locationService.stopSelf()
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
            }
        }
    }
}