package com.siddhartho.phonebook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDataBasePB extends SQLiteOpenHelper {

    private static String dbName = "MyContacts.db";
    private static String tableName = "PersonalContacts";
    private static String columnName = "Name";
    private static String columnNum = "Number";

    private SQLiteDatabase pbdb;

    public ContactDataBasePB(Context context) {
        super(context, dbName,null,1);

        pbdb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + tableName + "' ('" + columnName + "' TEXT, '" + columnNum + "' TEXT PRIMARY KEY);");
    }

    public void insertContact(String name, String number) {
        pbdb.execSQL("INSERT INTO '" + tableName + "' VALUES ('" + name + "', '" + number + "');");
    }

    public Cursor getAllContacts() {
        Cursor cursor = pbdb.rawQuery("SELECT * FROM '" + tableName + "';", null);

        return cursor;
    }

    public Cursor getContact(String number) {
        Cursor cursor = pbdb.rawQuery("SELECT * FROM '" + tableName + "' WHERE " + columnNum + " = '" + number + "';", null);

        return cursor;
    }

    public void deleteContact(String number) {
        pbdb.execSQL("DELETE FROM '" + tableName + "' WHERE " + columnNum + " = '" + number + "';");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
