package com.example.diplomaapp

import android.os.Parcel
import android.os.Parcelable

class Message(private var avatar: Int, private var personName: String?, private var personMessage: String?, private var messageDate: String?): Parcelable {

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Message> {
            override fun createFromParcel(parcel: Parcel) = Message(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Message>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        avatar = parcel.readInt(),
        personName = parcel.readString(),
        personMessage = parcel.readString(),
        messageDate = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(avatar)
        parcel.writeString(personName)
        parcel.writeString(personMessage)
        parcel.writeString(messageDate)
    }

    override fun describeContents() = 0

    fun getAvatar(): Int {
        return avatar
    }

    fun getPersonName(): String? {
        return personName
    }

    fun getPersonMessage(): String? {
        return personMessage
    }

    fun getMessageDate(): String? {
        return messageDate
    }
}