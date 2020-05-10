package ee.taltech.mmalia

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class SessionStartDialog(
    val context: Context,
    val onStart: (title: String, description: String) -> Unit,
    val onCancel: (dialog: DialogInterface) -> Unit = {}
) {

    fun create(): AlertDialog {
        val view =
            LayoutInflater.from(context).inflate(R.layout.session_info, null, false)
        val titleEditText = view.findViewById<EditText>(R.id.session_dialog_title_edit_text)
        val descriptionEditText =
            view.findViewById<EditText>(R.id.session_dialog_description_edit_text)

        return AlertDialog.Builder(context).apply {
            setView(view)
            setPositiveButton("Start") { dialog, _ ->

                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()

                onStart(title, description)
            }
            setNeutralButton("Cancel") { dialog, which ->
                onCancel(dialog)
            }
        }.create()
    }
}