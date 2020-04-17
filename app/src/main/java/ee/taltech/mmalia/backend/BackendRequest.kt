package ee.taltech.mmalia.backend

import ee.taltech.mmalia.Jackson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

abstract class BackendRequest(val url: String) {

    companion object {
        internal const val CONTENT_TYPE = "application/json"
    }

    protected fun toJson() = Jackson.mapper.writeValueAsString(this)

    abstract fun build(): Request
}

open class BackendGetRequest(url: String) : BackendRequest(url) {

    override fun build(): Request {
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }
}

open class BackendPostRequest(url: String) : BackendRequest(url) {

    override fun build(): Request {
        return Request.Builder()
            .url(url)
            .post(toJson().toRequestBody(CONTENT_TYPE.toMediaType()))
            .build()
    }
}

class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
) : BackendPostRequest("https://sportmap.akaver.com/api/account/register")

class LogInRequest(val email: String, val password: String) :
    BackendPostRequest("https://sportmap.akaver.com/api/account/login")

class NewSessionRequest(
    val name: String,
    val description: String,
    val recordedAt: Date
) : BackendPostRequest("https://sportmap.akaver.com/api/GpsSessions")

class LocationTypesRequest : BackendGetRequest("https://sportmap.akaver.com/api/GpsLocationTypes")

class SessionInfoRequest(val id: String) :
    BackendGetRequest("https://sportmap.akaver.com/api/GpsSessions/${id}")

class NewLocationRequest(
    val recordedAt: Date,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val altitude: Double,
    val verticalAccuracy: Double,
    val gpsSessionId: String,
    val gpsLocationTypeId: String
) : BackendPostRequest("https://sportmap.akaver.com/api/GpsLocations")