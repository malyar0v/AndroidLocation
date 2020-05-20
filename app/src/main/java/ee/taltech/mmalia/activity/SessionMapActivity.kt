package ee.taltech.mmalia.activity

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import ee.taltech.mmalia.*
import ee.taltech.mmalia.backend.BackendSync
import ee.taltech.mmalia.backend.NewSessionQuery
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.Session_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.io.File
import java.io.FileWriter
import java.util.*

class SessionMapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    lateinit var sessionBox: Box<Session>
    lateinit var session: Session
    lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_map)

        val id = intent.getLongExtra(Session_.id.name, 0L)
        sessionBox = ObjectBox.boxStore.boxFor()
        session = sessionBox.get(id)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawTrack()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_session_map_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_export -> onExportSelected()
            R.id.menu_item_edit -> onEditSelected()
            R.id.menu_item_session_sync -> onSessionSync()
        }
        return true
    }

    private fun onEditSelected() {
        SessionEditDialog(
            this,
            session,
            {
                sessionBox.put(session)
                drawTrack()
            },
            {
                sessionBox.remove(session)
                onBackPressed()
            })
            .create()
            .show()
    }

    private fun onExportSelected() {
        val gpx = GpxWriter(session).create()

        val name = "session-${session.id}.gpx"

        openFileOutput(name, Context.MODE_PRIVATE).use {
            it.write(gpx.toByteArray())
        }

        val path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/" + name
        val file = File(path)
        FileWriter(file).use { it.append(gpx) }

        Toast.makeText(this, "Saved to: $path", Toast.LENGTH_LONG).show()
/*        val uri = Uri.fromFile(file)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, uri)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(intent, "Share session .gpx file"))*/

        Log.d(TAG, "GPX")
    }

    private fun onSessionSync() {
        NewSessionQuery(
            session.title,
            session.description,
            Date(session.locations.firstOrNull()?.time ?: Date().time),
            { response ->
                session.backendId = response.id
                BackendSync(
                    BackendSync.State(
                        session.backendId,
                        LinkedList(session.locations),
                        LinkedList(session.checkpoints),
                        LinkedList(session.waypoints)
                    )
                )
                    .run()
            },
            {}
        )
            .execute()
    }

    private fun drawTrack() {
        mMap.clear()

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
}
