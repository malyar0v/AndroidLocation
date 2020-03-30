package ee.taltech.mmalia

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NavigationNotification(val locationManager: LocationManager) : NavigationDataUpdatesListener {

    lateinit var locationService: LocationService
    lateinit var context: Context


    val broadcastReceiver = NotificationBroadcastReceiver(locationManager)
    val intentFilter = NotificationIntentFilter()

    fun update() {

        val intentStartStop = Intent(C.NOTIFICATION_START_STOP_ACTION)
        val intentCp = Intent(C.NOTIFICATION_CP_ACTION)
        val intentWp = Intent(C.NOTIFICATION_WP_ACTION)

        val pendingIntentStartStop = PendingIntent.getBroadcast(context, 0, intentStartStop, 0)
        val pendingIntentCp = PendingIntent.getBroadcast(context, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(context, 0, intentWp, 0)

        val notifyview = RemoteViews(context.packageName, R.layout.map_navigation)

        notifyview.setOnClickPendingIntent(R.id.start_stop_img_btn, pendingIntentStartStop)
        notifyview.setOnClickPendingIntent(R.id.cp_img_btn, pendingIntentCp)
        notifyview.setOnClickPendingIntent(R.id.wp_img_btn, pendingIntentWp)


        /*notifyview.setTextViewText(R.id.session_distance_text_view, "%.2f".format(locationManager.distanceSession))

        notifyview.setTextViewText(R.id.direct_distance_cp_to_current_text_view, "%.2f".format(locationManager.distanceCPDirect))
        notifyview.setTextViewText(R.id.distance_cp_to_current_text_view, "%.2f".format(locationManager.distanceCP))

        notifyview.setTextViewText(R.id.direct_distance_wp_to_current_text_view, "%.2f".format(locationManager.distanceWPDirect))
        notifyview.setTextViewText(R.id.distance_wp_to_current_text_view, "%.2f".format(locationManager.distanceWP))*/

        // construct and show notification
        var builder = NotificationCompat.Builder(context, C.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_gps_fixed)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyview)


        // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
        // must be called within 5 secs after service starts.
        locationService.startForeground(C.NOTIFICATION_ID, builder.build())
    }

    fun stop() {
        // remove notifications
        NotificationManagerCompat.from(context).cancelAll()

        // broadcast stop to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onSessionDistance(distance: String) {
        TODO("Not yet implemented")
    }

    override fun onSessionDuration(duration: String) {
        TODO("Not yet implemented")
    }

    override fun onSessionSpeed(speed: String) {
        TODO("Not yet implemented")
    }

    override fun onCpDistance(distance: String) {
        TODO("Not yet implemented")
    }

    override fun onCpDirectDistance(distance: String) {
        TODO("Not yet implemented")
    }

    override fun onCpSpeed(speed: String) {
        TODO("Not yet implemented")
    }

    override fun onWpDistance(distance: String) {
        TODO("Not yet implemented")
    }

    override fun onWpDirectDistance(distance: String) {
        TODO("Not yet implemented")
    }

    override fun onWpSpeed(speed: String) {
        TODO("Not yet implemented")
    }
}