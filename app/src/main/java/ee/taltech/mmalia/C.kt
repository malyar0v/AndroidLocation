package ee.taltech.mmalia

class C {
    companion object {
        const val NOTIFICATION_CHANNEL = "default_channel"

        const val NOTIFICATION_START_STOP_ACTION = "ee.taltech.mmalia.start_stop"
        const val NOTIFICATION_WP_ACTION = "ee.taltech.mmalia.wp"
        const val NOTIFICATION_CP_ACTION = "ee.taltech.mmalia.cp"

        const val LOCATION_SERVICE_START_ACTION = "ee.taltech.mmalia.location_service_start"
        const val LOCATION_UPDATE_ACTION = "ee.taltech.mmalia.location_update"

/*        const val LOCATION_UPDATE_ACTION_LATITUDE = "ee.taltech.mmalia.location_update.latitude"
        const val LOCATION_UPDATE_ACTION_LONGITUDE = "ee.taltech.mmalia.location_update.longitude"*/
        const val LOCATION_UPDATE_ACTION_LOCATION = "ee.taltech.mmalia.location_update.location"

        const val NOTIFICATION_ID = 180
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}