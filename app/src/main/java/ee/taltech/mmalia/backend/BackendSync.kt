package ee.taltech.mmalia.backend

import android.util.Log
import ee.taltech.mmalia.model.SimpleLocation
import java.util.*

class BackendSync(
    val state: State
) : TimerTask() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun run() {
        syncAll(state.locations, BackendResponse.LocationTypesResponse.LOC)
        syncAll(state.checkpoints, BackendResponse.LocationTypesResponse.CP)
        syncAll(state.waypoints, BackendResponse.LocationTypesResponse.WP)
    }

    private fun syncAll(locations: Queue<SimpleLocation>, typeId: String) {
        while (locations.isNotEmpty()) {
            val l = locations.remove()

            sync(l, typeId)
        }
    }

    private fun sync(location: SimpleLocation, typeId: String) {
        NewLocationQuery(
            Date(location.time),
            location.latitude,
            location.longitude,
            location.accuracy,
            location.altitude,
            location.accuracy,
            state.sessionId,
            typeId,
            { response -> Log.d(TAG, "New location success!") },
            {}).execute()
    }

    class State(
        val sessionId: String,
        val locations: Queue<SimpleLocation>,
        val checkpoints: Queue<SimpleLocation>,
        val waypoints: Queue<SimpleLocation>
    )
}
