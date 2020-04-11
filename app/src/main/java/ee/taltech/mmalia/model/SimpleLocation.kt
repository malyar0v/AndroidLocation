package ee.taltech.mmalia.model

import android.location.Location
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class SimpleLocation(var latitude: Double, var longitude: Double, var time: Long) {

    @Id var id: Long = 0

    companion object {

        fun from(location: Location): SimpleLocation =
            SimpleLocation(location.latitude, location.longitude, location.time)
    }
}