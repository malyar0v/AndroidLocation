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

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var mMap: GoogleMap

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()
        .apply {
            addAction(C.LOCATION_UPDATE_ACTION)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // safe to call every time
        // TODO: Application class
        createNotificationChannel()

        if (!checkPermissions()) {
            requestPermissions()
        }

        start_stop_img_btn.setOnClickListener(this)
        cp_img_btn.setOnClickListener(this)
        wp_img_btn.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        if (LocationService.running && NotificationService.running) setStartStopButton(R.drawable.ic_stop)
        else setStartStopButton(R.drawable.ic_play_arrow_black)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    fun setStartStopButton(resourceId: Int) = start_stop_img_btn.setImageResource(resourceId)

    fun notifyLocationService(intent: Intent) {
        Log.d(TAG, "Sending intent: ${intent.action}")

        sendBroadcast(intent)
    }

    override fun onClick(v: View?) {

        v?.let {

            when (it.id) {
                R.id.start_stop_img_btn -> {
                    notifyLocationService(Intent(C.START_STOP_ACTION))
                    buttonStartStopOnClick()
                }
                R.id.cp_img_btn -> {
                    notifyLocationService(Intent(C.CP_ACTION))
                }
                R.id.wp_img_btn -> {
                    notifyLocationService(Intent(C.WP_ACTION))
                }
            }

        }
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
    fun buttonStartStopOnClick() {

        if (LocationService.running && NotificationService.running) {

            stopService(Intent(this, NotificationService::class.java))
            stopService(Intent(this, LocationService::class.java))

            setStartStopButton(R.drawable.ic_play_arrow_black)
        } else if (!LocationService.running && !NotificationService.running) {
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissable notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
                startForegroundService(Intent(this, NotificationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
                startService(Intent(this, NotificationService::class.java))
            }
            setStartStopButton(R.drawable.ic_stop)
        } else {
            Log.d(TAG, "One of the services is unintentionally running!")
        }
    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action) {
                C.LOCATION_UPDATE_ACTION -> {
                    val navigationData =
                        intent.getParcelableExtra<NavigationData>(C.NAVIGATION_DATA_UPDATE_KEY)

                    navigationData?.run {
                        session_distance_text_view.text = sessionDistance()
                        session_duration_text_view.text = sessionDuration()
                        session_speed_text_view.text = sessionSpeed()
                        distance_cp_text_view.text = cpDistance()
                        direct_distance_cp_text_view.text = cpDirectDistance()
                        cp_speed_text_view.text = cpSpeed()
                        distance_wp_text_view.text = wpDistance()
                        direct_distance_wp_text_view.text = wpDirectDistance()
                        wp_speed_text_view.text = wpSpeed()
                    }

                    val location = intent.getParcelableExtra<Location>(C.LOCATION_UPDATE_KEY)
                    Log.d(TAG, "New location: ${location}")
                }
            }
        }
    }
}