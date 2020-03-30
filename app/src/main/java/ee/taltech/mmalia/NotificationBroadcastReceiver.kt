package ee.taltech.mmalia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationBroadcastReceiver(val locationManager: LocationManager) : BroadcastReceiver() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, intent!!.action)
        when(intent!!.action){
/*            C.NOTIFICATION_WP_ACTION -> {
                navigation.locationWP = navigation.currentLocation
                navigation.distanceWPDirect = 0f
                navigation.distanceWP = 0f
//                navigation.showNotification()
                navigation.notification.update()
            }
            C.NOTIFICATION_CP_ACTION -> {
                navigation.locationCP = navigation.currentLocation
                navigation.distanceCPDirect = 0f
                navigation.distanceCP = 0f
//                navigation.showNotification()
                navigation.notification.update()
            }*/
        }
    }
}