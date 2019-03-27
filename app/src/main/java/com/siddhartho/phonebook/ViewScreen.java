package com.siddhartho.phonebook;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewScreen extends AppCompatActivity {
    private static final String TAG = "ViewScreen";

    TableLayout table;

    ContactDataBasePB dataBasePB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_screen);
        Log.d(TAG, "onCreate: success");

        table = (TableLayout) findViewById(R.id.table_view);
        Button deletebtn = (Button) findViewById(R.id.btn_delete);

        dataBasePB = new ContactDataBasePB(this);

        ArrayList<String> namearr = new ArrayList<String>();
        ArrayList<String> numarr = new ArrayList<String>();
        final ArrayList<String> delnumarr = new ArrayList<>();

        Cursor cursor = dataBasePB.getAllContacts();
        if (cursor.moveToFirst()) {
            do {
                namearr.add(cursor.getString(0));
                numarr.add(cursor.getString(1));
            }while (cursor.moveToNext());
        }

        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < delnumarr.size(); i++){
                    dataBasePB.deleteContact(delnumarr.get(i));
                }
                recreate();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ViewScreen.this, new String[] {Manifest.permission.CALL_PHONE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG}, 1);
                else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + v.getTag().toString()));
                    startActivity(intent);
                }
            }
        };

        View.OnClickListener sms_listener = (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ViewScreen.this, new String[]{Manifest.permission.SEND_SMS}, 1);
                } else {
                    final String number = v.getTag().toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewScreen.this);
                    builder.setTitle("Type your message:");

                    final EditText message = new EditText(ViewScreen.this);
                    message.setInputType(InputType.TYPE_CLASS_TEXT);

                    builder.setView(message);

                    builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendSms(number, message.getText().toString());
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });

        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    delnumarr.add(buttonView.getTag().toString());
            }
        };

        for(int i = 0; i < namearr.size(); i++){
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView textViewName = new TextView(this);
            textViewName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));
            textViewName.setGravity(Gravity.CENTER);
            textViewName.setTextColor(Color.BLACK);
            textViewName.setTypeface(null, Typeface.BOLD_ITALIC);
            textViewName.setPadding(15,5,15,5);

            TextView textViewNum = new TextView(this);
            textViewNum.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT));
            textViewNum.setGravity(Gravity.CENTER);
            textViewNum.setTextColor(Color.BLACK);
            textViewNum.setTypeface(null, Typeface.BOLD_ITALIC);
            textViewNum.setPadding(15,5,15,5);

            Button btn_call = new Button(this);
            btn_call.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
            btn_call.setTag(numarr.get(i));
            btn_call.setText("CALL");
            btn_call.setPadding(15,5,15,5);
            btn_call.setBackgroundColor(Color.LTGRAY);
            btn_call.setTextColor(Color.BLACK);
            btn_call.setTypeface(null, Typeface.BOLD);
            btn_call.setOnClickListener(listener);

            Button btn_sms = new Button(this);
            btn_sms.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
            btn_sms.setTag(numarr.get(i));
            btn_sms.setText("SEND SMS");
            btn_sms.setPadding(15,5,15,5);
            btn_sms.setBackgroundColor(Color.LTGRAY);
            btn_sms.setTextColor(Color.BLACK);
            btn_sms.setTypeface(null, Typeface.BOLD);
            btn_sms.setOnClickListener(sms_listener);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setChecked(false);
            checkBox.setTag(numarr.get(i));
            checkBox.setOnCheckedChangeListener(checkedChangeListener);

            textViewName.setText(namearr.get(i));
            textViewNum.setText(numarr.get(i));

            tableRow.addView(checkBox);
            tableRow.addView(textViewName);
            tableRow.addView(textViewNum);
            tableRow.addView(btn_call);
            tableRow.addView(btn_sms);

            table.addView(tableRow);
        }
    }

    private void sendSms(String number, String message){
        try{
            PendingIntent sentPi = PendingIntent.getBroadcast(ViewScreen.this, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredPi = PendingIntent.getBroadcast(ViewScreen.this, 0, new Intent("SMS_DELIVERED"), 0);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, sentPi, deliveredPi);
            Toast.makeText(ViewScreen.this, "Message Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(ViewScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
