package com.wordofmouth.ObjectClasses;

public class Shared {

    private int listId;
    private String username;

    public Shared(int listId, String username){
        this.listId = listId;
        this.username = username;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
