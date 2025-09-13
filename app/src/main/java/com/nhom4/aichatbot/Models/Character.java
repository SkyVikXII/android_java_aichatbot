package com.nhom4.aichatbot.Models;

public class Character {
    private static final String TAG = "Character";
    String datecreate;
    String dateupdate;
    String id;
    String name;
    String description;

    public String getDatecreate() {
        return datecreate;
    }

    public void setDatecreate(String datecreate) {
        this.datecreate = datecreate;
    }

    public String getDateupdate() {
        return dateupdate;
    }

    public void setDateupdate(String dateupdate) {
        this.dateupdate = dateupdate;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
