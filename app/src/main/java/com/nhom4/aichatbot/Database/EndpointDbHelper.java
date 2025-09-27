package com.nhom4.aichatbot.Database;

import android.content.Context;
import android.database.Cursor;
import com.nhom4.aichatbot.Models.Endpoint;
import java.util.ArrayList;
import java.util.List;

public class EndpointDbHelper {
    private static final String DATABASE_NAME = "settings.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_ENDPOINTS = "endpoints";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_SYNCED = "synced";

    private static final String CREATE_TABLE_ENDPOINTS = "CREATE TABLE IF NOT EXISTS " + TABLE_ENDPOINTS + "(" +
            KEY_ID + " TEXT PRIMARY KEY," +
            KEY_NAME + " TEXT," +
            KEY_URL + " TEXT," +
            KEY_API_KEY + " TEXT," +
            KEY_SYNCED + " INTEGER DEFAULT 0" + ")";

    private DataBase db;

    public EndpointDbHelper(Context context) {
        db = new DataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        db.querrydata(CREATE_TABLE_ENDPOINTS);
    }

    public void addEndpoint(Endpoint endpoint, boolean isSynced) {
        int synced = isSynced ? 1 : 0;
        String sql = "INSERT INTO " + TABLE_ENDPOINTS + " VALUES ('" +
                endpoint.getId() + "', '" +
                endpoint.getName() + "', '" +
                endpoint.getEndpoint_url() + "', '" +
                endpoint.getAPI_KEY() + "', " +
                synced + ")";
        db.querrydata(sql);
    }

    public List<Endpoint> getAllEndpoints() {
        List<Endpoint> list = new ArrayList<>();
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_ENDPOINTS);
        while (cursor.moveToNext()) {
            Endpoint endpoint = new Endpoint();
            endpoint.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            endpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            endpoint.setEndpoint_url(cursor.getString(cursor.getColumnIndexOrThrow(KEY_URL)));
            endpoint.setAPI_KEY(cursor.getString(cursor.getColumnIndexOrThrow(KEY_API_KEY)));
            list.add(endpoint);
        }
        cursor.close();
        return list;
    }

    public Endpoint getEndpointById(String endpointId) {
        Endpoint endpoint = null;
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_ENDPOINTS + " WHERE " + KEY_ID + " = '" + endpointId + "'");
        if (cursor.moveToFirst()) {
            endpoint = new Endpoint();
            endpoint.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            endpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            endpoint.setEndpoint_url(cursor.getString(cursor.getColumnIndexOrThrow(KEY_URL)));
            endpoint.setAPI_KEY(cursor.getString(cursor.getColumnIndexOrThrow(KEY_API_KEY)));
        }
        cursor.close();
        return endpoint;
    }

    public void updateEndpoint(Endpoint endpoint, boolean isSynced) {
        int synced = isSynced ? 1 : 0;
        String sql = "UPDATE " + TABLE_ENDPOINTS + " SET " +
                KEY_NAME + " = '" + endpoint.getName() + "', " +
                KEY_URL + " = '" + endpoint.getEndpoint_url() + "', " +
                KEY_API_KEY + " = '" + endpoint.getAPI_KEY() + "', " +
                KEY_SYNCED + " = " + synced +
                " WHERE " + KEY_ID + " = '" + endpoint.getId() + "'";
        db.querrydata(sql);
    }

    public void deleteEndpoint(String endpointId) {
        db.querrydata("DELETE FROM " + TABLE_ENDPOINTS + " WHERE " + KEY_ID + " = '" + endpointId + "'");
    }

    public void markAsSynced(String endpointId) {
        db.querrydata("UPDATE " + TABLE_ENDPOINTS + " SET " + KEY_SYNCED + " = 1 WHERE " + KEY_ID + " = '" + endpointId + "'");
    }

    public List<Endpoint> getUnsyncedEndpoints() {
        List<Endpoint> list = new ArrayList<>();
        Cursor cursor = db.getdata("SELECT * FROM " + TABLE_ENDPOINTS + " WHERE " + KEY_SYNCED + " = 0");
        while (cursor.moveToNext()) {
            Endpoint endpoint = new Endpoint();
            endpoint.setId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)));
            endpoint.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
            endpoint.setEndpoint_url(cursor.getString(cursor.getColumnIndexOrThrow(KEY_URL)));
            endpoint.setAPI_KEY(cursor.getString(cursor.getColumnIndexOrThrow(KEY_API_KEY)));
            list.add(endpoint);
        }
        cursor.close();
        return list;
    }
}
