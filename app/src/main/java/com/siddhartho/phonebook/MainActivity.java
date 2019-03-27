package com.siddhartho.phonebook;

import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    EditText name;
    EditText number;

    ContactDataBasePB dataBasePB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: success");

        dataBasePB = new ContactDataBasePB(this);

        name = (EditText) findViewById(R.id.editText_name);
        number = (EditText) findViewById(R.id.editText_number);

        Button submit = (Button) findViewById(R.id.btn_submit);
        Button view = (Button) findViewById(R.id.btn_view);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (!name.getText().toString().isEmpty() && number.getText().toString().length() == 10 && number.getText().toString().matches("[0-9]+")) {
                        dataBasePB.insertContact(name.getText().toString(), number.getText().toString());
                        name.setText("");
                        number.setText("");
                        Toast.makeText(MainActivity.this, "Saved successfully!", Toast.LENGTH_LONG).show();
                    } else if(name.getText().toString().isEmpty())
                        Toast.makeText(MainActivity.this, "Name cannot be empty!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this, "Invalid number!", Toast.LENGTH_LONG).show();
                } catch (SQLiteConstraintException e){
                    Toast.makeText(MainActivity.this, "Number already saved!", Toast.LENGTH_LONG).show();
                }
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewScreen.class);

                startActivity(intent);
            }
        });
    }
}
