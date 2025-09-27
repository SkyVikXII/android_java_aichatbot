package com.nhom4.aichatbot.Models;

public class Model {
    private String id;
    private String name;
    private String description;
    private String context_length;
    private String max_tokens;
    private String temperature;
    private String top_p;
    private String frequency_penalty;
    private String presence_penalty;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext_length() {
        return context_length;
    }

    public void setContext_length(String context_length) {
        this.context_length = context_length;
    }

    public String getMax_tokens() {
        return max_tokens;
    }

    public void setMax_tokens(String max_tokens) {
        this.max_tokens = max_tokens;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTop_p() {
        return top_p;
    }

    public void setTop_p(String top_p) {
        this.top_p = top_p;
    }

    public String getFrequency_penalty() {
        return frequency_penalty;
    }

    public void setFrequency_penalty(String frequency_penalty) {
        this.frequency_penalty = frequency_penalty;
    }

    public String getPresence_penalty() {
        return presence_penalty;
    }

    public void setPresence_penalty(String presence_penalty) {
        this.presence_penalty = presence_penalty;
    }
}
