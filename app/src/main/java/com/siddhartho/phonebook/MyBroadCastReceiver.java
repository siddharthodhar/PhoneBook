package com.siddhartho.phonebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Toast.makeText(context, "Call ended!", Toast.LENGTH_LONG).show();
            Intent i = new Intent();
            i.setClassName("com.siddhartho.phonebook", "com.siddhartho.phonebook.CallLogScreen");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
