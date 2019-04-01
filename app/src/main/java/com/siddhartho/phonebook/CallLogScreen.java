package com.siddhartho.phonebook;

import android.Manifest;
import android.content.Intent;
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

        Button home = (Button) findViewById(R.id.btn_home);

        name = (TextView) findViewById(R.id.tv_name);
        number = (TextView) findViewById(R.id.tv_number);
        dateTime = (TextView) findViewById(R.id.tv_callst_time);
        duration = (TextView) findViewById(R.id.tv_call_dur);

        if (ContextCompat.checkSelfPermission(CallLogScreen.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(CallLogScreen.this, new String[] {Manifest.permission.READ_CALL_LOG}, 1);
        else {
            Cursor cursor = CallLogScreen.this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null,null, null);
            Cursor curdb;

            while (cursor.moveToNext()) {
                NUMBER = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

                dateTime.setText(new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)))).toString());
                duration.setText(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
            }
            cursor.close();

            curdb = dataBasePB.getContact(NUMBER);
            if (curdb.moveToFirst()){
                    name.setText(curdb.getString(0));
                    number.setText(curdb.getString(1));
            }
        }

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CallLogScreen.this, MainActivity.class);

                startActivity(intent);
                CallLogScreen.this.finish();
            }
        });
    }
}
