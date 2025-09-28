package com.nhom4.aichatbot.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.Models.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chats.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CHATS = "chats";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_CHAR_USER = "character_user";
    private static final String KEY_CHAR_AI = "character_ai";
    private static final String KEY_LAST_UPDATED = "last_updated";

    private Gson gson;

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        gson = new Gson();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CHATS_TABLE = "CREATE TABLE " + TABLE_CHATS + "(" +
                KEY_ID + " TEXT PRIMARY KEY," +
                KEY_NAME + " TEXT," +
                KEY_DESCRIPTION + " TEXT," +
                KEY_MESSAGES + " TEXT," +
                KEY_CHAR_USER + " TEXT," +
                KEY_CHAR_AI + " TEXT," +
                KEY_LAST_UPDATED + " INTEGER" + ")";
        db.execSQL(CREATE_CHATS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATS);
        onCreate(db);
    }

    public void addChat(Chat chat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, chat.getId());
        values.put(KEY_NAME, chat.getName());
        values.put(KEY_DESCRIPTION, chat.getDescription());
        values.put(KEY_LAST_UPDATED, System.currentTimeMillis());

        // Serialize complex objects to JSON
        values.put(KEY_MESSAGES, gson.toJson(chat.getMessages()));
        values.put(KEY_CHAR_USER, gson.toJson(chat.getCharacterUser()));
        values.put(KEY_CHAR_AI, gson.toJson(chat.getCharacterAI()));

        db.insert(TABLE_CHATS, null, values);
        db.close();
    }

    public List<Chat> getAllChats() {
        List<Chat> chatList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHATS + " ORDER BY " + KEY_LAST_UPDATED + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                chatList.add(cursorToChat(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return chatList;
    }

    public void updateChat(Chat chat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, chat.getName());
        values.put(KEY_DESCRIPTION, chat.getDescription());
        values.put(KEY_LAST_UPDATED, System.currentTimeMillis());

        values.put(KEY_MESSAGES, gson.toJson(chat.getMessages()));
        values.put(KEY_CHAR_USER, gson.toJson(chat.getCharacterUser()));
        values.put(KEY_CHAR_AI, gson.toJson(chat.getCharacterAI()));

        db.update(TABLE_CHATS, values, KEY_ID + " = ?", new String[]{chat.getId()});
        db.close();
    }

    public void deleteChat(Chat chat) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHATS, KEY_ID + " = ?", new String[]{chat.getId()});
        db.close();
    }

    private Chat cursorToChat(Cursor cursor) {
        Chat chat = new Chat();
        chat.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
        chat.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        chat.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));

        // Deserialize JSON strings back to objects
        String messagesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGES));
        String charUserJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAR_USER));
        String charAiJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAR_AI));

        Type messageListType = new TypeToken<ArrayList<Message>>(){}.getType();
        List<Message> messages = gson.fromJson(messagesJson, messageListType);
        chat.setMessages(messages != null ? messages : new ArrayList<>());
        chat.setCharacterUser(gson.fromJson(charUserJson, Character.class));
        chat.setCharacterAI(gson.fromJson(charAiJson, Character.class));

        return chat;
    }
}
