package ee.taltech.mmalia.model

import android.graphics.Color
import ee.taltech.mmalia.db.IntRangeConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class SpeedRange(
    @Convert(converter = IntRangeConverter::class, dbType = String::class)
    val optimalSpeedRange: IntRange
) {

    companion object {

        val DEFAULT_MIN = 5
        val DEFAULT_MAX = 8
        val DEFAULT =
            SpeedRange(DEFAULT_MIN..DEFAULT_MAX)

        public const val COLOR_SLOW = Color.RED
        public const val COLOR_OPTIMAL = Color.YELLOW
        public const val COLOR_FAST = Color.GREEN
    }

    @Id
    var id: Long = 0

    val min = optimalSpeedRange.first
    val max = optimalSpeedRange.last

/*    constructor() : this(DEFAULT_MIN..DEFAULT_MAX)*/

    fun color(speed: Float): Int {
        return when {
            speed.toInt() in optimalSpeedRange -> COLOR_OPTIMAL
            speed < optimalSpeedRange.last -> COLOR_FAST
            else -> COLOR_SLOW
        }
    }

    override fun toString() = "$optimalSpeedRange"
}