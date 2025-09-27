package com.nhom4.aichatbot.Database;

import android.content.Context;
import android.database.Cursor;
import com.nhom4.aichatbot.Models.Prompt;
import java.util.ArrayList;
import java.util.List;

public class PromptDbHelper {
    private static final String DATABASE_NAME = "settings.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PROMPTS = "prompts";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SYNCED = "synced";
    private static final String KEY_IS_DEFAULT = "is_default";
    private static final String KEY_IS_ACTIVE = "is_active";

    private static final String CREATE_TABLE_PROMPTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PROMPTS + "(" +
            KEY_ID + " TEXT PRIMARY KEY," +
            KEY_NAME + " TEXT," +
            KEY_CONTENT + " TEXT," +
            KEY_TYPE + " INTEGER," +
            KEY_SYNCED + " INTEGER DEFAULT 0," +
            KEY_IS_DEFAULT + " INTEGER DEFAULT 0," +
            KEY_IS_ACTIVE + " INTEGER DEFAULT 0" + ")";

    private DataBase db;

    public PromptDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_PROMPTS);
        populateInitialData();
    }

    private void populateInitialData() {
        if (getAllPrompts().isEmpty()) {
            Prompt defaultPrompt = new Prompt();
            defaultPrompt.setId("prompt_default_0");
            defaultPrompt.setName("Default System Prompt");
            defaultPrompt.setContent("You are a helpful assistant.");
            defaultPrompt.setType(1); // 1 for System
            defaultPrompt.setDefault(true);
            defaultPrompt.setActive(true);
            addPrompt(defaultPrompt, true);
        }
    }

    public void addPrompt(Prompt prompt, boolean isSynced) {
        String sql = "INSERT INTO " + TABLE_PROMPTS + " (id, name, content, type, synced, is_default, is_active) VALUES ('" + 
        prompt.getId() + "', '" + prompt.getName() + "', '" + prompt.getContent() + "', " + prompt.getType() + ", " + (isSynced ? 1:0) + ", " + (prompt.isDefault() ? 1:0) + ", " + (prompt.isActive() ? 1:0) + ")";
        db.querrydata(sql);
    }

    private Prompt cursorToPrompt(Cursor cursor) {
        Prompt prompt = new Prompt();
        prompt.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
        prompt.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        prompt.setContent(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTENT)));
        prompt.setType(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE)));
        prompt.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_DEFAULT)) == 1);
        prompt.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1);
        return prompt;
    }

    public List<Prompt> getAllPrompts() {
        List<Prompt> list = new ArrayList<>();
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_PROMPTS);
        while (cursor.moveToNext()) {
            list.add(cursorToPrompt(cursor));
        }
        cursor.close();
        return list;
    }

    public void updatePrompt(Prompt prompt, boolean isSynced) {
        String sql = "UPDATE " + TABLE_PROMPTS + " SET " +
                KEY_NAME + " = '" + prompt.getName() + "', " +
                KEY_CONTENT + " = '" + prompt.getContent() + "', " +
                KEY_TYPE + " = " + prompt.getType() + ", " +
                KEY_SYNCED + " = " + (isSynced ? 1:0) + ", " +
                KEY_IS_DEFAULT + " = " + (prompt.isDefault() ? 1:0) + ", " +
                KEY_IS_ACTIVE + " = " + (prompt.isActive() ? 1:0) +
                " WHERE " + KEY_ID + " = '" + prompt.getId() + "'";
        db.querrydata(sql);
    }

    public void setPromptActive(String promptId, boolean isActive) {
        db.querrydata("UPDATE " + TABLE_PROMPTS + " SET " + KEY_IS_ACTIVE + " = " + (isActive ? 1:0) + " WHERE " + KEY_ID + " = '" + promptId + "'");
    }

    public void deletePrompt(String promptId) {
        db.querrydata("DELETE FROM " + TABLE_PROMPTS + " WHERE " + KEY_ID + " = '" + promptId + "'");
    }
}
