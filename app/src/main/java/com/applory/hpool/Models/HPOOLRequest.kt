package com.applory.hpool.Models

import android.os.Parcel
import android.os.Parcelable

class HPOOLRequest(val id: String, val departure: String, val destination: String, val date: String, val time:String, val pickUpLocation: String, var number: String = "1") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(departure)
        parcel.writeString(destination)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(pickUpLocation)
        parcel.writeString(number)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HPOOLRequest> {
        override fun createFromParcel(parcel: Parcel): HPOOLRequest {
            return HPOOLRequest(parcel)
        }

        override fun newArray(size: Int): Array<HPOOLRequest?> {
            return arrayOfNulls(size)
        }
    }

}