package ee.taltech.mmalia.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
class Session {

    @Id
    var id: Long = 0

    val start: Long

    lateinit var locations: MutableList<SimpleLocation>
    lateinit var checkpoints: MutableList<SimpleLocation>
    lateinit var waypoints: MutableList<SimpleLocation>

    val end: Long
        get() {
            return try {
                locations.last().time
            } catch (e: NoSuchElementException) {
                start
            }
        }

    init {
        start = Date().time
    }

    override fun toString(): String {
        return "\nStart: ${start}\n\tLs:${locations}\n\tCPs: ${checkpoints}\n\tWPs: ${waypoints}\nEnd: ${end}"
    }
}