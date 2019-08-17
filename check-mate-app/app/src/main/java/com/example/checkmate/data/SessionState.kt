package com.example.checkmate.data

import android.os.Parcel
import android.os.Parcelable


class SessionState(
    var billId: Long?,
    var isCreator: Boolean,
    var personalPaymentAmount: Double,
    var totalPaymentAmount: Double,
    var myColor: String?,
    var restaurant: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(billId ?: 0)
        parcel.writeByte(if (isCreator) 1 else 0)
        parcel.writeDouble(personalPaymentAmount)
        parcel.writeDouble(totalPaymentAmount)
        parcel.writeString(myColor)
        parcel.writeString(restaurant)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SessionState> {
        override fun createFromParcel(parcel: Parcel): SessionState {
            return SessionState(parcel)
        }

        override fun newArray(size: Int): Array<SessionState?> {
            return arrayOfNulls(size)
        }
    }

}