package ee.taltech.mmalia.backend

import okhttp3.Response

data class BackendError(val code: Int, val body: String, val message: String) {

    val authorized = code != 401

    companion object {
        fun from(response: Response) =
            BackendError(response.code, response.body?.string() ?: "", response.message)
    }
}