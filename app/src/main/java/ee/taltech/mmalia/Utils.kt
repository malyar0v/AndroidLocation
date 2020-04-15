package ee.taltech.mmalia

import android.location.Location
import android.widget.EditText
import ee.taltech.mmalia.model.SpeedRange
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    object Extensions {

        fun SpeedRange.Companion.parse(
            minEditText: EditText,
            maxEditText: EditText
        ): SpeedRange? {
            val min = try {
                minEditText.text.toString().toInt()
            } catch (e: IllegalArgumentException) {
                return null
            }
            val max = try {
                maxEditText.text.toString().toInt()
            } catch (e: IllegalArgumentException) {
                return null
            }

            if (max < min) return null

            return SpeedRange(min..max)
        }
    }

    class NavigationData {

        companion object {

            const val PATTERN = "H:mm:ss"
            val TIME_FORMATTER =
                SimpleDateFormat(PATTERN).apply { timeZone = TimeZone.getTimeZone("GMT") }

            fun distance(from: Location, to: Location) = from.distanceTo(to)
            fun distance(meters: Float) = "%.1f".format(meters)

            fun duration(millis: Long) = TIME_FORMATTER.format(Date(millis))

            fun speed(distance: Float, time: Long) =
                1 / (distance / (time / 60))

            fun speed(speed: Float): String {

                if (speed > 600 || speed.isNaN()) return "∞"

                val secs = ((speed % 1) * 60).toInt()
                val mins = speed.toInt()

                return "${mins}:${secs}"
            }
        }
    }

    object Session {

        private const val DATE_TIME_PATTERN = "HH:mm:ss | dd/MM/YYYY"
        val DATE_TIME_FORMATTER =
            SimpleDateFormat(DATE_TIME_PATTERN)
    }
}