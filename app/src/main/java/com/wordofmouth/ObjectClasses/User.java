package com.wordofmouth.ObjectClasses;

public class User {
    int id;
    String name;
    String username;
    String email;
    String password;

    String gcmId;
    String picture;

    public User(int id, String name, String email, String username, String password){
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password){
        this.id = 0;
        this.username = username;
        this.password = password;
        this.name = "";
        this.email = "";
    }

    public User(int id, String name, String username, String picture){
        this.id=id;
        this.username = username;
        this.name=name;
        this.picture = picture;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
