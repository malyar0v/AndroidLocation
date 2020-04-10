package ee.taltech.mmalia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ee.taltech.mmalia.Utils.NavigationData.Companion.distance
import ee.taltech.mmalia.Utils.NavigationData.Companion.speed

interface LocationServiceEventsListener {

    fun onStart()
    fun onNewLocation(location: Location)
    fun onStop()
}

class LocationServiceManager(val locationService: LocationService) : LocationServiceEventsListener {

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

    lateinit var navigationData: NavigationData

    override fun onStart() {
        locationService.startForeground(C.NOTIFICATION_LOCATION_ACTIVE_ID, LocationActiveNotification.create(context))
        broadcastReceiver.onStart()
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

        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.NAVIGATION_DATA_UPDATE_KEY, navigationData)
        intent.putExtra(C.LOCATION_UPDATE_KEY, location)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onStop() {
        // remove notifications
        broadcastReceiver.onStop()
        NotificationManagerCompat.from(context).cancel(C.NOTIFICATION_LOCATION_ACTIVE_ID)
    }

    inner class UserEventsBroadcastReceiver : BroadcastReceiver() {

        fun onStart() {
            Log.d(TAG, "Location service is started")
            navigationData = NavigationData()
        }

        fun onStop() {
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
                }
            }
        }
    }
}