package app.android.outlinevpntv.data.model

import android.os.Parcel
import android.os.Parcelable

data class ShadowSocksInfo(
    val method: String,
    val password: String,
    val host: String,
    val port: Int,
    val prefix: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(method)
        parcel.writeString(password)
        parcel.writeString(host)
        parcel.writeInt(port)
        parcel.writeString(prefix)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ShadowSocksInfo> {
        override fun createFromParcel(parcel: Parcel) = ShadowSocksInfo(parcel)
        override fun newArray(size: Int): Array<ShadowSocksInfo?> = arrayOfNulls(size)
    }
}
