package com.siddhartho.phonebook.dataclass

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_number_table")
data class ContactNumber(
    private var _contactOwnerId: Long?,
    private var _countryCode: String?,
    private var _number: String?
) :
    Parcelable {

    @PrimaryKey(autoGenerate = true)
    private var _contactNumberId: Long? = null
    var contactNumberId: Long?
        get() = _contactNumberId
        set(value) {
            _contactNumberId = value
        }

    var contactOwnerId: Long?
        get() = _contactOwnerId
        set(value) {
            _contactOwnerId = value
        }

    var countryCode: String?
        get() = _countryCode
        set(value) {
            _countryCode = value
        }

    var number: String?
        get() = _number
        set(value) {
            _number = value
        }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()
    ) {
        _contactNumberId = parcel.readLong()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        _contactOwnerId?.let { dest?.writeLong(it) }
        _countryCode?.let { dest?.writeString(it) }
        _number?.let { dest?.writeString(it) }
        _contactNumberId?.let { dest?.writeLong(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContactNumber> {
        override fun createFromParcel(parcel: Parcel): ContactNumber {
            return ContactNumber(parcel)
        }

        override fun newArray(size: Int): Array<ContactNumber?> {
            return arrayOfNulls(size)
        }
    }
}