package com.nhom4.aichatbot.Database;

import android.content.Context;
import android.database.Cursor;
import com.nhom4.aichatbot.Models.Character;
import java.util.ArrayList;
import java.util.List;

public class CharacterDbHelper {
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
            "CREATE TABLE IF NOT EXISTS " + TABLE_CHARACTERS + "(" +
                    KEY_ID + " TEXT PRIMARY KEY," +
                    KEY_NAME + " TEXT," +
                    KEY_DESCRIPTION + " TEXT," +
                    KEY_DATE_CREATE + " TEXT," +
                    KEY_DATE_UPDATE + " TEXT," +
                    KEY_SYNCED + " INTEGER DEFAULT 0" + ")";

    private DataBase db;

    public CharacterDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_CHARACTERS);
    }

    // CRUD Operations
    public int addCharacter(Character character, boolean isSynced) {
        String name = character.getName().replace("'", "''");
        String description = character.getDescription().replace("'", "''");
        int synced = isSynced ? 1 : 0;

        String sql = "INSERT INTO " + TABLE_CHARACTERS + " (" +
                KEY_ID + ", " + KEY_NAME + ", " + KEY_DESCRIPTION + ", " +
                KEY_DATE_CREATE + ", " + KEY_DATE_UPDATE + ", " + KEY_SYNCED +
                ") VALUES ('" +
                character.getId() + "', '" +
                name + "', '" +
                description + "', '" +
                character.getDatecreate() + "', '" +
                character.getDateupdate() + "', " +
                synced + ")";
        db.querrydata(sql);
        return 1;
    }

    public List<Character> getAllCharacters() {
        List<Character> characterList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHARACTERS + " ORDER BY " + KEY_DATE_UPDATE + " DESC";

        Cursor cursor = db.getdata(selectQuery);

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
        return characterList;
    }

    public List<Character> getUnsyncedCharacters() {
        List<Character> characterList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CHARACTERS + " WHERE " + KEY_SYNCED + " = 0";

        Cursor cursor = db.getdata(selectQuery);

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
        return characterList;
    }

    public int updateCharacter(Character character, boolean isSynced) {
        String name = character.getName().replace("'", "''");
        String description = character.getDescription().replace("'", "''");
        int synced = isSynced ? 1 : 0;

        String sql = "UPDATE " + TABLE_CHARACTERS + " SET " +
                KEY_NAME + " = '" + name + "', " +
                KEY_DESCRIPTION + " = '" + description + "', " +
                KEY_DATE_UPDATE + " = '" + character.getDateupdate() + "', " +
                KEY_SYNCED + " = " + synced +
                " WHERE " + KEY_ID + " = '" + character.getId() + "'";
        db.querrydata(sql);
        return 1;
    }

    public void markAsSynced(String characterId) {
        String sql = "UPDATE " + TABLE_CHARACTERS + " SET " + KEY_SYNCED + " = 1 WHERE " + KEY_ID + " = '" + characterId + "'";
        db.querrydata(sql);
    }

    public void deleteCharacter(String characterId) {
        String sql = "DELETE FROM " + TABLE_CHARACTERS + " WHERE " + KEY_ID + " = '" + characterId + "'";
        db.querrydata(sql);
    }

    public Character getCharacterById(String characterId) {
        String selectQuery = "SELECT * FROM " + TABLE_CHARACTERS + " WHERE " + KEY_ID + " = '" + characterId + "'";
        Cursor cursor = db.getdata(selectQuery);

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
        return character;
    }
}
