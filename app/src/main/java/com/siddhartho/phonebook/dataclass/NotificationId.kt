package com.siddhartho.phonebook.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_id_table")
data class NotificationId(private var _notificationId: Int?, private var _number: String?) {

    @PrimaryKey(autoGenerate = true)
    private var _notificationAutoId: Int? = null
    var notificationAutoId: Int?
        get() = _notificationAutoId
        set(value) {
            _notificationAutoId = value
        }

    val notificationId: Int?
        get() = _notificationId

    val number: String?
        get() = _number
}