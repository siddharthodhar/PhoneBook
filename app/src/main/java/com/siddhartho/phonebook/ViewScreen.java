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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewScreen extends AppCompatActivity {
    private static final String TAG = "ViewScreen";

    ListView listView;

    boolean isDeleted;

    ContactDataBasePB dataBasePB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_main);
        Log.d(TAG, "onCreate: success");

        listView = (ListView) findViewById(R.id.listViewId);

        dataBasePB = new ContactDataBasePB(this);

        final ArrayList<String> namearr = new ArrayList<String>();
        final ArrayList<String> numarr = new ArrayList<String>();

        Cursor cursor = dataBasePB.getAllContacts();
        if (cursor.moveToFirst()) {
            do {
                namearr.add(cursor.getString(0));
                numarr.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ViewScreen.this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG}, 1);
                else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + v.getTag().toString()));
                    startActivity(intent);
                }
            }
        };

        final View.OnClickListener sms_listener = (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ViewScreen.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ViewScreen.this, new String[]{Manifest.permission.SEND_SMS}, 1);
                } else {
                    final String number = v.getTag().toString();
                    smsDialog(number);
                }
            }
        });

        MyAdapterClass myAdapterClass = new MyAdapterClass(namearr, numarr, listener, sms_listener);

        listView.setAdapter(myAdapterClass);
        listView.setLongClickable(true);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return delAlert(numarr.get(position), dataBasePB);
            }
        });
    }

    private void sendSms(String number, String message) {
        try {
            PendingIntent sentPi = PendingIntent.getBroadcast(ViewScreen.this, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredPi = PendingIntent.getBroadcast(ViewScreen.this, 0, new Intent("SMS_DELIVERED"), 0);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, sentPi, deliveredPi);
            Toast.makeText(ViewScreen.this, "Message Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ViewScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void smsDialog(final String number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewScreen.this);
        builder.setTitle("Type your message:");

        final EditText message = new EditText(ViewScreen.this);
        message.setInputType(InputType.TYPE_CLASS_TEXT);
        message.setHint("Type your message here");

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

    private boolean delAlert(final String number, final ContactDataBasePB dataBasePB) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewScreen.this);
        builder.setTitle("Warning!");

        final TextView message = new TextView(ViewScreen.this);
        message.setTextColor(Color.RED);
        message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        message.setTypeface(null, Typeface.BOLD);
        message.setPadding(40, 35, 0, 0);
        message.setTextSize(20);
        message.append("Are you sure you want to delete the contact?");

        builder.setView(message);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dataBasePB.deleteContact(number);
                isDeleted = true;
                recreate();
                Toast.makeText(ViewScreen.this, "Deleted successfully!", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDeleted = false;
            }
        });
        builder.show();
        return isDeleted;
    }

    class MyAdapterClass extends BaseAdapter {

        ArrayList<String> namearr = new ArrayList<String>();
        ArrayList<String> numarr = new ArrayList<String>();
        View.OnClickListener listener;
        View.OnClickListener sms_listener;

        public MyAdapterClass(ArrayList<String> namearr, ArrayList<String> numarr, View.OnClickListener listener, View.OnClickListener sms_listener) {
            this.namearr = namearr;
            this.numarr = numarr;
            this.listener = listener;
            this.sms_listener = sms_listener;
        }

        @Override
        public int getCount() {
            return namearr.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.activity_custom_item, null);

            TextView name = (TextView) convertView.findViewById(R.id.textView_name);
            TextView number = (TextView) convertView.findViewById(R.id.textView_number);
            Button call = (Button) convertView.findViewById(R.id.btn_call);
            Button sendsms = (Button) convertView.findViewById(R.id.btn_sms);

            name.setText(namearr.get(position));
            number.setText(numarr.get(position));

            call.setTag(numarr.get(position));
            call.setOnClickListener(listener);

            sendsms.setTag(numarr.get(position));
            sendsms.setOnClickListener(sms_listener);

            return convertView;
        }
    }
}
