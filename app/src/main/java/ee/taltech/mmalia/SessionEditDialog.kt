package ee.taltech.mmalia

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ee.taltech.mmalia.Utils.Extensions.parse
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.SpeedRange

class SessionEditDialog(
    val context: Context,
    val session: Session,
    val onSave: (dialog: DialogInterface) -> Unit,
    val onDelete: (dialog: DialogInterface) -> Unit,
    val onCancel: (dialog: DialogInterface) -> Unit = {}
) {

    fun create(): AlertDialog {
        val view =
            LayoutInflater.from(context).inflate(R.layout.session_edit, null, false)
        val titleEditText = view.findViewById<EditText>(R.id.session_dialog_title_edit_text)
            .apply { setText(session.title) }
        val descriptionEditText =
            view.findViewById<EditText>(R.id.session_dialog_description_edit_text)
                .apply { setText(session.description) }
        val minEditText = view.findViewById<EditText>(R.id.dialog_optimal_speed_min_edit_text)
            .apply { setText(session.speedRange.min.toString()) }
        val maxEditText = view.findViewById<EditText>(R.id.dialog_optimal_speed_max_edit_text)
            .apply { setText(session.speedRange.max.toString()) }

        return AlertDialog.Builder(context).apply {
            setView(view)
            setPositiveButton("Save") { dialog, _ ->

                session.title = titleEditText.text.toString()
                session.description = descriptionEditText.text.toString()
                SpeedRange.parse(minEditText, maxEditText)?.let { session.speedRange = it }

                onSave(dialog)
            }
            setNegativeButton("Delete") { dialog, which ->

                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.session_deletion_confirmation))
                    .setPositiveButton("Yes") { _, _ -> onDelete(dialog) }
                    .setNegativeButton("No") { _, _ ->  }
                    .create()
                    .show()
            }
            setNeutralButton("Cancel") { dialog, which ->
                onCancel(dialog)
            }
        }.create()
    }
}