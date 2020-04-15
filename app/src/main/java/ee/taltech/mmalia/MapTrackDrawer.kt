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

abstract class MapTrackDrawer(protected val map: GoogleMap) {

    companion object {
        internal val TAG = this::class.java.declaringClass!!.simpleName
    }

    abstract fun draw(
        width: Float
    ): MapTrackDrawer

    abstract fun zoom(update: CameraUpdate): MapTrackDrawer

    protected fun marker(location: SimpleLocation, title: String): MarkerOptions {
        return MarkerOptions()
            .position(LatLng(location.latitude, location.longitude))
            .title(title)
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

        if (previous in session.waypoints) map.addMarker(marker(previous, "WP"))
        else if (previous in session.checkpoints) map.addMarker(marker(previous, "CP"))

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

class SessionMapTrackDrawer(map: GoogleMap, private val session: Session) :
    MapTrackDrawer(map) {

    override fun draw(width: Float): SessionMapTrackDrawer {

        val polyline = PolylineOptions()

        for ((idx, location) in session.locations.withIndex()) {

            if (isWp(location)) map.addMarker(marker(location, "WP"))
            else if (isCp(location)) map.addMarker(marker(location, "CP"))

            val next = if (idx + 1 < session.locations.size) session.locations[idx + 1] else break

            val distance = distanceBetween(location, next)
            val speed = speed(distance, next.time - location.time)
            val color = session.speedRange.color(speed)


            polyline.apply {
                add(
                    LatLng(location.latitude, location.longitude),
                    LatLng(next.latitude, next.longitude)
                )

                width(width)
                color(color)
            }

        }

        map.addPolyline(polyline)
        map.addMarker(marker(session.locations.first(), "Start"))
        map.addMarker(marker(session.locations.last(), "Finish"))

        return this
    }

    override fun zoom(update: CameraUpdate): SessionMapTrackDrawer {

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

/*abstract class MapMode {

    lateinit var target: LatLng
    var zoom: Float? = null
    var bearing: Float? = null
    lateinit var bounds: LatLngBounds
    var mapWidth: Int? = null
    var mapHeight: Int? = null

    fun target(target: LatLng): MapMode {
        this.target = target

        return this
    }

    fun zoom(zoom: Float): MapMode {
        this.zoom = zoom

        return this
    }

    fun bearing(bearing: Float): MapMode {
        this.bearing = bearing

        return this
    }

    fun mapWidth(width: Int): MapMode {
        this.mapWidth = width

        return this
    }

    fun mapHeight(height: Int): MapMode {
        this.mapHeight = height

        return this
    }

    abstract fun build(): CameraUpdate?
}

class NoZoomMapMode : MapMode() {
    override fun build(): CameraUpdate? = null
}

class NorthUpMapMode : MapMode() {

    override fun build(): CameraUpdate? {
        return CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(target)
                .zoom(zoom ?: 19F)
                .bearing(0F)
                .build()
        )
    }


}

class DirectionUpMapMode : MapMode() {

    override fun build(): CameraUpdate? {
        return CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(target)
                .zoom(zoom ?: 19F)
                .bearing(bearing ?: throw IllegalArgumentException("Specify bearing!"))
                .build()
        )
    }
}

class BoundsInclusiveMapMode

    : MapMode() {
    override fun build(): CameraUpdate? {

        val width = mapWidth ?: throw java.lang.IllegalArgumentException("Specify map width!")
        val height = mapHeight ?: throw java.lang.IllegalArgumentException("Specify map height!")

        val padding = (width * 0.05F).toInt() // offset from edges of the map 10% of screen

        return CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
    }


}*/

abstract class MapMode {

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
