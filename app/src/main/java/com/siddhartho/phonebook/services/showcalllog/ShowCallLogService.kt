package com.siddhartho.phonebook.services.showcalllog

import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.IBinder
import android.provider.CallLog
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.siddhartho.phonebook.R
import com.siddhartho.phonebook.activities.displaycontacts.DisplayContactsActivity
import com.siddhartho.phonebook.databinding.CallLogScreenBinding
import com.siddhartho.phonebook.dataclass.CallLogsCount
import com.siddhartho.phonebook.dataclass.ContactWithContactNumbers
import com.siddhartho.phonebook.dataclass.NotificationId
import com.siddhartho.phonebook.utils.Constants
import com.siddhartho.phonebook.utils.ContentResolverForCallLog
import com.siddhartho.phonebook.utils.callThroughIntent
import com.siddhartho.phonebook.viewmodel.ContactsViewModel
import dagger.android.DaggerService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class ShowCallLogService : DaggerService() {

    @Inject
    lateinit var contactsViewModel: ContactsViewModel

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    @field:[Named(Constants.MISSED_CALL_CHANNEL_ID_KEY)]
    lateinit var missedCallChannelId: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            TAG,
            "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId"
        )
        intent?.getBooleanExtra(Constants.FOR_CALL_LOG, false)?.let {
            if (it)
                fetchLastCallLog()
        }

        intent?.getBooleanExtra(Constants.FOR_REMOVE_NOTIFICATION, false)?.let {
            if (it) {
                contactsViewModel.getNotificationId(intent.getStringExtra(Constants.CONTACT_NUMBER))
                    ?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.flatMapCompletable { notificationId ->
                        Log.d(
                            TAG,
                            "onStartCommand: getNotificationId() called with notificationId = $notificationId"
                        )
                        callThroughIntent(notificationId.number!!)
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(notificationId.notificationId!!)
                        contactsViewModel.deleteNotificationIds(intent.getStringExtra(Constants.CONTACT_NUMBER))
                            ?.subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                    }
                    ?.subscribe({
                        Log.d(TAG, "onStartCommand: notification Id delete successful")
                        stopSelf()
                    }, { e ->
                        Log.e(
                            TAG,
                            "onStartCommand: error while deleting notification Id${e.message}",
                            e
                        )
                    })?.let { d -> disposables.add(d) }
                return START_NOT_STICKY
            }
        }

        intent?.getStringExtra(Constants.CONTACT_NUMBER)?.let { number ->
            contactsViewModel.deleteNotificationIds(number)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    Log.d(TAG, "onStartCommand: notification Id delete successful")
                    stopSelf()
                }, {
                    Log.e(
                        TAG,
                        "onStartCommand: error while deleting notification Id${it.message}",
                        it
                    )
                })?.let { disposables.add(it) }
        }
        return START_NOT_STICKY
    }

    private fun fetchLastCallLog() {
        Log.d(TAG, "fetchLastCallLog() called")
        var callLogDetails = HashMap<String, String>()
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Completable.timer(2, TimeUnit.SECONDS, Schedulers.io())
                .subscribe {
                    run {
                        ContentResolverForCallLog.setCursor(contentResolver)
                            .andThen(contactsViewModel.callLogsCount)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap {
                                Log.d(
                                    TAG,
                                    "fetchLastCallLog: call log count from database = ${it.count}"
                                )
                                Single.create { emitter: SingleEmitter<Int> ->
                                    ContentResolverForCallLog.getCellPhoneCallLogsCount()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ cellPhoneCallLogsCount ->
                                            Log.d(
                                                TAG,
                                                "fetchLastCallLog: getCellPhoneCallLogsCount = $cellPhoneCallLogsCount"
                                            )
                                            if (it.count != cellPhoneCallLogsCount)
                                                emitter.onSuccess(cellPhoneCallLogsCount)
                                            else
                                                emitter.onError(Exception("Call logs count matched."))
                                        }, { e ->
                                            Log.e(
                                                TAG,
                                                "fetchLastCallLog: error while getCellPhoneCallLogsCount = ${e.message}",
                                                e
                                            )
                                            emitter.onError(e)
                                        })
                                }
                            }
                            .flatMapCompletable { cellPhoneCallLogsCount ->
                                Log.d(
                                    TAG,
                                    "fetchLastCallLog() called with: cellPhoneCallLogsCount = $cellPhoneCallLogsCount for insertOrUpdateCallLogsCount"
                                )
                                contactsViewModel.insertOrUpdateCallLogsCount(
                                    CallLogsCount(
                                        cellPhoneCallLogsCount
                                    )
                                )
                                    ?.subscribeOn(Schedulers.io())
                                    ?.observeOn(AndroidSchedulers.mainThread())
                            }
                            .andThen(ContentResolverForCallLog.getCallLogs())
                            .subscribeOn(Schedulers.io())
                            ?.observeOn(AndroidSchedulers.mainThread())
                            ?.flatMapMaybe {
                                Log.d(TAG, "fetchLastCallLog: call log details = $it")
                                callLogDetails = it as HashMap<String, String>
                                contactsViewModel.getContact(
                                    getCountryCode(it[Constants.CONTACT_NUMBER]),
                                    getTenDigitNumber(it[Constants.CONTACT_NUMBER])
                                )
                                    ?.subscribeOn(Schedulers.io())
                                    ?.observeOn(AndroidSchedulers.mainThread())
                            }
                            ?.subscribe({
                                Log.d(TAG, "fetchLastCallLog: received contact details = $it")
                                if (callLogDetails[Constants.CALL_TYPE]?.toInt() == CallLog.Calls.MISSED_TYPE)
                                    showNotification(
                                        it,
                                        getTenDigitNumber(callLogDetails[Constants.CONTACT_NUMBER])
                                    )
                                else
                                    showDialogCallLog(
                                        it.contact?.name,
                                        callLogDetails[Constants.CONTACT_NUMBER],
                                        callLogDetails[Constants.CALL_DATE],
                                        callLogDetails[Constants.CALL_DURATION]
                                    )
                            }, {
                                Log.e(TAG, "fetchLastCallLog: error = ${it.message}", it)
                                stopSelf()
                            })
                    }
                }.let { disposables.add(it) }
        }
    }

    private fun showNotification(
        contactWithContactNumbers: ContactWithContactNumbers,
        number: String?
    ) {
        Log.d(
            TAG,
            "showNotification() called with: contactWithContactNumbers = $contactWithContactNumbers, number = $number"
        )
        val group = "$packageName.MISSED_CALL"
        val notificationId = getNotificationId(number)
        val contactNumberIndex = indexOfContactNumber(
            contactWithContactNumbers,
            number
        )
        val numberWithCC =
            "${
                contactWithContactNumbers.contactNumbers?.get(
                    contactNumberIndex
                )?.countryCode
            } ${
                contactWithContactNumbers.contactNumbers?.get(
                    contactNumberIndex
                )?.number
            }"

        contactsViewModel.insertNotificationId(NotificationId(notificationId, numberWithCC))
            ?.andThen(contactsViewModel.getMissedCount(numberWithCC))
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d(TAG, "showNotification() called with missed call count = $it")
                val notificationGroup =
                    NotificationCompat.Builder(
                        this,
                        missedCallChannelId
                    )
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setStyle(NotificationCompat.InboxStyle().setSummaryText("Missed calls"))
                        .setGroup(group)
                        .setGroupSummary(true)
                        .build()

                val notification =
                    NotificationCompat.Builder(
                        this,
                        missedCallChannelId
                    )
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle(
                            "You have $it missed call(s)."
                        )
                        .setContentText(
                            "${contactWithContactNumbers.contact?.name}($numberWithCC)"
                        )
                        .setGroup(group)
                        .setContentIntent(getDisplayContactsPendingIntent())
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setAutoCancel(true)
                        .setDeleteIntent(getDeletePendingIntent(numberWithCC))
                        .addAction(0, "Call", getCallPendingIntent(numberWithCC))
                        .build()

                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, notificationGroup)
                notificationManager.notify(notificationId, notification)
            }, {
                Log.e(TAG, "showNotification: error while getting missed count = ${it.message}", it)
            })?.let { disposables.add(it) }
    }

    private fun showDialogCallLog(
        name: String?,
        number: String?,
        callTime: String?,
        callDuration: String?
    ) {
        Log.d(
            TAG,
            "showDialogCallLog() called with: name = $name, number = $number, callTime = $callTime, callDuration = $callDuration"
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && Settings.canDrawOverlays(
                this
            )
        ) {
            val dialog = Dialog(this)
            val callLogScreenBinding = CallLogScreenBinding.inflate(dialog.layoutInflater)
            dialog.setContentView(callLogScreenBinding.root)
            callLogScreenBinding.textViewCallLogs.text = resources.getString(
                R.string.call_log,
                name,
                number,
                callTime,
                callDuration
            )
            callLogScreenBinding.buttonOk.setTag(R.string.dialog_key, dialog)
            callLogScreenBinding.buttonOk.setOnClickListener {
                Log.d(TAG, "showDialogCallLog() buttonOk pressed called")
                val d = (it as Button).getTag(R.string.dialog_key) as Dialog
                if (d.isShowing)
                    d.dismiss()
            }
            dialog.setOnDismissListener {
                Log.d(TAG, "showDialogCallLog() dialog dismiss called")
                stopSelf()
            }
            dialog.window?.decorView?.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            dialog.window?.attributes = layoutParams
            dialog.window?.setWindowAnimations(R.style.DialogTheme)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setGravity(Gravity.BOTTOM)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) dialog.window?.setType(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            )
            dialog.show()
        }
    }

    private fun getDisplayContactsPendingIntent(): PendingIntent {
        Log.d(TAG, "getDisplayContactsPendingIntent() called")
        val intent = Intent(applicationContext, DisplayContactsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getCallPendingIntent(number: String): PendingIntent {
        Log.d(TAG, "getCallPendingIntent() called")
        val intent = Intent(applicationContext, ShowCallLogService::class.java)
        intent.putExtra(Constants.FOR_REMOVE_NOTIFICATION, true)
        intent.putExtra(Constants.CONTACT_NUMBER, number)
        return PendingIntent.getService(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getDeletePendingIntent(number: String): PendingIntent {
        Log.d(TAG, "getDeletePendingIntent() called with: number = $number")
        val intent = Intent(applicationContext, ShowCallLogService::class.java)
        intent.putExtra(Constants.CONTACT_NUMBER, number)
        return PendingIntent.getService(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun indexOfContactNumber(
        contactWithContactNumbers: ContactWithContactNumbers,
        number: String?
    ): Int {
        Log.d(TAG, "indexOf() called with: contactWithContactNumbers = $contactWithContactNumbers")
        contactWithContactNumbers.contactNumbers?.let {
            for (contactNumber in it) {
                if (contactNumber.number == number)
                    return it.indexOf(contactNumber)
            }
        }
        return 0
    }

    private fun getTenDigitNumber(number: String?): String {
        Log.d(TAG, "getTenDigitNumber() called with: number = $number")
        var result = ""
        number?.let {
            if (number.length >= 10)
                result = number.substring(number.length - 10)
        }
        return result
    }

    private fun getCountryCode(number: String?): String {
        Log.d(TAG, "getCountryCode() called with: number = $number")
        var result = ""
        number?.let {
            if (number.length >= 10)
                result = number.substring(0, number.length - 10)
        }
        return result
    }

    private fun getNotificationId(number: String?): Int {
        Log.d(TAG, "getNotificationId() called with: number = $number")
        var id = number?.substring(3)?.toLong()
        id = (id?.div(1000)?.plus(id.rem(1000)))?.rem(1000)
        return id.toString().toInt()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d(TAG, "onBind() called with: p0 = $p0")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        disposables.dispose()
    }

    companion object {
        private const val TAG = "ShowCallLogService"
    }
}