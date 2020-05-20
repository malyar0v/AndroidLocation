package ee.taltech.mmalia.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import ee.taltech.mmalia.C

class LocationService : Service() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        // The desired intervals for location updates. Inexact. Updates may be more or less frequent.
        public val UPDATE_INTERVAL_SLOW: Long = 3000
        public val UPDATE_INTERVAL_MEDIUM: Long = 1500
        public val UPDATE_INTERVAL_FAST: Long = 500

        var running = false
            private set
    }

    private lateinit var locationServiceManager: LocationServiceManager

    private var mLocationRequest: LocationRequest? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        locationServiceManager =
            LocationServiceManager(this)

        registerReceiver(
            locationServiceManager.broadcastReceiver,
            locationServiceManager.intentFilter
        )

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = createLocationCallback()

        getLastLocation()

        mLocationRequest = createLocationRequest(UPDATE_INTERVAL_MEDIUM)
        requestLocationUpdates()

        running = true
    }

    fun updateFrequency(frequency: Long) {
        createLocationRequest(frequency)

        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        requestLocationUpdates()
    }

    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")

        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(
                TAG,
                "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    private fun createLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationServiceManager.onNewLocation(locationResult.lastLocation)
            }
        }
    }

    private fun createLocationRequest(frequency: Long): LocationRequest {
        val locationRequest = LocationRequest()

        locationRequest.setInterval(frequency)
        locationRequest.setFastestInterval(UPDATE_INTERVAL_FAST)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setMaxWaitTime(frequency)

        return locationRequest
    }


    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.w(TAG, "task successfull");
                        if (task.result != null) {
                            locationServiceManager.onNewLocation(task.result!!)
                        }
                    } else {

                        Log.w(TAG, "Failed to get location." + task.exception)
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        locationServiceManager.onServiceStop()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        // don't forget to unregister brodcast receiver!!!!
        unregisterReceiver(locationServiceManager.broadcastReceiver)

        running = false
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(C.LOCATION_SERVICE_START_ACTION))

        locationServiceManager.onServiceStart(intent)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        TODO("not implemented")
    }

}