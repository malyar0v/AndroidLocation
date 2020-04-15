package ee.taltech.mmalia.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import ee.taltech.mmalia.BoundsInclusiveMapMode
import ee.taltech.mmalia.ObjectBox
import ee.taltech.mmalia.R
import ee.taltech.mmalia.SessionMapTrackDrawer
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.Session_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor

class SessionMapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    lateinit var session: Session
    lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_map)

        val id = intent.getLongExtra(Session_.id.name, 0L)

        val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()
        session = sessionBox.get(id)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val start = session.locations.first()
        val end = session.locations.last()

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val bounds = LatLngBounds.Builder()
            .include(LatLng(start.latitude, start.longitude))
            .include(LatLng(end.latitude, end.longitude))
            .build()

        BoundsInclusiveMapMode(width, height)
            .bounds(bounds)
            .cameraUpdate()?.let {
                SessionMapTrackDrawer(mMap, session)
                    .draw(5F)
                    .zoom(it)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_session_map_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_export -> onExportSelected()
        }
        return true
    }

    private fun onExportSelected() {
        TODO("Not yet implemented")
    }
}
