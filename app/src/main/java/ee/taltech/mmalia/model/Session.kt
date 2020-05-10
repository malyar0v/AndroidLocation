package ee.taltech.mmalia.model

import android.os.Parcel
import android.os.Parcelable
import ee.taltech.mmalia.db.SpeedRangeConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
class Session() : Parcelable {

    @Id
    var id: Long = 0

    var title: String = "Title"
    var description: String = ""
    var distance: Float = 0F

    @Convert(converter = SpeedRangeConverter::class, dbType = String::class)
    var speedRange: SpeedRange = SpeedRange.DEFAULT

    val start: Long = Date().time

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

    var backendId: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString() ?: title
        description = parcel.readString() ?: description
        distance = parcel.readFloat()
        backendId = parcel.readString() ?: backendId
    }

    override fun toString(): String {
        return "\nStart: ${start}\n\tLs:${locations}\n\tCPs: ${checkpoints}\n\tWPs: ${waypoints}\nEnd: ${end}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeFloat(distance)
        parcel.writeString(backendId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Session> {

        val DEFAULT = Session()

        override fun createFromParcel(parcel: Parcel): Session {
            return Session(parcel)
        }

        override fun newArray(size: Int): Array<Session?> {
            return arrayOfNulls(size)
        }
    }
}