package ee.taltech.mmalia

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import ee.taltech.mmalia.Utils.NavigationData.Companion.speed
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.SimpleLocation
import ee.taltech.mmalia.model.SpeedRange

abstract class MapTrackDrawer(protected val map: GoogleMap) {

    companion object {
        internal val TAG = this::class.java.declaringClass!!.simpleName
    }

    abstract fun draw(
        width: Float
    ): MapTrackDrawer

    abstract fun zoom(update: CameraUpdate): MapTrackDrawer

    protected fun marker(
        location: SimpleLocation,
        title: String,
        alpha: Float = 1F
    ): MarkerOptions {
        return MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title(title)
            .alpha(alpha)
    }

    protected fun distanceBetween(start: SimpleLocation, finish: SimpleLocation): Float {
        val arr = floatArrayOf(0.0F)

        Location.distanceBetween(
            start.latitude,
            start.longitude,
            finish.latitude,
            finish.longitude,
            arr
        )

        return arr[0]
    }
}

class IncrementalMapTrackDrawer(map: GoogleMap) : MapTrackDrawer(map) {

    lateinit var session: Session

    fun update(session: Session): IncrementalMapTrackDrawer {
        this.session = session

        return this
    }

    override fun draw(width: Float): IncrementalMapTrackDrawer {

        if (session.locations.size < 2) return this

        val new: SimpleLocation = session.locations.last()
        val previous: SimpleLocation = session.locations[session.locations.lastIndex - 1]

        if (previous in session.waypoints) map.addMarker(marker(previous, "WP", 0.5F))
        else if (previous in session.checkpoints) map.addMarker(marker(previous, "CP", 0.5F))

        val distance = distanceBetween(previous, new)
        val speed = speed(distance, new.time - previous.time)
        Log.d(TAG, "Speed $speed")
        val color = session.speedRange.color(speed)

        map.addPolyline(
            PolylineOptions().apply {
                add(
                    LatLng(previous.latitude, previous.longitude),
                    LatLng(new.latitude, new.longitude)
                )
                width(width)
                color(color)
            }
        )

        return this
    }

    override fun zoom(update: CameraUpdate): IncrementalMapTrackDrawer {

        if (session.locations.size < 2) return this

        map.moveCamera(update)

        return this
    }
}

class SessionMapTrackDrawer(
    map: GoogleMap,
    private val session: Session,
    val markEdges: Boolean = true
) :
    MapTrackDrawer(map) {

    override fun draw(width: Float): SessionMapTrackDrawer {

        if (session.locations.size < 2) return this

        var color = SpeedRange.COLOR_SLOW
        var previousColor: Int

        var polyline = PolylineOptions().apply {
            width(width)
            color(color)
        }

        for ((idx, location) in session.locations.withIndex()) {

            if (isWp(location)) map.addMarker(marker(location, "WP", 0.5F))
            else if (isCp(location)) map.addMarker(marker(location, "CP", 0.5F))

            val next = if (idx + 1 < session.locations.size) session.locations[idx + 1] else break

            val distance = distanceBetween(location, next)
            val speed = speed(distance, next.time - location.time)

            previousColor = color
            color = session.speedRange.color(speed)

            if (color == previousColor) {
                polyline.apply {
                    add(
                        LatLng(location.latitude, location.longitude),
                        LatLng(next.latitude, next.longitude)
                    )
                }
            } else {
                map.addPolyline(polyline)

                polyline = PolylineOptions().apply {
                    width(width)
                    add(
                        LatLng(location.latitude, location.longitude),
                        LatLng(next.latitude, next.longitude)
                    )
                    color(color)
                }
            }
        }

        map.addPolyline(polyline)

        if (markEdges) {
            map.addMarker(marker(session.locations.first(), "Start"))
            map.addMarker(marker(session.locations.last(), "Finish"))
        }

        return this
    }

    override fun zoom(update: CameraUpdate): SessionMapTrackDrawer {

        if (session.locations.size < 2) return this

        val start = session.locations.first()
        val end = session.locations.last()

        val bounds = LatLngBounds.Builder()
            .include(LatLng(start.latitude, start.longitude))
            .include(LatLng(end.latitude, end.longitude))
            .build()


        map.moveCamera(update)

        return this
    }

    private fun isCp(location: SimpleLocation) = location in session.checkpoints
    private fun isWp(location: SimpleLocation) = location in session.waypoints
}

abstract class MapMode {

    companion object {
        const val NORTH_UP = 0
        const val DIRECTION_UP = 1
        const val NO_ZOOM = 2
    }

    var zoomLevel: Float = 19F
    var bearing: Float = 0F

    fun bearing(bearing: Float): MapMode {
        this.bearing = bearing

        return this
    }

    abstract fun cameraUpdate(target: LatLng): CameraUpdate?
    open fun cameraUpdate(): CameraUpdate? = null
}

class NoZoomMapMode : MapMode() {

    override fun cameraUpdate(target: LatLng): CameraUpdate? {
        return null
    }
}

class NorthUpMapMode : MapMode() {

    override fun cameraUpdate(target: LatLng): CameraUpdate {
        return CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(target)
                .zoom(zoomLevel)
                .bearing(0F)
                .build()
        )
    }
}

class DirectionUpMapMode : MapMode() {

    override fun cameraUpdate(target: LatLng): CameraUpdate {
        return CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(target)
                .zoom(zoomLevel)
                .bearing(bearing)
                .build()
        )
    }
}

class BoundsInclusiveMapMode(
    val width: Int,
    val height: Int,
    val offset: Float = 0.05F
) : MapMode() {

    private var bounds: LatLngBounds? = null

    fun bounds(bounds: LatLngBounds): MapMode {
        this.bounds = bounds

        return this
    }

    override fun cameraUpdate(target: LatLng): CameraUpdate {
        return cameraUpdate()
    }

    override fun cameraUpdate(): CameraUpdate {

        val padding = (width * offset).toInt() // offset from edges of the map 10% of screen

        return CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
    }

}
