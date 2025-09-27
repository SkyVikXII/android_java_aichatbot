package com.nhom4.aichatbot.Models;

import java.util.List;

public class Chat {
    private String id;
    private String name;
    private String description;
    private List<Message> messages;
    private Character characterUser; // User's roleplay character
    private Character characterAI;   // AI's roleplay character

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

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Character getCharacterUser() {
        return characterUser;
    }

    public void setCharacterUser(Character characterUser) {
        this.characterUser = characterUser;
    }

    public Character getCharacterAI() {
        return characterAI;
    }

    public void setCharacterAI(Character characterAI) {
        this.characterAI = characterAI;
    }
}
