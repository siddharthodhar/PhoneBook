package com.siddhartho.phonebook.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import android.widget.Toast
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "Utils"

fun Context.showToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT
) {
    Log.d(
        TAG,
        "showToast() called with: message = [$message], duration = [$duration]"
    )
    Toast.makeText(this, message, duration).show()
}

fun Context.callThroughIntent(number: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = Uri.parse("tel:$number")
    startActivity(intent)
}

object ContentResolverForCallLog {
    private val datetimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    private var cursor: Cursor? = null

    fun setCursor(contentResolver: ContentResolver): Completable {
        Log.d(TAG, "setCursor() called with: contentResolver = $contentResolver")
        return Completable.create {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                ),
                null,
                null,
                null
            )
            if (cursor == null)
                it.onError(NullPointerException())
            else it.onComplete()
        }
    }

    fun getCellPhoneCallLogsCount(): Single<Int> {
        Log.d(TAG, "getCellPhoneCallLogsCount() called")
        return Single.create { emitter ->
            if (cursor == null)
                emitter.onError(NullPointerException())
            cursor?.let { emitter.onSuccess(it.count) }
        }
    }

    fun getCallLogs(): Observable<Map<String, String>> {
        Log.d(TAG, "getCallLogs() called")
        return Observable.create { emitter ->
            if (cursor == null)
                emitter.onError(NullPointerException())
            cursor?.let {
                if (it.moveToLast()) {
                    val callLogsMap = HashMap<String, String>()
                    callLogsMap[Constants.CONTACT_NUMBER] =
                        it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                    callLogsMap[Constants.CALL_DATE] =
                        datetimeFormat.format(
                            Date(
                                it.getString(it.getColumnIndex(CallLog.Calls.DATE)).toLong()
                            )
                        )
                    callLogsMap[Constants.CALL_DURATION] =
                        getDurationFormat(it.getString(it.getColumnIndex(CallLog.Calls.DURATION)))
                    callLogsMap[Constants.CALL_TYPE] =
                        it.getString(it.getColumnIndex(CallLog.Calls.TYPE))
                    emitter.onNext(callLogsMap)
                    emitter.onComplete()
                } else emitter.onError(Exception("Cursor did not move to last."))
            }
        }
    }

    private fun getDurationFormat(duration: String): String {
        Log.d(TAG, "getDurationFormat() called with: duration = $duration")
        val dur = duration.toInt()
        return getTwoDigits(dur / 3600) + ":" + getTwoDigits(dur % 3600 / 60) + ":" + getTwoDigits(
            dur % 3600 % 60
        )
    }

    private fun getTwoDigits(number: Int): String {
        Log.d(TAG, "getTwoDigits() called with: number = $number")
        if (number == 0) return "00" else if (number / 10 == 0) return "0$number"
        return number.toString()
    }
}

object Constants {
    const val CONTACT_WITH_NUMBER_KEY = "contact_with_contact_numbers"
    const val DEFAULT_CC = "+91"
    const val CONTACT_NUMBER = "contact_number"
    const val CALL_DATE = "call_date"
    const val CALL_DURATION = "call_duration"
    const val CALL_TYPE = "call_type"
    const val FOR_CALL_LOG = "for_call_log"
    const val FOR_REMOVE_NOTIFICATION = "for_remove_notification"
}