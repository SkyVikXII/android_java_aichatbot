package com.nhom4.aichatbot.Database;

import android.content.Context;
import android.database.Cursor;
import com.nhom4.aichatbot.Models.Model;
import java.util.ArrayList;
import java.util.List;

public class ModelDbHelper {
    private static final String DATABASE_NAME = "settings.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MODELS = "models";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CONTEXT_LENGTH = "context_length";
    private static final String KEY_MAX_TOKENS = "max_tokens";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_TOP_P = "top_p";
    private static final String KEY_FREQUENCY_PENALTY = "frequency_penalty";
    private static final String KEY_PRESENCE_PENALTY = "presence_penalty";
    private static final String KEY_SYNCED = "synced";

    private static final String CREATE_TABLE_MODELS = "CREATE TABLE IF NOT EXISTS " + TABLE_MODELS + "(" +
            KEY_ID + " TEXT PRIMARY KEY," +
            KEY_NAME + " TEXT," +
            KEY_DESCRIPTION + " TEXT," +
            KEY_CONTEXT_LENGTH + " TEXT," +
            KEY_MAX_TOKENS + " TEXT," +
            KEY_TEMPERATURE + " TEXT," +
            KEY_TOP_P + " TEXT," +
            KEY_FREQUENCY_PENALTY + " TEXT," +
            KEY_PRESENCE_PENALTY + " TEXT," +
            KEY_SYNCED + " INTEGER DEFAULT 0" + ")";

    private DataBase db;

    public ModelDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_MODELS);
    }

    public void addModel(Model model, boolean isSynced) {
        int synced = isSynced ? 1 : 0;
        String sql = "INSERT INTO " + TABLE_MODELS + " VALUES ('" + model.getId() + "', '" + model.getName() + "', '" + model.getDescription() + "', '" + model.getContext_length() + "', '" + model.getMax_tokens() + "', '" + model.getTemperature() + "', '" + model.getTop_p() + "', '" + model.getFrequency_penalty() + "', '" + model.getPresence_penalty() + "', " + synced + ")";
        db.querrydata(sql);
    }

    private Model cursorToModel(Cursor cursor) {
        Model model = new Model();
        model.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
        model.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        model.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
        model.setContext_length(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTEXT_LENGTH)));
        model.setMax_tokens(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MAX_TOKENS)));
        model.setTemperature(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TEMPERATURE)));
        model.setTop_p(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TOP_P)));
        model.setFrequency_penalty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FREQUENCY_PENALTY)));
        model.setPresence_penalty(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRESENCE_PENALTY)));
        return model;
    }

    public List<Model> getAllModels() {
        List<Model> list = new ArrayList<>();
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_MODELS);
        while (cursor.moveToNext()) {
            list.add(cursorToModel(cursor));
        }
        cursor.close();
        return list;
    }

    public Model getModelById(String modelId) {
        Model model = null;
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_MODELS + " WHERE " + KEY_ID + " = '" + modelId + "'");
        if (cursor.moveToFirst()) {
            model = cursorToModel(cursor);
        }
        cursor.close();
        return model;
    }

    public void updateModel(Model model, boolean isSynced) {
        int synced = isSynced ? 1 : 0;
        String sql = "UPDATE " + TABLE_MODELS + " SET " +
                KEY_NAME + " = '" + model.getName() + "', " +
                KEY_DESCRIPTION + " = '" + model.getDescription() + "', " +
                KEY_CONTEXT_LENGTH + " = '" + model.getContext_length() + "', " +
                KEY_MAX_TOKENS + " = '" + model.getMax_tokens() + "', " +
                KEY_TEMPERATURE + " = '" + model.getTemperature() + "', " +
                KEY_TOP_P + " = '" + model.getTop_p() + "', " +
                KEY_FREQUENCY_PENALTY + " = '" + model.getFrequency_penalty() + "', " +
                KEY_PRESENCE_PENALTY + " = '" + model.getPresence_penalty() + "', " +
                KEY_SYNCED + " = " + synced +
                " WHERE " + KEY_ID + " = '" + model.getId() + "'";
        db.querrydata(sql);
    }

    public void deleteModel(String modelId) {
        db.querrydata("DELETE FROM " + TABLE_MODELS + " WHERE " + KEY_ID + " = '" + modelId + "'");
    }

    public void markAsSynced(String modelId) {
        db.querrydata("UPDATE " + TABLE_MODELS + " SET " + KEY_SYNCED + " = 1 WHERE " + KEY_ID + " = '" + modelId + "'");
    }

    public List<Model> getUnsyncedModels() {
        List<Model> list = new ArrayList<>();
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_MODELS + " WHERE " + KEY_SYNCED + " = 0");
        while (cursor.moveToNext()) {
            list.add(cursorToModel(cursor));
        }
        cursor.close();
        return list;
    }
}
