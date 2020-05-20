package ee.taltech.mmalia

import ee.taltech.mmalia.model.Session
import java.text.SimpleDateFormat
import java.util.*

class GpxWriter(val session: Session) {

    companion object {
        val DF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    fun create(): String {
        val sb = StringBuilder()

        sb.append("<gpx version=\"1.0\">")
        sb.append("<name>${session.title} | ${session.description}</name>")

        session.waypoints.plus(session.checkpoints).sortedBy { l -> l.time }.forEach {
            sb.append("<wpt lat=\"${it.latitude}\" lon=\"${it.longitude}\"/>")
        }

        sb.append("<trk>")
        sb.append("<trkseg>")

        session.locations.sortedBy { it.time }.forEach {
            sb.append(
                "<trkpt lat=\"${it.latitude}\" lon=\"${it.longitude}\">" +
                        "<time>${DF.format(Date(it.time))}</time>" +
                        "</trkpt>"
            )
        }

        sb.append("</trkseg>")
        sb.append("</trk>")
        sb.append("</gpx>")

        return sb.toString()
    }
}