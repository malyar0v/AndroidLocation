package ee.taltech.mmalia.backend

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class BackendAuthenticator(val token: String) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") != null) {
            return null // Give up, we've already attempted to authenticate.
        }

        println("Authenticating for response: $response")
        val credential = "Bearer $token"
        return response.request.newBuilder()
            .header("Authorization", credential)
            .build()
    }
}