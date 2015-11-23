package com.wordofmouth;


public class Item {
    private int _itemId;
    private int _listId;
    private String _name;
    private double _rating;
    private String _description;

    public Item(String name, double rating, String description) {
        this._name = name;
        this._rating = rating;
        this._description = description;
    }

    public void set_itemId(int _itemId) {
        this._itemId = _itemId;
    }

    public void set_listId(int _listId) {
        this._listId = _listId;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public void set_rating(double _rating) {
        this._rating = _rating;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public int get_itemId() {
        return _itemId;
    }

    public int get_listId() {
        return _listId;
    }

    public String get_name() {
        return _name;
    }

    public double get_rating() {
        return _rating;
    }

    public String get_description() {
        return _description;
    }
}
