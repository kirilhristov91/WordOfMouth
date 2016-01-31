package com.wordofmouth.ObjectClasses;


public class MyList {
    private int _listId;

    private int userId;
    private String _username;
    private String _name;
    private String image;
    private String _description;

    public MyList(){}

    public MyList(int userId, String _username, String name, String description, String image) {
        this.userId = userId;
        this._username = _username;
        this._name = name;
        this.image = image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void set_name(String _name) {
        this._name = _name;
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

    public String get_description() {
        return _description;
    }
}
