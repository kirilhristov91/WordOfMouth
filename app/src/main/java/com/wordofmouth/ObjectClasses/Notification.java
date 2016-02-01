package com.wordofmouth.ObjectClasses;

public class Notification {

    int id;
    int listId;
    int userId;
    String msg;
    int accepted;

    public Notification(int listId, int userId, String msg, int accepted){
        this.listId = listId;
        this.userId = userId;
        this.msg = msg;
        this.accepted = accepted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }
}
