package com.nhom4.aichatbot.Models;

public class Endpoint {
    private String id;
    private String name;
    private String endpoint_url;
    private String API_KEY;
    private boolean isActive;

    public Endpoint() {
    }

    public Endpoint(String name, String endpoint_url, String API_KEY) {
        this.name = name;
        this.endpoint_url = endpoint_url;
        this.API_KEY = API_KEY;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint_url() {
        return endpoint_url;
    }

    public void setEndpoint_url(String endpoint_url) {
        this.endpoint_url = endpoint_url;
    }

    public String getAPI_KEY() {
        return API_KEY;
    }

    public void setAPI_KEY(String API_KEY) {
        this.API_KEY = API_KEY;
    }
}
