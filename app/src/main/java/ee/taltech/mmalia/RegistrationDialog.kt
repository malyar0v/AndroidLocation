package ee.taltech.mmalia

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class RegistrationDialog(
    val ctx: Context,
    val onRegister: (firstName: String, lastName: String, email: String, password: String) -> Unit
) {

    fun create(): AlertDialog {

        val view = LayoutInflater.from(ctx).inflate(R.layout.registration_dialog, null, false)

        return AlertDialog.Builder(ctx)
            .setView(view)
            .setPositiveButton("Register") { _, _ ->
                val firstName =
                    view.findViewById<EditText>(R.id.dialog_registration_first_name_edit_text).text.toString()
                val lastName =
                    view.findViewById<EditText>(R.id.dialog_registration_last_name_edit_text).text.toString()
                val email =
                    view.findViewById<EditText>(R.id.dialog_login_email_edit_text).text.toString()
                val password =
                    view.findViewById<EditText>(R.id.dialog_login_password_edit_text).text.toString()

                onRegister(firstName, lastName, email, password)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
    }
}