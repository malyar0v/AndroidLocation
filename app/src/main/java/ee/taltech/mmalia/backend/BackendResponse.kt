package ee.taltech.mmalia.backend

import java.util.*

object BackendResponse {

    data class RegisterResponse(val token: String, val status: String) {

        constructor() : this("", "")
    }

    data class LogInResponse(val token: String, val status: String) {

        constructor() : this("", "")
    }

    data class SessionResponse(
        val name: String,
        val description: String,
        val recordedAt: String,
        val duration: Int,
        val speed: Int,
        val distance: Int,
        val climb: Int,
        val descent: Int,
        val appUserId: String,
        val id: String
    ) {
        constructor() : this("", "", "", 0, 0, 0, 0, 0, "", "")
    }

    class LocationTypesResponse(
        val types: List<LocationType>
    ) {
        data class LocationType(
            val name: String,
            val description: String,
            val id: String
        ) {
            constructor() : this("", "", "")
        }
    }

    data class NewLocationResponse(
        val recordedAt: Date?,
        val latitude: Double,
        val longitude: Double,
        val accuracy: Double,
        val altitude: Double,
        val verticalAccuracy: Double,
        val gpsSessionId: String,
        val gpsLocationTypeId: String,
        val appUserId: String,
        val id: String
    ) {
        constructor() : this(null, 0.0, 0.0, 0.0, 0.0, 0.0, "", "", "", "")
    }
}