package com.example.aichatassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabase extends SQLiteOpenHelper {

    public ChatDatabase(Context context) {
        super(context, "chat_history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat(id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT, isUser INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS chat");
        onCreate(db);
    }

    // Insert message into database
    public void insertMessage(String message, boolean isUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("message", message);
        values.put("isUser", isUser ? 1 : 0);
        db.insert("chat", null, values);
        db.close();
    }

    // Fetch all saved messages
    public Cursor getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM chat", null);
    }
}
