package com.wordofmouth.ObjectClasses;


public class MyList {
    private int _listId;

    private int userId;
    private String _username;
    private String _name;
    private int _visibility;
    private String _description;

    public MyList(){}

    public MyList(int userId, String _username, String name, int visibility, String description) {
        this.userId = userId;
        this._username = _username;
        this._name = name;
        this._visibility = visibility;
        this._description = description;

    }

    public void set_listId(int _listId) {
        this._listId = _listId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public void set_visibility(int _visibility) {
        this._visibility = _visibility;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public int get_listId() {
        return _listId;
    }

    public String get_username() {
        return _username;
    }

    public String get_name() {
        return _name;
    }

    public int get_visibility() {
        return _visibility;
    }

    public String get_description() {
        return _description;
    }
}
