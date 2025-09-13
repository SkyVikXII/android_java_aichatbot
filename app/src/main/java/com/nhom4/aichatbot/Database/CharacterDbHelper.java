package com.nhom4.aichatbot.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.nhom4.aichatbot.Models.Character;
import java.util.ArrayList;
import java.util.List;

public class CharacterDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "characters.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_CHARACTERS = "characters";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE_CREATE = "datecreate";
    private static final String KEY_DATE_UPDATE = "dateupdate";
    private static final String KEY_SYNCED = "synced"; // 0 = not synced, 1 = synced

    // Create table SQL
    private static final String CREATE_TABLE_CHARACTERS =
            "CREATE TABLE " + TABLE_CHARACTERS + "(" +
                    KEY_ID + " TEXT PRIMARY KEY," +
                    KEY_NAME + " TEXT," +
                    KEY_DESCRIPTION + " TEXT," +
                    KEY_DATE_CREATE + " TEXT," +
                    KEY_DATE_UPDATE + " TEXT," +
                    KEY_SYNCED + " INTEGER DEFAULT 0" + ")";

    public CharacterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHARACTERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHARACTERS);
        onCreate(db);
    }

    // CRUD Operations
    public long addCharacter(Character character, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, character.getId());
        values.put(KEY_NAME, character.getName());
        values.put(KEY_DESCRIPTION, character.getDescription());
        values.put(KEY_DATE_CREATE, character.getDatecreate());
        values.put(KEY_DATE_UPDATE, character.getDateupdate());
        values.put(KEY_SYNCED, isSynced ? 1 : 0);

        long result = db.insert(TABLE_CHARACTERS, null, values);
        db.close();
        return result;
    }

    public List<Character> getAllCharacters() {
        List<Character> characterList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHARACTERS + " ORDER BY " + KEY_DATE_UPDATE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Character character = new Character();
                character.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
                character.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                character.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                character.setDatecreate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATE)));
                character.setDateupdate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_UPDATE)));

                characterList.add(character);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return characterList;
    }

    public List<Character> getUnsyncedCharacters() {
        List<Character> characterList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHARACTERS + " WHERE " + KEY_SYNCED + " = 0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Character character = new Character();
                character.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
                character.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                character.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
                character.setDatecreate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATE)));
                character.setDateupdate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_UPDATE)));

                characterList.add(character);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return characterList;
    }

    public int updateCharacter(Character character, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, character.getName());
        values.put(KEY_DESCRIPTION, character.getDescription());
        values.put(KEY_DATE_UPDATE, character.getDateupdate());
        values.put(KEY_SYNCED, isSynced ? 1 : 0);

        int result = db.update(TABLE_CHARACTERS, values, KEY_ID + " = ?",
                new String[]{character.getId()});
        db.close();
        return result;
    }

    public void markAsSynced(String characterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SYNCED, 1);

        db.update(TABLE_CHARACTERS, values, KEY_ID + " = ?",
                new String[]{characterId});
        db.close();
    }

    public void deleteCharacter(String characterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHARACTERS, KEY_ID + " = ?", new String[]{characterId});
        db.close();
    }

    public Character getCharacterById(String characterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CHARACTERS, null, KEY_ID + " = ?",
                new String[]{characterId}, null, null, null);

        Character character = null;
        if (cursor != null && cursor.moveToFirst()) {
            character = new Character();
            character.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            character.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            character.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
            character.setDatecreate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_CREATE)));
            character.setDateupdate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_UPDATE)));
            cursor.close();
        }
        db.close();
        return character;
    }
}