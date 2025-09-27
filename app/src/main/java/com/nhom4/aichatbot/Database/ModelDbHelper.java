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
    private static final String KEY_API_MODEL_ID = "api_model_id";

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
            KEY_IS_ACTIVE + " INTEGER DEFAULT 0," +
            KEY_API_MODEL_ID + " TEXT" + ")";

    private DataBase db;

    public ModelDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_MODELS);
        populateInitialData();
    }

    private void populateInitialData() {
        List<Model> defaultModels = new ArrayList<>();

        // Default Model 1
        Model defaultModel1 = new Model();
        defaultModel1.setId("model_default_0");
        defaultModel1.setName("DeepSeek: DeepSeek V3.1 (free)");
        defaultModel1.setDescription("DeepSeek-V3.1 is a large hybrid reasoning model (671B parameters, 37B active) that supports both thinking and non-thinking modes via prompt templates.");
        defaultModel1.setContext_length("64000");
        defaultModel1.setMax_tokens("16000");
        defaultModel1.setTemperature("1.0");
        defaultModel1.setTop_p("1.0");
        defaultModel1.setFrequency_penalty("0.0");
        defaultModel1.setPresence_penalty("0.0");
        defaultModel1.setDefault(true);
        defaultModel1.setActive(true);
        defaultModel1.setApi_model_id("deepseek/deepseek-chat-v3.1:free");
        defaultModels.add(defaultModel1);

        // Default Model 2 (Example)
        Model defaultModel2 = new Model();
        defaultModel2.setId("model_default_1");
        defaultModel2.setName("OpenAI: gpt-oss-120b (free)");
        defaultModel2.setDescription("gpt-oss-120b is an open-weight, 117B-parameter Mixture-of-Experts (MoE) language model from OpenAI designed for high-reasoning, agentic, and general-purpose production use cases.");
        defaultModel2.setContext_length("32000");
        defaultModel2.setMax_tokens("8000");
        defaultModel2.setTemperature("0.7");
        defaultModel2.setTop_p("0.9");
        defaultModel2.setFrequency_penalty("0.0");
        defaultModel2.setPresence_penalty("0.0");
        defaultModel2.setDefault(true);
        defaultModel2.setActive(false);
        defaultModel2.setApi_model_id("openai/gpt-oss-120b:free");
        defaultModels.add(defaultModel2);

        for (Model defaultModel : defaultModels) {
            Model existingModel = getModelById(defaultModel.getId());
            if (existingModel == null) {
                addModel(defaultModel, true);
            } else {
                // Update existing default model
                updateModel(defaultModel, true);
            }
        }
    }

    public void addModel(Model model, boolean isSynced) {
        String name = model.getName().replace("'", "''");
        String description = model.getDescription().replace("'", "''");
        String sql = "INSERT INTO " + TABLE_MODELS + " (id, name, description, context_length, max_tokens, temperature, top_p, frequency_penalty, presence_penalty, synced, is_default, is_active, api_model_id) VALUES ('" +
                model.getId() + "', '" + name + "', '" + description + "', '" + model.getContext_length() + "', '" + model.getMax_tokens() + "', '" + model.getTemperature() + "', '" + model.getTop_p() + "', '" + model.getFrequency_penalty() + "', '" + model.getPresence_penalty() + "', " + (isSynced ? 1:0) + ", " + (model.isDefault() ? 1:0) + ", " + (model.isActive() ? 1:0) + ", '" + model.getApi_model_id() + "')";
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
        model.setApi_model_id(cursor.getString(cursor.getColumnIndexOrThrow(KEY_API_MODEL_ID)));
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
        String name = model.getName().replace("'", "''");
        String description = model.getDescription().replace("'", "''");
        String sql = "UPDATE " + TABLE_MODELS + " SET " +
                KEY_NAME + " = '" + name + "', " +
                KEY_DESCRIPTION + " = '" + description + "', " +
                KEY_CONTEXT_LENGTH + " = '" + model.getContext_length() + "', " +
                KEY_MAX_TOKENS + " = '" + model.getMax_tokens() + "', " +
                KEY_TEMPERATURE + " = '" + model.getTemperature() + "', " +
                KEY_TOP_P + " = '" + model.getTop_p() + "', " +
                KEY_FREQUENCY_PENALTY + " = '" + model.getFrequency_penalty() + "', " +
                KEY_PRESENCE_PENALTY + " = '" + model.getPresence_penalty() + "', " +
                KEY_SYNCED + " = " + (isSynced ? 1:0) + ", " +
                KEY_IS_DEFAULT + " = " + (model.isDefault() ? 1:0) + ", " +
                KEY_IS_ACTIVE + " = " + (model.isActive() ? 1:0) + ", " +
                KEY_API_MODEL_ID + " = '" + model.getApi_model_id() + "'" +
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

    public Model getModelById(String id) {
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_MODELS + " WHERE " + KEY_ID + " = '" + id + "'");
        if (cursor.moveToFirst()) {
            Model model = cursorToModel(cursor);
            cursor.close();
            return model;
        }
        cursor.close();
        return null;
    }
}
