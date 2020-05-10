package ee.taltech.mmalia.backend

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ee.taltech.mmalia.Utils
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class BackendAuthenticator(val ctx: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") != null) {
            Handler(Looper.getMainLooper()).post {
                Utils.Api.clearToken(ctx)
                Toast.makeText(ctx, "Log in for syncing to work", Toast.LENGTH_LONG).show()
            }
            return null // Give up, we've already attempted to authenticate.
        }

        println("Authenticating for response: $response")
        val credential = "Bearer ${Utils.Api.getToken(ctx)}"
        return response.request.newBuilder()
            .header("Authorization", credential)
            .build()
    }
}