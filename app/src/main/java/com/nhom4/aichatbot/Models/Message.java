package com.nhom4.aichatbot.Models;

import java.util.Date;

public class Message {
    private String id;
    private Date date;
    private String role; // Can be a character ID or a generic role like 'user'/'assistant'
    private String content;

    public Message(String id, Date date, String role, String content) {
        this.id = id;
        this.date = date;
        this.role = role;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
