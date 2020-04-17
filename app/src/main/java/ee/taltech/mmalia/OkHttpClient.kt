package ee.taltech.mmalia

import okhttp3.Authenticator
import okhttp3.OkHttpClient

object OkHttpClient {

    var client: OkHttpClient = init()
        private set

    private fun init(): OkHttpClient {
        return OkHttpClient()
    }

    fun authenticate(authenticator: Authenticator): OkHttpClient {
        client = client
            .newBuilder()
            .authenticator(authenticator).build()

        return client
    }
}