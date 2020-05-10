package ee.taltech.mmalia.backend

import android.util.Log
import com.fasterxml.jackson.databind.type.TypeFactory
import ee.taltech.mmalia.Jackson.mapper
import ee.taltech.mmalia.OkHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*


abstract class Query(val request: BackendRequest) {

    companion object {
        val TAG = this::class.java.declaringClass!!.simpleName
    }

    fun execute() {

        OkHttpClient.client.newCall(request.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(BackendError.from(response))
                    } else onSuccess(response)
                }
            }
        })
        // FOR SYNCHRONOUS

/*        OkHttpClient.client.newCall(request.build()).execute().use { response ->
            if (!response.isSuccessful) onError(response)


        }*/
    }

    protected fun <T : Any> toObject(json: String?, clazz: Class<T>): T {
        return mapper.readValue(json, clazz)
    }

    abstract fun onSuccess(response: Response)
    abstract fun onError(error: BackendError)
}

class RegisterQuery(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val onSuccessCallback: (BackendResponse.RegisterResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit
) : Query(RegisterRequest(firstName, lastName, email, password)) {

    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val registerResponse = toObject(body, BackendResponse.RegisterResponse::class.java)

        Log.d(TAG, "Successfully registered! Status: ${registerResponse.status}")
        onSuccessCallback(registerResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error registering: ${error}")
        onErrorCallback(error)
    }
}

class LogInQuery(
    val email: String,
    val password: String,
    val onSuccessCallback: (BackendResponse.LogInResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit
) : Query(LogInRequest(email, password)) {

    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val logInResponse = toObject(body, BackendResponse.LogInResponse::class.java)

        Log.d(TAG, "Successfully logged in! Token: ${logInResponse.token}")
        onSuccessCallback(logInResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error logging in: ${error}")
        onErrorCallback(error)
    }
}

class NewSessionQuery(
    val name: String,
    val description: String,
    val recordedAt: Date,
    val onSuccessCallback: (BackendResponse.SessionResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit,
    val paceMin: Int = 420,
    val paceMax: Int = 600
) : Query(NewSessionRequest(name, description, recordedAt, paceMin, paceMax)) {

    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val newSessionResponse = toObject(body, BackendResponse.SessionResponse::class.java)

        Log.d(TAG, "Successfully created new session! Session id: ${newSessionResponse.id}")
        onSuccessCallback(newSessionResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error creating new Session! ${error}")
        onErrorCallback(error)
    }
}

class LocationTypesQuery(
    val onSuccessCallback: (BackendResponse.LocationTypesResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit
) : Query(LocationTypesRequest()) {

    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val locationTypesResponse =
            BackendResponse.LocationTypesResponse(
                mapper.readValue(
                    body,
                    TypeFactory.defaultInstance().constructCollectionType(
                        List::class.java,
                        BackendResponse.LocationTypesResponse.LocationType::class.java
                    )
                )
            )

        Log.d(TAG, "Successfully obtained location types: ${locationTypesResponse.types}")
        onSuccessCallback(locationTypesResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error obtaining location types! $error")
        onErrorCallback(error)
    }
}

class SessionInfoQuery(
    val id: String,
    val onSuccessCallback: (BackendResponse.SessionResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit
) : Query(SessionInfoRequest(id)) {

    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val sessionInfoResponse = toObject(body, BackendResponse.SessionResponse::class.java)

        Log.d(TAG, "Successfully obtained session info: ${sessionInfoResponse}")
        onSuccessCallback(sessionInfoResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error obtaining session info! $error")
        onErrorCallback(error)
    }
}

class NewLocationQuery(
    val recordedAt: Date,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val verticalAccuracy: Float,
    val gpsSessionId: String,
    val gpsLocationTypeId: String,
    val onSuccessCallback: (BackendResponse.NewLocationResponse) -> Unit,
    val onErrorCallback: (BackendError) -> Unit
) : Query(
    NewLocationRequest(
        recordedAt,
        latitude,
        longitude,
        accuracy,
        altitude,
        verticalAccuracy,
        gpsSessionId,
        gpsLocationTypeId
    )
) {
    override fun onSuccess(response: Response) {

        val body = response.body?.string()

        val newLocationResponse = toObject(body, BackendResponse.NewLocationResponse::class.java)

        Log.d(TAG, "Successfully sent new location: $newLocationResponse")
        onSuccessCallback(newLocationResponse)
    }

    override fun onError(error: BackendError) {
        Log.d(TAG, "Error sending new location! $error")
        onErrorCallback(error)
    }
}