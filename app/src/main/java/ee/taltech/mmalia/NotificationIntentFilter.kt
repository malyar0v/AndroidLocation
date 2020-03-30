package ee.taltech.mmalia

import android.content.IntentFilter

class NotificationIntentFilter : IntentFilter() {

    init {
        addAction(C.NOTIFICATION_START_STOP_ACTION)
        addAction(C.NOTIFICATION_CP_ACTION)
        addAction(C.NOTIFICATION_WP_ACTION)
        addAction(C.LOCATION_UPDATE_ACTION)
    }

}