package ee.taltech.mmalia

import android.app.Notification
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocationActiveNotification {

    companion object {

        fun create(context: Context) : Notification {

            // construct and show notification
            val builder = NotificationCompat.Builder(context, C.NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_location_searching_black)
                .setContentText("${context.resources.getString(R.string.app_name)} is using your location.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


            // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
            // must be called within 5 secs after service starts.
            return builder.build()
        }
    }
}