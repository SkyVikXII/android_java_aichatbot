package com.nhom4.aichatbot.Models;

public class User {
    private String uid;
    private String email;
    private String password;
    public User(){};
    public User(String email){
        this.email = email;
    };
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public User(String uid, String email, String password) {
        this.uid = uid;
        this.email = email;
        this.password = password;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
