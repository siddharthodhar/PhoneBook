package com.siddhartho.phonebook.dataclass

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithContactNumbers(
    @Embedded private var _contact: Contact?,
    @Relation(
        parentColumn = "_contactId", entityColumn = "_contactOwnerId"
    )
    private var _contactNumbers: List<ContactNumber>?
) : Parcelable {

    var contact: Contact?
        get() = _contact
        set(value) {
            _contact = value
        }

    var contactNumbers: ArrayList<ContactNumber>?
        get() = _contactNumbers as ArrayList<ContactNumber>
        set(value) {
            _contactNumbers = value
        }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Contact::class.java.classLoader),
        parcel.createTypedArrayList(ContactNumber)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(_contact, flags)
        parcel.writeTypedList(_contactNumbers)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isValid(): Boolean {
        Log.d(TAG, "isValid() called $this")
        var result = true
        if (_contact?.name.isNullOrEmpty())
            result = false
        else
            _contactNumbers?.let {
                for (item in it) {
                    if (item.number.isNullOrEmpty()) {
                        contactNumbers?.remove(item)
                        _contactNumbers = contactNumbers
                    } else if (!item.number!!.matches(Regex("^\\d{1,10}?$")) || (!item.countryCode.isNullOrEmpty() && !(item.countryCode + item.number).matches(
                            Regex("^\\+\\d{1,12}?$")
                        ))
                    )
                        result = false
                }
            }
        return result
    }

    companion object CREATOR : Parcelable.Creator<ContactWithContactNumbers> {
        override fun createFromParcel(parcel: Parcel): ContactWithContactNumbers {
            return ContactWithContactNumbers(parcel)
        }

        override fun newArray(size: Int): Array<ContactWithContactNumbers?> {
            return arrayOfNulls(size)
        }

        private const val TAG = "ContactWithNumber"
    }
}