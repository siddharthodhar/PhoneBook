package com.siddhartho.phonebook.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs_count_table")
data class CallLogsCount(private var _count: Int?) {

    @PrimaryKey(autoGenerate = true)
    private var _countId: Long? = null
    var countId: Long?
        get() = _countId
        set(value) {
            _countId = value
        }

    val count: Int?
        get() = _count
}