package ee.taltech.mmalia.model

import android.os.Parcel
import android.os.Parcelable
import ee.taltech.mmalia.MapMode

class ActivityState() : Parcelable {

    var compass: Boolean = false
    var navigationData: NavigationData = NavigationData()
    var mapMode: Int = MapMode.NORTH_UP
    var session: Session = Session()

    constructor(parcel: Parcel) : this() {
        compass = parcel.readByte() != 0.toByte()
        navigationData = parcel.readParcelable(NavigationData::class.java.classLoader) ?: navigationData
        mapMode = parcel.readInt()
        session = parcel.readParcelable(NavigationData::class.java.classLoader) ?: session
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (compass) 1 else 0)
        parcel.writeParcelable(navigationData, flags)
        parcel.writeInt(mapMode)
        parcel.writeParcelable(session, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActivityState> {
        override fun createFromParcel(parcel: Parcel): ActivityState {
            return ActivityState(parcel)
        }

        override fun newArray(size: Int): Array<ActivityState?> {
            return arrayOfNulls(size)
        }
    }
}