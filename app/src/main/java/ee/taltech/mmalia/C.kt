package ee.taltech.mmalia

object C {

    const val NOTIFICATION_CHANNEL = "default_channel"
    const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    const val NOTIFICATION_NAVIGATION_ID = 180
    const val NOTIFICATION_CONFIRMATION_ID = 360

    const val LOCATION_SERVICE_START_ACTION = "ee.taltech.mmalia.location_service_start"
    const val LOCATION_SERVICE_STOP_ACTION = "ee.taltech.mmalia.location_service_stop"
    const val LOCATION_UPDATE_ACTION = "ee.taltech.mmalia.location_update"

    const val START_STOP_ACTION = "ee.taltech.mmalia.start_stop"
    const val CP_ACTION = "ee.taltech.mmalia.cp"
    const val WP_ACTION = "ee.taltech.mmalia.wp"

    const val SESSION_STOP_CONFIRM_ACTION = "ee.taltech.mmalia.session_stop_confirm"
    const val SESSION_STOP_CANCEL_ACTION = "ee.taltech.mmalia.cp.session_stop_cancel"



    object IntentExtraKeys {
        const val NAVIGATION_DATA_UPDATE = "New Navigation data is available"
        const val LOCATION_UPDATE = "New Location is available"
        const val SESSION_UPDATE = "Updated session is available"
        const val SESSION_TITLE = "Session title"
        const val SESSION_DESCRIPTION = "Session description"
        const val SESSION_BACKEND_ID = "Session backend id"
    }

    object SharedPreferences {
        const val OPTIMAL_SPEED_MIN_KEY = "min speed"
        const val OPTIMAL_SPEED_MAX_KEY = "max speed"

        const val API_PREFERENCES_KEY = "api"
        const val API_TOKEN_KEY = "token"
    }
}