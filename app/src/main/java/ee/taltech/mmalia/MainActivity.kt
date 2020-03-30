package ee.taltech.mmalia

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.map_navigation.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, NavigationDataUpdatesListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    val navigationData = NavigationData(this)
    val navigationEventsListener: NavigationEventsListener = navigationData
    val userEventsListener: UserEventsListener = navigationData

    private lateinit var mMap: GoogleMap

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private var locationServiceActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // safe to call every time
        createNotificationChannel()

        if (!checkPermissions()) {
            requestPermissions()
        }

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_SERVICE_START_ACTION)

        start_stop_img_btn.setOnClickListener() {
            buttonStartStopOnClick(it)
        }

        cp_img_btn.setOnClickListener(this)
        wp_img_btn.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.start_stop_img_btn -> userEventsListener.onStartStop()
                R.id.cp_img_btn -> userEventsListener.onCPClick()
                R.id.wp_img_btn -> userEventsListener.onWPClick()
            }
        }
    }


    override fun onSessionDistance(distance: String) {
        session_distance_text_view.text = "$distance"
    }

    override fun onSessionDuration(duration: String) {
        session_duration_text_view.text = duration
    }

    override fun onSessionSpeed(speed: String) {
        session_speed_text_view.text = speed
    }

    override fun onCpDistance(distance: String) {
        distance_cp_text_view.text = distance
    }

    override fun onCpDirectDistance(distance: String) {
        direct_distance_cp_text_view.text = distance
    }

    override fun onCpSpeed(speed: String) {
        cp_speed_text_view.text = speed
    }

    override fun onWpDistance(distance: String) {
        distance_wp_text_view.text = distance
    }

    override fun onWpDirectDistance(distance: String) {
        direct_distance_wp_text_view.text = distance
    }

    override fun onWpSpeed(speed: String) {
        wp_speed_text_view.text = speed
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    // ============================================== NOTIFICATION CHANNEL CREATION =============================================
    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                C.NOTIFICATION_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_LOW
            );

            //.setShowBadge(false).setSound(null, null);

            channel.description = "Default channel"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    // ============================================== PERMISSION HANDLING =============================================
    // Returns the current state of the permissions needed.
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(
                TAG,
                "Displaying permission rationale to provide additional context."
            )
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    "Hey, i really need to access GPS!",
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction("OK", View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        C.REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                C.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode === C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.count() <= 0) { // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
                Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {// Permission was granted.
                Log.i(TAG, "Permission was granted")
                Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
            } else { // Permission denied.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        "You denied GPS! What can I do?",
                        Snackbar.LENGTH_INDEFINITE
                    )
                    .setAction("Settings", View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri: Uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
                    .show()
            }
        }
    }

    // ============================================== CLICK HANDLERS =============================================
    fun buttonStartStopOnClick(view: View) {
        Log.d(TAG, "buttonStartStopOnClick. locationServiceActive: $locationServiceActive")
        // try to start/stop the background service
        userEventsListener.onStartStop()

        if (locationServiceActive) {
            // stopping the service
            stopService(Intent(this, LocationService::class.java))

            start_stop_img_btn.setImageResource(R.drawable.ic_play_arrow_black)
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissable notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            start_stop_img_btn.setImageResource(R.drawable.ic_stop)
        }

        locationServiceActive = !locationServiceActive
    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action) {
                C.LOCATION_UPDATE_ACTION -> {
                    val location = intent.getParcelableExtra<Location>(C.LOCATION_UPDATE_ACTION_LOCATION)
                    location?.let {
                        navigationEventsListener.onNewLocation(it)
                    }
/*                    val latitude =
                        intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, 0.0)
                    val longitude =
                        intent.getDoubleExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, 0.0)

                    navigationEventsListener.onNewLocation(latitude, longitude)
                    */
                }
                C.LOCATION_SERVICE_START_ACTION -> navigationEventsListener.onStart()
            }
        }
    }
}