package com.nhom4.aichatbot.Database;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.Models.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatDbHelper {

    private static final String DATABASE_NAME = "chats.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CHATS = "chats";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_CHAR_USER = "character_user";
    private static final String KEY_CHAR_AI = "character_ai";
    private static final String KEY_DATE_CREATE = "date_create";
    private static final String KEY_DATE_UPDATE = "date_update";

    private final DataBase db;
    private final Gson gson;

    public ChatDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        String CREATE_CHATS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CHATS + "(" +
                KEY_ID + " TEXT PRIMARY KEY," +
                KEY_NAME + " TEXT," +
                KEY_DESCRIPTION + " TEXT," +
                KEY_MESSAGES + " TEXT," +
                KEY_CHAR_USER + " TEXT," +
                KEY_CHAR_AI + " TEXT," +
                KEY_DATE_CREATE + " TEXT," +
                KEY_DATE_UPDATE + " TEXT" + ")";
        db.querrydata(CREATE_CHATS_TABLE);
        gson = new Gson();
    }

    public void addChat(Chat chat) {
        String sql = "INSERT INTO " + TABLE_CHATS + " VALUES (" +
                "'" + chat.getId() + "'," +
                "'" + chat.getName().replace("'", "''") + "'," +
                "'" + chat.getDescription().replace("'", "''") + "'," +
                "'" + gson.toJson(chat.getMessages()).replace("'", "''") + "'," +
                "'" + gson.toJson(chat.getCharacterUser()).replace("'", "''") + "'," +
                "'" + gson.toJson(chat.getCharacterAI()).replace("'", "''") + "'," +
                "'" + chat.getDateCreate() + "'," +
                "'" + chat.getDateUpdate() + "'" +
                ")";
        db.querrydata(sql);
    }

    public List<Chat> getAllChats() {
        List<Chat> chatList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHATS + " ORDER BY " + KEY_DATE_UPDATE + " DESC";
        Cursor cursor = db.getdata(selectQuery);

        if (cursor.moveToFirst()) {
            do {
                chatList.add(cursorToChat(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chatList;
    }

    public void updateChat(Chat chat) {
        String sql = "UPDATE " + TABLE_CHATS + " SET " +
                KEY_NAME + " = '" + chat.getName().replace("'", "''") + "'," +
                KEY_DESCRIPTION + " = '" + chat.getDescription().replace("'", "''") + "'," +
                KEY_MESSAGES + " = '" + gson.toJson(chat.getMessages()).replace("'", "''") + "'," +
                KEY_CHAR_USER + " = '" + gson.toJson(chat.getCharacterUser()).replace("'", "''") + "'," +
                KEY_CHAR_AI + " = '" + gson.toJson(chat.getCharacterAI()).replace("'", "''") + "'," +
                KEY_DATE_UPDATE + " = '" + chat.getDateUpdate() + "'" +
                " WHERE " + KEY_ID + " = '" + chat.getId() + "'";
        db.querrydata(sql);
    }

    public void deleteChat(Chat chat) {
        String sql = "DELETE FROM " + TABLE_CHATS + " WHERE " + KEY_ID + " = '" + chat.getId() + "'";
        db.querrydata(sql);
    }

    private Chat cursorToChat(Cursor cursor) {
        Chat chat = new Chat();
        chat.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
        chat.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        chat.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
        chat.setDateCreate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATE)));
        chat.setDateUpdate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_UPDATE)));

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
