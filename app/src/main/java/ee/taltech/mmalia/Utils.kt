package ee.taltech.mmalia

import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    class NavigationData {

        companion object {

            const val PATTERN = "H:mm:ss"
            val FORMATTER = SimpleDateFormat(PATTERN).apply { timeZone = TimeZone.getTimeZone("GMT") }

            fun distance(from: Location, to: Location) = from.distanceTo(to)
            fun distance(meters: Float) = "%.1f".format(meters)

            fun duration(millis: Long) = FORMATTER.format(Date(millis))

            fun speed(distance: Float, time: Long) =
                1 / ((distance / 1000f) / (time / (1000 * 60f)))

            fun speed(speed: Float): String {

                if (speed > 600 || speed.isNaN()) return "∞"

                val secs = ((speed % 1) * 60).toInt()
                val mins = speed.toInt()

                return "${mins}:${secs}"
            }
        }
    }
}