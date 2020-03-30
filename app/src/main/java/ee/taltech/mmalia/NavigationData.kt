package ee.taltech.mmalia

import android.location.Location
import android.util.Log
import ee.taltech.mmalia.Utils.NavigationData.Companion.distance
import ee.taltech.mmalia.Utils.NavigationData.Companion.duration
import ee.taltech.mmalia.Utils.NavigationData.Companion.speed

interface NavigationEventsListener {
    fun onStart()
    fun onNewLocation(location: Location)
}

interface UserEventsListener {
    fun onStartStop()
    fun onCPClick()
    fun onWPClick()
}

interface NavigationDataUpdatesListener {
    fun onSessionDistance(distance: String)
    fun onSessionDuration(duration: String)
    fun onSessionSpeed(speed: String)
    fun onCpDistance(distance: String)
    fun onCpDirectDistance(distance: String)
    fun onCpSpeed(speed: String)
    fun onWpDistance(distance: String)
    fun onWpDirectDistance(distance: String)
    fun onWpSpeed(speed: String)
}

class NavigationData(val updatesListener: NavigationDataUpdatesListener) : NavigationEventsListener,
    UserEventsListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    // last received location
    var currentLocation: Location? = null
    var startLocation: Location? = null
    var sessionStartTime = 0L

    var sessionDistance = 0f
    var sessionDuration = 0L
    var sessionSpeed = 0f

    var cpLocation: Location? = null
    var cpStartTime = 0L
    var cpDuration = 0L
    var cpDistance = 0f
    var cpDistanceDirect = 0f
    var cpSpeed = 0f

    var wpLocation: Location? = null
    var wpStartTime = 0L
    var wpDuration = 0L
    var wpDistance = 0f
    var wpDistanceDirect = 0f
    var wpSpeed = 0f

    override fun onStart() {
        Log.d(TAG, "Location service started")
    }

    fun isFirstLocation() = startLocation == null

    override fun onNewLocation(location: Location) {
        Log.d(TAG, "New location arrived")

        if (isFirstLocation()) {
            startLocation = location
            cpLocation = location
            wpLocation = location
            sessionStartTime = location.time
            cpStartTime = location.time
            wpStartTime = location.time
        } else {
            sessionDistance = distance(startLocation!!, location)
            sessionDuration = location.time - sessionStartTime
            sessionSpeed = speed(sessionDistance, sessionDuration)

            cpDistance += distance(currentLocation!!, location)
            cpDuration = location.time - cpStartTime
            cpDistanceDirect = distance(cpLocation!!, location)
            cpSpeed = speed(cpDistance, cpDuration)

            wpDistance += distance(currentLocation!!, location)
            wpDuration = location.time - wpStartTime
            wpDistanceDirect = distance(wpLocation!!, location)
            wpSpeed = speed(wpDistance, wpDuration)
        }

        notifyListener()
        currentLocation = location
    }

    private fun notifyListener() {
        updatesListener.onSessionDistance(distance(sessionDistance))
        updatesListener.onSessionDuration(duration(sessionDuration))
        updatesListener.onSessionSpeed(speed(sessionSpeed))

        updatesListener.onCpDistance(distance(cpDistance))
        updatesListener.onCpDirectDistance(distance(cpDistanceDirect))
        updatesListener.onCpSpeed(speed(cpSpeed))

        updatesListener.onWpDistance(distance(wpDistance))
        updatesListener.onWpDirectDistance(distance(wpDistanceDirect))
        updatesListener.onWpSpeed(speed(wpSpeed))
    }

    override fun onStartStop() {
        Log.d(TAG, "Start/Stop clicked")
    }

    override fun onCPClick() {
        Log.d(TAG, "CP clicked")

        cpStartTime = currentLocation!!.time
        cpLocation = currentLocation
        cpDistance = 0F
        cpDistanceDirect = 0F
        cpDuration = 0L
        cpSpeed = 0F
    }

    override fun onWPClick() {
        Log.d(TAG, "WP clicked")

        wpStartTime = currentLocation!!.time
        wpLocation = currentLocation
        wpDistance = 0F
        wpDistanceDirect = 0F
        wpDuration = 0L
        wpSpeed = 0F
    }
}