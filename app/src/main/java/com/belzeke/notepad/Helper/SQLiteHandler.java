package com.belzeke.notepad.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Models.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by marko on 29.4.2015.
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "android_notes";

    //table names
    private static final String TABLE_LOGIN = "android_login";
    private static final String TABLE_NOTES = "android_notes";
    private static final String TABLE_HASHTAG = "android_hashtag";

    //shared fields
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    //Login Table
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";

    //hashtag Table
    public static final String KEY_HASHTAG = "hashtag";

    //Note Table
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOTE_TYPE = "note_type";
    private static final String KEY_NAME = "name";
    private static final String KEY_TEXT = "note_text";
    private static final String KEY_UPLOADED = "uploaded";

    //Indexs
    public static final int INDEX_KEY_ID = 0;
    public static final int INDEX_KEY_CREATED_AT = 1;
    public static final int INDEX_KEY_USER_ID = 2;
    public static final int INDEX_KEY_NOTE_TYPE = 3;
    public static final int INDEX_KEY_NAME = 4;
    public static final int INDEX_KEY_TEXT = 5;
    public static final int INDEX_KEY_UPLOADED = 6;



    public SQLiteHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_HASHTAG_TABLE = "CREATE TABLE " + TABLE_HASHTAG + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_HASHTAG + " TEXT)";
        db.execSQL(CREATE_HASHTAG_TABLE);



        String CREATE_NOTE_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_NOTE_TYPE + " INTEGER,"
                + KEY_HASHTAG + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_TEXT + " TEXT,"
                + KEY_UPLOADED + " INTEGER" + ")";
        db.execSQL(CREATE_NOTE_TABLE);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public void addUser(String email, String uid, String created_at){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email);
        values.put(KEY_UID, uid);
        values.put(KEY_CREATED_AT, created_at);

        long id = db.insert(TABLE_LOGIN, null, values);
        db.close();

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public long addNote(Integer noteType, String name, @Nullable String text) {
        SQLiteDatabase db = this.getWritableDatabase();

      //  if(hashTag == null) hashTag = "noHasTag";

        String now = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("Si")).format(new Date());

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, AppConfig.userId);
        values.put(KEY_NOTE_TYPE, noteType);
        values.put(KEY_CREATED_AT, now);
        values.put(KEY_NAME, name);
        values.put(KEY_TEXT, text);
        values.put(KEY_UPLOADED, 0);

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();

        Log.d(TAG, "New note inserted into sqlite: " + id);
        Log.d(TAG, "VALUES: " + values.toString());
        return id;
    }

    public void noteUploaded(long id){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UPLOADED, 1);

        db.update(TABLE_NOTES, values, KEY_ID + " = " + id, null);
        db.close();
        Log.d(TAG, "Note ("+id+") uploaded");
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        String sql = "SELECT * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("email", cursor.getString(1));
            user.put("uid", cursor.getString(2));
            user.put("created_at", cursor.getString(3));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public List<Note> getItems(int type){
        List<Note> results = new ArrayList<>();

        String sql = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_NOTE_TYPE + " = " + type;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                Note note = new Note();
                note.setId(cursor.getLong(INDEX_KEY_ID));
                note.setNoteName(cursor.getString(INDEX_KEY_NAME));
                note.setCreated(cursor.getString(INDEX_KEY_CREATED_AT));
                note.setNoteText(cursor.getString(INDEX_KEY_NOTE_TYPE));
                note.setNoteType(type);
                note.setUploaded(cursor.getInt(INDEX_KEY_UPLOADED) > 0);
                results.add(note);
            }
        }else{
            return null;
        }
        cursor.close();
        db.close();

        return results;
    }

    public  int getRowCount(){
        String sql = "SELECT * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        return rowCount;
    }

    public void deleteUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGIN, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
