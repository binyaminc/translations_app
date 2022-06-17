package com.example.translations_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


public class mySQLiteDatabase extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String COL1 = "email";
    private static final String COL2 = "password";

    public mySQLiteDatabase(Context context) {
        super(context, "Login.db", null, 1);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("Create table " +
            "last_user(" +
            "user_id integer primary key)");

        sqLiteDatabase.execSQL("Create table " +
                "users(" +
                "id integer primary key autoincrement, " +
                "email text unique not null, " +
                "password text, " +
                "userType text)");
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists last_user");
        sqLiteDatabase.execSQL("drop table if exists users");
        onCreate(sqLiteDatabase); //needed? from previous clip
    }

    public boolean setLastUserId(String email) {

        int user_id = getIdOfUser(email);

        android.database.sqlite.SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id", user_id);

        long result = sqLiteDatabase.insert("last_user", null, contentValues);

        //if data was inserted incorrectly it will return -1
        if(result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public int getIdOfUser(String email) {
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from users where email =?", new String[]{email});
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public boolean addUser(String email, String password, String userType) {
        android.database.sqlite.SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValuesUser = new ContentValues();
        contentValuesUser.put("email", email);
        contentValuesUser.put("password", password);
        contentValuesUser.put("userType", userType);

        Log.d(TAG, "addUser: Adding email address " + email + " and password " + password + " to " + "users");

        long result = sqLiteDatabase.insert("users", null, contentValuesUser);

        //put the value in the table "last_user"
        ContentValues contentValuesLastUser = new ContentValues();
        contentValuesLastUser.put("user_id", getIdOfUser(email));
        sqLiteDatabase.insert("last_user", null, contentValuesLastUser);

        //create new table of lists of this user

        String tableName = "tableListsOf" + getIdOfUser(email);

        sqLiteDatabase.execSQL("Create table " +
                tableName + "(" +
                "list_id integer primary key autoincrement, " +
                "list_name text)");

        //if data was inserted incorrectly it will return -1
        if(result == -1) {
            return false;
        } else {
            return true;
        }

    }
    public int getLastUserIfExist() {
        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("last_user", null, null, null, null, null, null, null);
        if(cursor == null)//there is mo content- no user registrated
            return -1;
        else{
            if(cursor.moveToFirst()) {
                int lastUserId = cursor.getInt(0);
                cursor.close();
                return lastUserId;
            }
            else
                return -1;
        }
    }

    public User getUserIfExist(int lastUserId) {

        android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", null, null, null, null, null, null, null);
        if(cursor == null)//there is mo content- no user registrated
            return null;
        else{
            if(cursor.move(lastUserId)) {
                User user = new User(Integer.parseInt(cursor.getString(0)),
                                     cursor.getString(1),
                                     cursor.getString(2),
                                     cursor.getString(3));
                cursor.close();
                return user;
            }
            else
                return null;
        }

    }

    public ArrayList<String> getListOfLists(User user) {
        String tableName = "tableListsOf" + user.getId();

        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();



        db = this.getReadableDatabase();

        Cursor cursor = db.query(tableName, null, null, null, null, null, null, null);
        if(cursor == null) {
            cursor.close();
            return null;
        }
        else{
            ArrayList<String> names = new ArrayList<>();
            while (cursor.moveToNext()) {
                names.add(cursor.getString(1));
            }
            cursor.close();
            return names;
        }
    }
}
