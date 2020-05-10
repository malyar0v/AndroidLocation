package ee.taltech.mmalia

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ee.taltech.mmalia.activity.SessionMapActivity
import ee.taltech.mmalia.model.Session
import ee.taltech.mmalia.model.Session_
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.util.*

class SessionRecyclerViewAdapter(val context: Context, val sessions: MutableList<Session>) :
    RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionItemViewHolder>() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    val sessionBox: Box<Session> = ObjectBox.boxStore.boxFor()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionItemViewHolder {
        val layout = LayoutInflater.from(context).inflate(R.layout.session_item, parent, false)

        return SessionItemViewHolder(layout)
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: SessionItemViewHolder, position: Int) {
        val session = sessions[position]

        holder.title.text = session.title
        holder.startTime.text = Utils.Session.DATE_TIME_FORMATTER.format(Date(session.start))
        holder.endTime.text = Utils.Session.DATE_TIME_FORMATTER.format(Date(session.end))
        holder.distance.text = "${session.distance}".format(".1f").plus(" m")
        holder.locations.text = "${session.locations.size}"
        holder.checkpoints.text = "${session.checkpoints.size}"
        holder.waypoints.text = "${session.waypoints.size}"

        holder.itemView.setOnClickListener {

            context.startActivity(
                Intent(context, SessionMapActivity::class.java)
                    .apply {
                        putExtra(Session_.id.name, session.id)
                    })
        }

        holder.itemView.setOnLongClickListener {
            Log.d(TAG, "long click")

            Log.d(TAG, "BackendId: ${session.backendId}")

            SessionEditDialog(context, session,
                {
                    notifyItemChanged(position)
                    sessionBox.put(session)
                },
                {
                    sessionBox.remove(session)
                    sessions.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)
                })
                .create()
                .show()

            true
        }
    }

    inner class SessionItemViewHolder(sessionItem: View) : RecyclerView.ViewHolder(sessionItem) {

        val title = sessionItem.findViewById<TextView>(R.id.session_title_text_view)
        val startTime = sessionItem.findViewById<TextView>(R.id.session_start_time_text_view)
        val endTime = sessionItem.findViewById<TextView>(R.id.session_end_time_text_view)
        val distance = sessionItem.findViewById<TextView>(R.id.session_distance_text_view)
        val locations = sessionItem.findViewById<TextView>(R.id.session_locations_text_view)
        val checkpoints = sessionItem.findViewById<TextView>(R.id.session_checkpoints_text_view)
        val waypoints = sessionItem.findViewById<TextView>(R.id.session_waypoints_text_view)
    }
}