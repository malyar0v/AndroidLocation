package ee.taltech.mmalia.backend

import ee.taltech.mmalia.Jackson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

abstract class BackendRequest(val path: String) {

    companion object {
        internal const val BASE_URL = "https://sportmap.akaver.com/api/v1.0"
        internal const val CONTENT_TYPE = "application/json"
    }

    protected fun toJson() = Jackson.mapper.writeValueAsString(this)

    abstract fun build(): Request
}

open class BackendGetRequest(path: String) : BackendRequest(path) {

    override fun build(): Request {
        return Request.Builder()
            .url(BASE_URL + path)
            .get()
            .addHeader("Content-Type", CONTENT_TYPE)
            .build()
    }
}

open class BackendPostRequest(path: String) : BackendRequest(path) {

    override fun build(): Request {
        return Request.Builder()
            .url(BASE_URL + path)
            .post(toJson().toRequestBody(CONTENT_TYPE.toMediaType()))
            .build()
    }
}

class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
) : BackendPostRequest("/account/register")

class LogInRequest(val email: String, val password: String) :
    BackendPostRequest("/account/login")

class NewSessionRequest(
    val name: String,
    val description: String,
    val recordedAt: Date,
    val paceMin: Int,
    val paceMax: Int
) : BackendPostRequest("/GpsSessions")

class LocationTypesRequest : BackendGetRequest("/GpsLocationTypes")

class SessionInfoRequest(val id: String) :
    BackendGetRequest("/GpsSessions/${id}")

class NewLocationRequest(
    val recordedAt: Date,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val verticalAccuracy: Float,
    val gpsSessionId: String,
    val gpsLocationTypeId: String
) : BackendPostRequest("/GpsLocations")