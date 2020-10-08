package com.siddhartho.phonebook.broadcastreceiver

import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.siddhartho.phonebook.services.showcalllog.ShowCallLogService
import com.siddhartho.phonebook.utils.Constants
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsBroadcastReceiver @Inject constructor() : DaggerBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive() called with: context = $context, intent = $intent")
        if (intent.action.equals("android.intent.action.PHONE_STATE")) {
            val state: String? = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            onCallStateChanged(context, state)
        }
    }

    private fun onCallStateChanged(context: Context, state: String?) {
        Log.d(TAG, "onCallStateChanged() called with: context = $context, state = $state")
        if (!isCallOffHook && state == TelephonyManager.EXTRA_STATE_OFFHOOK)
            isCallOffHook = true
        else if (isCallOffHook && state == TelephonyManager.EXTRA_STATE_RINGING)
            isWaiting = true
        else if (!isRinging && state == TelephonyManager.EXTRA_STATE_RINGING)
            isRinging = true

        Log.d(TAG, "onCallStateChanged: $isCallOffHook $isRinging $state")

        when {
            (isCallOffHook || isRinging) && state == TelephonyManager.EXTRA_STATE_IDLE -> {
                isCallOffHook = false
                isRinging = false
                startCallLogService(context)
            }
            isWaiting && state == TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                isWaiting = false
                startCallLogService(context)
            }
        }
    }

    private fun startCallLogService(context: Context) {
        Log.d(TAG, "startCallLogService() called with: context = $context")
        val intent = Intent()
        intent.setClassName(context.packageName, ShowCallLogService::class.java.name)
        intent.putExtra(Constants.FOR_CALL_LOG, true)
        context.startService(intent)
    }

    companion object {
        private const val TAG = "MyContactsBroadcast"
        private var isRinging = false
        private var isWaiting = false
        private var isCallOffHook = false
    }
}