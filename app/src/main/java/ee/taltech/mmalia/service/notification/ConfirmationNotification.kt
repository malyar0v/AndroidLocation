package ee.taltech.mmalia.service.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ee.taltech.mmalia.C
import ee.taltech.mmalia.R

object ConfirmationNotification {

    fun create(context: Context): Notification {

        val intentConfirm = Intent(C.SESSION_STOP_CONFIRM_ACTION)
        val intentCancel = Intent(C.SESSION_STOP_CANCEL_ACTION)

        val pendingIntentConfirm = PendingIntent.getBroadcast(
            context,
            0,
            intentConfirm,
            0
        )

        val pendingIntentCancel = PendingIntent.getBroadcast(
            context,
            0,
            intentCancel,
            0
        )

        // construct and show notification
        val builder = NotificationCompat.Builder(
            context,
            C.NOTIFICATION_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_gps_fixed)
            .setContentText(context.resources.getString(R.string.session_stop_confirmation))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "Yes", pendingIntentConfirm)
            .addAction(0, "No", pendingIntentCancel)

        return builder.build()
    }
}
