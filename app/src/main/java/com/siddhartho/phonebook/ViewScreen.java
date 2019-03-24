package com.siddhartho.phonebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.security.Permission;
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

        dataBasePB = new ContactDataBasePB(this);

        ArrayList<String> namearr = new ArrayList<String>();
        ArrayList<String> numarr = new ArrayList<String>();

        Cursor cursor = dataBasePB.getAllContacts();
        if (cursor.moveToFirst()) {
            do {
                namearr.add(cursor.getString(0));
                numarr.add(cursor.getString(1));
            }while (cursor.moveToNext());
        }

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

            textViewName.setText(namearr.get(i));
            textViewNum.setText(numarr.get(i));

            tableRow.addView(textViewName);
            tableRow.addView(textViewNum);
            tableRow.addView(btn_call);

            table.addView(tableRow);
        }
    }
}
