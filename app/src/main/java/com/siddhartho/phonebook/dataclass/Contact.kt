package com.siddhartho.phonebook.dataclass

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class Contact(private var _name: String?) : Parcelable {

    @PrimaryKey(autoGenerate = true)
    private var _contactId: Long? = null
    var contactId: Long?
        get() = _contactId
        set(value) {
            _contactId = value
        }

    var name: String?
        get() = _name
        set(value) {
            _name = value
        }

    constructor(parcel: Parcel) : this(
        parcel.readString()
    ) {
        _contactId = parcel.readLong()
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        _name?.let { dest?.writeString(it) }
        _contactId?.let { dest?.writeLong(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }

}