package ee.taltech.mmalia.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import ee.taltech.mmalia.*
import ee.taltech.mmalia.Utils.Extensions.parse
import ee.taltech.mmalia.backend.BackendAuthenticator
import ee.taltech.mmalia.backend.LogInQuery
import ee.taltech.mmalia.backend.RegisterQuery
import ee.taltech.mmalia.model.ActivityState
import ee.taltech.mmalia.model.NavigationData
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.SpeedRange
import ee.taltech.mmalia.service.LocationService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.map_navigation.*
import kotlin.math.abs

class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private var activityState = ActivityState()

    private val menuHandler = OptionsMenuHandler()

    private var mMap: GoogleMap? = null
    private lateinit var mapTrackDrawer: IncrementalMapTrackDrawer
    private var mapMode: MapMode = NorthUpMapMode()
    private lateinit var speedRange: SpeedRange

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()
        .apply {
            addAction(C.LOCATION_UPDATE_ACTION)
            addAction(C.SESSION_STOP_CONFIRM_ACTION)
            addAction(C.LOCATION_SERVICE_START_ACTION)
        }

    // ============================================== LIFECYCLE =============================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!checkPermissions()) {
            requestPermissions()
        }

        start_stop_img_btn.setOnClickListener(this)
        cp_img_btn.setOnClickListener(this)
        wp_img_btn.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        restorePreferences(getPreferences(Context.MODE_PRIVATE))

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        if (LocationService.running) setStartStopButton(
            R.drawable.ic_stop
        )
        else setStartStopButton(R.drawable.ic_play_arrow_black)

        if (!Utils.Api.isLoggedIn(this)) {
            Toast.makeText(
                this,
                "Log in for syncing to work!",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        if (mMap != null) {
            Log.d(TAG, "Map is not null!")

            redrawTrack(activityState.session)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu) = menuHandler.onCreateOptionsMenu(menu)

    override fun onOptionsItemSelected(item: MenuItem) = menuHandler.onOptionsItemSelected(item)

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        restoreState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(C.IntentExtraKeys.ACTIVITY_STATE, activityState)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        Log.d(TAG, "onRestoreInstanceState")

        savedInstanceState?.getParcelable<ActivityState>(C.IntentExtraKeys.ACTIVITY_STATE)
            ?.let { state ->
                Log.d(TAG, "onRestoreInstanceState retrieved state!")

                compass.visibility =
                    if (state.compass) View.VISIBLE
                    else View.INVISIBLE

                mapMode = when (state.mapMode) {
                    MapMode.NORTH_UP -> NorthUpMapMode()
                    MapMode.DIRECTION_UP -> DirectionUpMapMode()
                    MapMode.NO_ZOOM -> NoZoomMapMode()

                    else -> NorthUpMapMode()
                }

                updateUi(state.navigationData)
                activityState = state
            }
    }

    private fun restorePreferences(sharedPref: SharedPreferences) {
        val min =
            sharedPref.getInt(C.SharedPreferences.OPTIMAL_SPEED_MIN_KEY, SpeedRange.DEFAULT_MIN)
        val max =
            sharedPref.getInt(C.SharedPreferences.OPTIMAL_SPEED_MAX_KEY, SpeedRange.DEFAULT_MAX)
        speedRange = SpeedRange(min..max)
    }

    private fun updateUi(navigationData: NavigationData) {
        navigationData.run {
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
    }

    private fun redrawTrack(session: Session) {
        mMap?.let { map ->
            map.clear()
            SessionMapTrackDrawer(map, session, markEdges = false).draw(10F)
        }
    }

    // ============================================== GOOGLE MAPS =============================================

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
        Log.d(TAG, "Map ready!")

        googleMap.isMyLocationEnabled = true

        // TODO:
//        redrawTrack(activityState.session)
        mapTrackDrawer = IncrementalMapTrackDrawer(googleMap)

        mMap = googleMap
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
                    .setAction("Settings") {
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
                    }
                    .show()
            }
        }
    }

    // ============================================== CLICK HANDLERS =============================================

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.start_stop_img_btn -> {
                    notifyLocationService(Intent(C.START_STOP_ACTION))
                    onStartStopClick()
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

    private fun notifyLocationService(intent: Intent) {
        Log.d(TAG, "Sending intent: ${intent.action}")

        sendBroadcast(intent)
    }

    fun onStartStopClick() {

        if (!LocationService.running) {

            SessionStartDialog(this, { title, description ->
                val i = Intent(this, LocationService::class.java)
                    .apply {
                        putExtra(C.IntentExtraKeys.SESSION_TITLE, title)
                        putExtra(C.IntentExtraKeys.SESSION_DESCRIPTION, description)
                    }

                if (Build.VERSION.SDK_INT >= 26) {
                    // starting the FOREGROUND service
                    // service has to display non-dismissable notification within 5 secs
                    startForegroundService(i)

                } else {
                    startService(i)
                }
            })
                .create()
                .show()
        } else {
            AlertDialog.Builder(this)
                .apply {
                    setMessage(getString(R.string.session_stop_confirmation))
                    setPositiveButton("Yes") { dialog, which ->
                        notifyLocationService(Intent(C.SESSION_STOP_CONFIRM_ACTION))
                    }
                    setNegativeButton("No") { dialog, which -> notifyLocationService(Intent(C.SESSION_STOP_CANCEL_ACTION)) }
                }
                .create()
                .show()
        }
    }

    fun setStartStopButton(resourceId: Int) = start_stop_img_btn.setImageResource(resourceId)

    // ============================================== OPTIONS MENU HANDLER =============================================
    private inner class OptionsMenuHandler {

        private val context = this@MainActivity
        lateinit var menu: Menu

        fun onCreateOptionsMenu(menu: Menu): Boolean {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(R.menu.activity_main_options_menu, menu)

            this.menu = menu
            updateMenu()

            return true
        }

        fun onOptionsItemSelected(item: MenuItem): Boolean {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            when (item.itemId) {
                R.id.menu_item_auto_zoom -> onAutoZoomSelected(item)
                R.id.menu_item_compass -> onCompassSelected()
                R.id.menu_item_history -> onHistorySelected()
                R.id.menu_item_north_up -> onNorthUpSelected()
                R.id.menu_item_direction_up -> onDirectionUpSelected()
                R.id.menu_item_optimal_speed -> onOptimalSpeedSelected()
                R.id.menu_item_login -> onLoginSelected()
                R.id.menu_item_logout -> onLogoutSelected()
                R.id.menu_item_register -> onRegisterSelected()
                R.id.menu_item_sync_1 -> onSync1Selected()
                R.id.menu_item_sync_2 -> onSync2Selected()
                R.id.menu_item_sync_3 -> onSync3Selected()
                R.id.menu_item_gps_updates_slow -> onGpsUpdatesSlowSelected()
                R.id.menu_item_gps_updates_medium -> onGpsUpdatesMediumSelected()
                R.id.menu_item_gps_updates_fast -> onGpsUpdatesFastSelected()
                R.id.menu_item_reset -> onResetSelected()
            }

            return true
        }

        fun updateMenu() {
            if (activityState.mapMode == MapMode.NO_ZOOM)
                menu.findItem(R.id.menu_item_auto_zoom).isChecked = false

            if (Utils.Api.isLoggedIn(context)) {
                menu.findItem(R.id.menu_item_login).isVisible = false
                menu.findItem(R.id.menu_item_register).isVisible = false
                menu.findItem(R.id.menu_item_sync).isVisible = true
                menu.findItem(R.id.menu_item_logout).isVisible = true
            } else {
                menu.findItem(R.id.menu_item_logout).isVisible = false
                menu.findItem(R.id.menu_item_login).isVisible = true
                menu.findItem(R.id.menu_item_register).isVisible = true
                menu.findItem(R.id.menu_item_sync).isVisible = false
            }
        }

        private fun onAutoZoomSelected(item: MenuItem) {
            if (item.isChecked) {
                mapMode = NoZoomMapMode()
                item.isChecked = false

                activityState.mapMode = MapMode.NO_ZOOM
            } else {
                mapMode = NorthUpMapMode()
                item.isChecked = true

                activityState.mapMode = MapMode.NORTH_UP
            }
        }

        fun onCompassSelected() {
            if (compass.visibility == View.VISIBLE) {
                compass.visibility = View.INVISIBLE
                activityState.compass = false
            } else {
                compass.visibility = View.VISIBLE
                activityState.compass = true
            }
        }

        fun onHistorySelected() {
            startActivity(Intent(context, SessionActivity::class.java))
        }

        fun onNorthUpSelected() {
            mapMode = NorthUpMapMode()
            activityState.mapMode = MapMode.NORTH_UP
        }

        fun onDirectionUpSelected() {
            mapMode = DirectionUpMapMode()
            activityState.mapMode = MapMode.DIRECTION_UP
        }

        fun onOptimalSpeedSelected() {
            val layout =
                layoutInflater.inflate(R.layout.optimal_speed_dialog, activity_main, false)

            val minEditText =
                layout.findViewById<EditText>(R.id.dialog_optimal_speed_min_edit_text)
                    .apply { setText(speedRange.min.toString()) }

            val maxEditText =
                layout.findViewById<EditText>(R.id.dialog_optimal_speed_max_edit_text)
                    .apply { setText(speedRange.max.toString()) }

            AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton("OK") { dialog, which ->

                    speedRange =
                        SpeedRange.parse(minEditText, maxEditText) ?: return@setPositiveButton

                    getPreferences(Context.MODE_PRIVATE)
                        .edit()
                        .putInt(C.SharedPreferences.OPTIMAL_SPEED_MIN_KEY, speedRange.min)
                        .putInt(C.SharedPreferences.OPTIMAL_SPEED_MAX_KEY, speedRange.max)
                        .apply()
                }
                .setNegativeButton("Cancel") { dialog, which -> }
                .create()
                .show()

            activityState.session.speedRange = speedRange

            redrawTrack(activityState.session)
        }

        fun authorize(token: String) {
            OkHttpClient.authenticate(BackendAuthenticator(context))
            Utils.Api.setToken(context, token)

            updateMenu()
        }

        fun onLoginSelected() {
            LoginDialog(context) { email, password ->
                LogInQuery(
                    email,
                    password,
                    { response ->
                        authorize(response.token)
                    },
                    { error ->
                    }
                )
                    .execute()
            }
                .create()
                .show()
        }

        fun onLogoutSelected() {
            Utils.Api.clearToken(context)
            updateMenu()
        }

        fun onRegisterSelected() {
            RegistrationDialog(context) { firstName, lastName, email, password ->
                RegisterQuery(
                    firstName,
                    lastName,
                    email,
                    password,
                    { response ->
                        authorize(response.token)
                    },
                    { error -> }
                )
                    .execute()
            }
                .create()
                .show()
        }

        fun onSync1Selected() {
            notifyLocationService(Intent(C.SYNC_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.SYNC_FREQUENCY,
                    0
                )
            })
        }

        fun onSync2Selected() {
            notifyLocationService(Intent(C.SYNC_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.SYNC_FREQUENCY,
                    1
                )
            })
        }

        fun onSync3Selected() {
            notifyLocationService(Intent(C.SYNC_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.SYNC_FREQUENCY,
                    2
                )
            })
        }

        fun onGpsUpdatesSlowSelected() {
            notifyLocationService(Intent(C.GPS_UPDATE_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.GPS_UPDATE_FREQUENCY,
                    LocationService.UPDATE_INTERVAL_SLOW
                )
            })
        }

        fun onGpsUpdatesMediumSelected() {
            notifyLocationService(Intent(C.GPS_UPDATE_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.GPS_UPDATE_FREQUENCY,
                    LocationService.UPDATE_INTERVAL_MEDIUM
                )
            })
        }

        fun onGpsUpdatesFastSelected() {
            notifyLocationService(Intent(C.GPS_UPDATE_FREQUENCY).apply {
                putExtra(
                    C.IntentExtraKeys.GPS_UPDATE_FREQUENCY,
                    LocationService.UPDATE_INTERVAL_FAST
                )
            })
        }

        fun onResetSelected() {
            mMap?.clear()
            onDirectionUpSelected()
        }


    }

    // ============================================== BROADCAST RECEIVER =============================================
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action)
            when (intent!!.action) {
                C.LOCATION_UPDATE_ACTION -> {

                    val navigationData =
                        intent.getParcelableExtra<NavigationData>(C.IntentExtraKeys.NAVIGATION_DATA_UPDATE)

                    val location = navigationData?.currentLocation
                    val bearing = location?.bearing

                    navigationData?.run {
                        this@MainActivity.updateUi(this)
                        activityState.navigationData = this
                    }


                    val session =
                        intent.getParcelableExtra<Session>(C.IntentExtraKeys.SESSION_UPDATE)

                    Log.d(TAG, "Range: ${speedRange}")

                    if (session != null && location != null) {
                        session.speedRange = speedRange

                        if (abs(activityState.session.locations.size - session.locations.size) > 1) {
                            redrawTrack(session)
                        } else {
                            mapTrackDrawer
                                .update(session)
                                .draw(10F)
                                .zoom(
                                    mapMode
                                        .bearing(bearing!!)
                                        .cameraUpdate(LatLng(location.latitude, location.longitude))
                                        ?: return
                                )
                        }

                        activityState.session = session
                    }
                }
                C.LOCATION_SERVICE_START_ACTION -> {

                    setStartStopButton(R.drawable.ic_stop)
                }
                C.SESSION_STOP_CONFIRM_ACTION -> {
                    setStartStopButton(R.drawable.ic_play_arrow_black)
                }
            }
        }
    }
}

