package com.siddhartho.phonebook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class CallLogScreen extends AppCompatActivity {
    private static final String TAG = "CallLogScreen";

    TextView name;
    TextView nameLabel;
    TextView number;
    TextView dateTime;
    TextView duration;

    ContactDataBasePB dataBasePB;

    String NUMBER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log_screen);
        Log.d(TAG, "onCreate: success");

        dataBasePB = new ContactDataBasePB(this);

        Button btn_ok = (Button) findViewById(R.id.btn_ok);

        name = (TextView) findViewById(R.id.tv_name);
        nameLabel = (TextView) findViewById(R.id.tv_label_name);
        number = (TextView) findViewById(R.id.tv_number);
        dateTime = (TextView) findViewById(R.id.tv_callst_time);
        duration = (TextView) findViewById(R.id.tv_call_dur);

        if (ContextCompat.checkSelfPermission(CallLogScreen.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(CallLogScreen.this, new String[]{Manifest.permission.READ_CALL_LOG}, 1);
        else {
            Cursor cursor = CallLogScreen.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            Cursor curdb;

            while (cursor.moveToNext()) {
                NUMBER = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

                dateTime.setText(new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)))).toString().replace("GMT+05:30", ""));
                duration.setText(getDurationFormat(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))));
            }
            cursor.close();

            curdb = dataBasePB.getContact(NUMBER);
            if (curdb.moveToFirst()) {
                if (curdb.getString(0).equals("")) {
                    nameLabel.setText("");
                } else {
                    name.setText(curdb.getString(0));
                }
            }
            number.setText(NUMBER);
        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLogScreen.this.finish();
            }
        });
    }

    private String getDurationFormat(String duration) {
        String time = "";
        int hr;
        String hr_s;
        int min;
        String min_s;
        int sec;
        String sec_s;

        int dur = Integer.parseInt(duration);
        hr = dur / 3600;
        min = (dur % 3600) / 60;
        sec = (dur % 3600) % 60;

        if (hr / 10 == 0)
            hr_s = "0" + String.valueOf(hr);
        else
            hr_s = String.valueOf(hr);

        if (min / 10 == 0)
            min_s = "0" + String.valueOf(min);
        else
            min_s = String.valueOf(min);

        if (sec / 10 == 0)
            sec_s = "0" + String.valueOf(sec);
        else
            sec_s = String.valueOf(sec);

        time = hr_s + ":" + min_s + ":" + sec_s;
        return time;
    }
}
