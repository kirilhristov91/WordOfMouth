package com.wordofmouth.ObjectClasses;

import java.util.Date;

public class Notification {

    int id;
    int listId;
    String msg;
    int accepted;
    String date;

    public Notification(int listId, String msg, String date, int accepted){
        this.listId = listId;
        this.msg = msg;
        this.date = date;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }
}
