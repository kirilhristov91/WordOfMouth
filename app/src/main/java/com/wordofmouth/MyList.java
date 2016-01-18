package com.wordofmouth;


public class MyList {
    private int _listId;
    private int _creatorId;
    private String _name;
    private int _visibility;
    private String _description;

    public MyList(){}

    public MyList(int _creatorId, String name, int visibility, String description) {
        this._creatorId = _creatorId;
        this._name = name;
        this._visibility = visibility;
        this._description = description;

    }

    public void set_listId(int _listId) {
        this._listId = _listId;
    }

    public void set_creatorId(int _creatorId) {
        this._creatorId = _creatorId;
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

    public int get_creatorId() {
        return _creatorId;
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
