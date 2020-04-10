package ee.taltech.mmalia

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        const val NOTIFICATION_LOCATION_ACTIVE_ID = 180
        const val NOTIFICATION_NAVIGATION_ID = 360

        const val LOCATION_SERVICE_START_ACTION = "ee.taltech.mmalia.location_service_start"
        const val LOCATION_SERVICE_STOP_ACTION = "ee.taltech.mmalia.location_service_stop"
        const val LOCATION_UPDATE_ACTION = "ee.taltech.mmalia.location_update"

        const val NOTIFICATION_SERVICE_START_ACTION = "ee.taltech.mmalia.notification_service_start"
        const val NOTIFICATION_SERVICE_STOP_ACTION = "ee.taltech.mmalia.notification_service_stop"

        const val START_STOP_ACTION = "ee.taltech.mmalia.start_stop"
        const val CP_ACTION = "ee.taltech.mmalia.cp"
        const val WP_ACTION = "ee.taltech.mmalia.wp"

        const val NAVIGATION_DATA_UPDATE_KEY = "New Navigation data is available"
        const val LOCATION_UPDATE_KEY = "New Location is available"
    }
}