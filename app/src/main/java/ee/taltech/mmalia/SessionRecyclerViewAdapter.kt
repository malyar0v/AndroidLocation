package ee.taltech.mmalia

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ee.taltech.mmalia.model.Session
import java.util.*

class SessionRecyclerViewAdapter(val context: Context, val sessions: List<Session>) :
    RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionItemViewHolder>() {

    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionItemViewHolder {
        val layout = LayoutInflater.from(context).inflate(R.layout.session_item, parent, false)

        return SessionItemViewHolder(layout)
    }

    override fun getItemCount() = sessions.size

    override fun onBindViewHolder(holder: SessionItemViewHolder, position: Int) {
        val session = sessions[position]

        holder.startTime.text = Utils.Session.DATE_TIME_FORMATTER.format(Date(session.start))
        holder.endTime.text = Utils.Session.DATE_TIME_FORMATTER.format(Date(session.end))
        holder.locations.text = "${session.locations.size}"
        holder.checkpoints.text = "${session.checkpoints.size}"
        holder.waypoints.text = "${session.waypoints.size}"
    }

    inner class SessionItemViewHolder(sessionItem: View) : RecyclerView.ViewHolder(sessionItem) {

        val startTime = sessionItem.findViewById<TextView>(R.id.session_start_time_text_view)
        val endTime = sessionItem.findViewById<TextView>(R.id.session_end_time_text_view)
        val locations = sessionItem.findViewById<TextView>(R.id.session_locations_text_view)
        val checkpoints = sessionItem.findViewById<TextView>(R.id.session_checkpoints_text_view)
        val waypoints = sessionItem.findViewById<TextView>(R.id.session_waypoints_text_view)
    }
}