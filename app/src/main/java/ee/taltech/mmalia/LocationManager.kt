package ee.taltech.mmalia

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*

interface LocationReceiver {

    fun onStart(locationService: LocationService)
    fun onNewLocation(location: Location)
    fun onStop()
}

class LocationManager : LocationReceiver {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    lateinit var locationService: LocationService
    lateinit var context: Context

    val notification = NavigationNotification(this)
    //TODO: retrieve NavigationData the right way
    //val navigationData = NavigationData(notification)

    override fun onStart(locationService: LocationService) {
        this.locationService = locationService
        this.context = locationService.applicationContext
        this.notification.locationService = locationService
        this.notification.context = context

        val intent = Intent(C.LOCATION_SERVICE_START_ACTION)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

//        showNotification()
        notification.update()
    }

    override fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")

        //navigationData.onNewLocation(location)


//        navigationData.onNewLocation(location.latitude, location.longitude)
/*
        if (currentLocation == null){
            locationStart = location
            locationCP = location
            locationWP = location
        } else {
            sessionStartTime = Date(Date().time - sessionStartTime.time)


            distanceSession = location.distanceTo(locationStart)
//            distanceSession += location.distanceTo(currentLocation)

            distanceCPDirect = location.distanceTo(locationCP)
            distanceCP += location.distanceTo(currentLocation)

            distanceWPDirect = location.distanceTo(locationWP)
            distanceWP += location.distanceTo(currentLocation)
        }
        // save the location for calculations
        currentLocation = location*/

//        showNotification()
        notification.update()

        // broadcast new location to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LOCATION, location)
//        intent.putExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, location.latitude)
//        intent.putExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, location.longitude)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onStop() {
        notification.stop()
    }
}