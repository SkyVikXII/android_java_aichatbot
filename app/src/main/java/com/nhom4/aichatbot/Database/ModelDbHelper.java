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
    private static final String KEY_IS_DEFAULT = "is_default";
    private static final String KEY_IS_ACTIVE = "is_active";

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
            KEY_SYNCED + " INTEGER DEFAULT 0," +
            KEY_IS_DEFAULT + " INTEGER DEFAULT 0," +
            KEY_IS_ACTIVE + " INTEGER DEFAULT 0" + ")";

    private DataBase db;

    public ModelDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_MODELS);
        populateInitialData();
    }

    private void populateInitialData() {
        if (getAllModels().isEmpty()) {
            Model defaultModel = new Model();
            defaultModel.setId("model_default_0");
            defaultModel.setName("Default GPT-4");
            defaultModel.setDescription("A powerful default model");
            defaultModel.setContext_length("8192");
            defaultModel.setMax_tokens("2048");
            defaultModel.setTemperature("1.0");
            defaultModel.setTop_p("1.0");
            defaultModel.setFrequency_penalty("0.0");
            defaultModel.setPresence_penalty("0.0");
            defaultModel.setDefault(true);
            defaultModel.setActive(true);
            addModel(defaultModel, true);
        }
    }

    public void addModel(Model model, boolean isSynced) {
        String sql = "INSERT INTO " + TABLE_MODELS + " (id, name, description, context_length, max_tokens, temperature, top_p, frequency_penalty, presence_penalty, synced, is_default, is_active) VALUES ('" +
                model.getId() + "', '" + model.getName() + "', '" + model.getDescription() + "', '" + model.getContext_length() + "', '" + model.getMax_tokens() + "', '" + model.getTemperature() + "', '" + model.getTop_p() + "', '" + model.getFrequency_penalty() + "', '" + model.getPresence_penalty() + "', " + (isSynced ? 1:0) + ", " + (model.isDefault() ? 1:0) + ", " + (model.isActive() ? 1:0) + ")";
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
        model.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_DEFAULT)) == 1);
        model.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1);
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

    public void updateModel(Model model, boolean isSynced) {
        String sql = "UPDATE " + TABLE_MODELS + " SET " +
                KEY_NAME + " = '" + model.getName() + "', " +
                KEY_DESCRIPTION + " = '" + model.getDescription() + "', " +
                KEY_CONTEXT_LENGTH + " = '" + model.getContext_length() + "', " +
                KEY_MAX_TOKENS + " = '" + model.getMax_tokens() + "', " +
                KEY_TEMPERATURE + " = '" + model.getTemperature() + "', " +
                KEY_TOP_P + " = '" + model.getTop_p() + "', " +
                KEY_FREQUENCY_PENALTY + " = '" + model.getFrequency_penalty() + "', " +
                KEY_PRESENCE_PENALTY + " = '" + model.getPresence_penalty() + "', " +
                KEY_SYNCED + " = " + (isSynced ? 1:0) + ", " +
                KEY_IS_DEFAULT + " = " + (model.isDefault() ? 1:0) + ", " +
                KEY_IS_ACTIVE + " = " + (model.isActive() ? 1:0) +
                " WHERE " + KEY_ID + " = '" + model.getId() + "'";
        db.querrydata(sql);
    }

    public void setModelActive(String modelId) {
        db.querrydata("UPDATE " + TABLE_MODELS + " SET " + KEY_IS_ACTIVE + " = 0");
        db.querrydata("UPDATE " + TABLE_MODELS + " SET " + KEY_IS_ACTIVE + " = 1 WHERE " + KEY_ID + " = '" + modelId + "'");
    }

    public void deleteModel(String modelId) {
        db.querrydata("DELETE FROM " + TABLE_MODELS + " WHERE " + KEY_ID + " = '" + modelId + "'");
    }
}
