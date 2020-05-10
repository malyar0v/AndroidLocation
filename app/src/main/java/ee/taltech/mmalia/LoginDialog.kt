package ee.taltech.mmalia

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class LoginDialog(val ctx: Context, val onLogin: (email: String, password: String) -> Unit) {

    fun create(): AlertDialog {

        val view = LayoutInflater.from(ctx).inflate(R.layout.login_dialog, null, false)

        return AlertDialog.Builder(ctx)
            .setView(view)
            .setPositiveButton("Login") { _, _ ->

                val email =
                    view.findViewById<EditText>(R.id.dialog_login_email_edit_text).text.toString()
                val password =
                    view.findViewById<EditText>(R.id.dialog_login_password_edit_text).text.toString()

                onLogin(email, password)
            }
            .setNegativeButton("Cancel") {_, _ -> }
            .create()
    }
}