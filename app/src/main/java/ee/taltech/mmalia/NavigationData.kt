package ee.taltech.mmalia

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import ee.taltech.mmalia.Utils.NavigationData.Companion.distance
import ee.taltech.mmalia.Utils.NavigationData.Companion.duration
import ee.taltech.mmalia.Utils.NavigationData.Companion.speed


interface NavigationDataUi {

    fun sessionDistance(): String
    fun sessionDuration(): String
    fun sessionSpeed(): String
    fun cpDistance(): String
    fun cpDirectDistance(): String
    fun cpSpeed(): String
    fun wpDistance(): String
    fun wpDirectDistance(): String
    fun wpSpeed(): String
}

class NavigationData() : NavigationDataUi, Parcelable {

    companion object CREATOR : Parcelable.Creator<NavigationData> {

        private val TAG = this::class.java.declaringClass!!.simpleName

        override fun createFromParcel(parcel: Parcel): NavigationData {
            return NavigationData(parcel)
        }

        override fun newArray(size: Int): Array<NavigationData?> {
            return arrayOfNulls(size)
        }
    }

    // last received location
    var currentLocation: Location? = null
    var startLocation: Location? = null
    var sessionStartTime = 0L

    var sessionDistance = 0f
    var sessionDuration = 0L
    var sessionSpeed = 0f

    var cpLocation: Location? = null
    var cpStartTime = 0L
    var cpDuration = 0L
    var cpDistance = 0f
    var cpDistanceDirect = 0f
    var cpSpeed = 0f

    var wpLocation: Location? = null
    var wpStartTime = 0L
    var wpDuration = 0L
    var wpDistance = 0f
    var wpDistanceDirect = 0f
    var wpSpeed = 0f

    constructor(parcel: Parcel) : this() {
        currentLocation = parcel.readParcelable(Location::class.java.classLoader)
        startLocation = parcel.readParcelable(Location::class.java.classLoader)
        sessionStartTime = parcel.readLong()
        sessionDistance = parcel.readFloat()
        sessionDuration = parcel.readLong()
        sessionSpeed = parcel.readFloat()
        cpLocation = parcel.readParcelable(Location::class.java.classLoader)
        cpStartTime = parcel.readLong()
        cpDuration = parcel.readLong()
        cpDistance = parcel.readFloat()
        cpDistanceDirect = parcel.readFloat()
        cpSpeed = parcel.readFloat()
        wpLocation = parcel.readParcelable(Location::class.java.classLoader)
        wpStartTime = parcel.readLong()
        wpDuration = parcel.readLong()
        wpDistance = parcel.readFloat()
        wpDistanceDirect = parcel.readFloat()
        wpSpeed = parcel.readFloat()
    }

    fun isFirstLocation() = startLocation == null

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(currentLocation, flags)
        parcel.writeParcelable(startLocation, flags)
        parcel.writeLong(sessionStartTime)
        parcel.writeFloat(sessionDistance)
        parcel.writeLong(sessionDuration)
        parcel.writeFloat(sessionSpeed)
        parcel.writeParcelable(cpLocation, flags)
        parcel.writeLong(cpStartTime)
        parcel.writeLong(cpDuration)
        parcel.writeFloat(cpDistance)
        parcel.writeFloat(cpDistanceDirect)
        parcel.writeFloat(cpSpeed)
        parcel.writeParcelable(wpLocation, flags)
        parcel.writeLong(wpStartTime)
        parcel.writeLong(wpDuration)
        parcel.writeFloat(wpDistance)
        parcel.writeFloat(wpDistanceDirect)
        parcel.writeFloat(wpSpeed)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun sessionDistance(): String = distance(sessionDistance)

    override fun sessionDuration(): String = duration(sessionDuration)

    override fun sessionSpeed(): String = speed(sessionSpeed)

    override fun cpDistance(): String = distance(cpDistance)

    override fun cpDirectDistance(): String = distance(cpDistanceDirect)

    override fun cpSpeed(): String = speed(cpSpeed)

    override fun wpDistance(): String = distance(wpDistance)

    override fun wpDirectDistance(): String = distance(wpDistanceDirect)

    override fun wpSpeed(): String = speed(wpSpeed)

}
