package ee.taltech.mmalia

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NotificationService : Service() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        var running = false
            private set
    }

    lateinit var notification: Notification

    val broadcastReceiver = NotificationBroadcastReceiver()
    val intentFilter = IntentFilter().apply {
        addAction(C.START_STOP_ACTION)
        addAction(C.CP_ACTION)
        addAction(C.WP_ACTION)
        addAction(C.LOCATION_UPDATE_ACTION)
    }

    override fun onCreate() {
        super.onCreate()

        notification = NavigationNotification.create(applicationContext, NavigationData())
        startForeground(C.NOTIFICATION_NAVIGATION_ID, notification)

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
        registerReceiver(broadcastReceiver, intentFilter)

        running = true
    }

    override fun onDestroy() {
        super.onDestroy()

        // remove notifications
        NotificationManagerCompat.from(this).cancel(C.NOTIFICATION_NAVIGATION_ID)

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        unregisterReceiver(broadcastReceiver)

        running = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    open inner class NotificationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                C.LOCATION_UPDATE_ACTION -> {

                    intent.getParcelableExtra<NavigationData>(C.NAVIGATION_DATA_UPDATE_KEY)
                        ?.let {
                            startForeground(
                                C.NOTIFICATION_NAVIGATION_ID,
                                NavigationNotification.create(applicationContext, it)
                            )
                        }
                }
                C.START_STOP_ACTION -> {
                    Log.d(TAG, "Start/Stop clicked")
                    stopSelf()
                }
                C.CP_ACTION -> {
                    Log.d(TAG, "CP clicked")
                }
                C.WP_ACTION -> {
                    Log.d(TAG, "WP clicked")
                }
            }
        }
    }
}



