package ee.taltech.mmalia

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class NavigationNotification {

    companion object {

        fun create(context: Context, navigationData: NavigationDataUi): Notification {
            val intentStartStop =
                Intent(C.START_STOP_ACTION)
            val intentCp =
                Intent(C.CP_ACTION)
            val intentWp =
                Intent(C.WP_ACTION)

            val pendingIntentStartStop = PendingIntent.getBroadcast(
                context,
                0,
                intentStartStop,
                0
            )
            val pendingIntentCp =
                PendingIntent.getBroadcast(context, 0, intentCp, 0)
            val pendingIntentWp =
                PendingIntent.getBroadcast(context, 0, intentWp, 0)

            val notifyview = RemoteViews(
                context.packageName,
                R.layout.map_navigation
            )

            notifyview.setOnClickPendingIntent(R.id.start_stop_img_btn, pendingIntentStartStop)
            notifyview.setOnClickPendingIntent(R.id.cp_img_btn, pendingIntentCp)
            notifyview.setOnClickPendingIntent(R.id.wp_img_btn, pendingIntentWp)

            navigationData.run {
                notifyview.setTextViewText(R.id.session_distance_text_view, sessionDistance())
                notifyview.setTextViewText(R.id.session_duration_text_view, sessionDuration())
                notifyview.setTextViewText(R.id.session_speed_text_view, sessionSpeed())
                notifyview.setTextViewText(R.id.distance_cp_text_view, cpDistance())
                notifyview.setTextViewText(R.id.direct_distance_cp_text_view, cpDirectDistance())
                notifyview.setTextViewText(R.id.cp_speed_text_view, cpSpeed())
                notifyview.setTextViewText(R.id.distance_wp_text_view, wpDistance())
                notifyview.setTextViewText(R.id.direct_distance_wp_text_view, wpDirectDistance())
                notifyview.setTextViewText(R.id.wp_speed_text_view, wpSpeed())
            }

            // construct and show notification
            var builder = NotificationCompat.Builder(
                context,
                C.NOTIFICATION_CHANNEL
            )
                .setSmallIcon(R.drawable.ic_gps_fixed)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            builder.setContent(notifyview)

            return builder.build()
        }
    }
}