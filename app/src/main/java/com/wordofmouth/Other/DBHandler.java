package com.wordofmouth.Other;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;

import java.util.ArrayList;


public class DBHandler extends SQLiteOpenHelper{

    private static DBHandler sInstance;

    //if updating the database change the version:
    private static final int DATABASE_VERSION = 14;
    private static final String DATABASE_NAME = "WOM.db";

    //Lists table
    public static final String TABLE_USER_LISTS = "UserLists";
    public static final String COLUMN_ID = "_listId";
    public static final String COLUMN_UserId = "_userId";
    public static final String COLUMN_Username = "_username";
    public static final String COLUMN_Name = "_name";
    public static final String COLUMN_Visibility = "_visibility";
    public static final String COLUMN_Description = "_description";

    // Items table
    public static final String TABLE_Items = "Items";
    public static final String COLUMN_ItemID = "_itemId";
    public static final String COLUMN_ListID = "_listId";
    public static final String COLUMN_CreatorId = "_creatorId";
    public static final String COLUMN_Creator = "_creatorUsername";
    public static final String COLUMN_ItemName = "_itemName";
    public static final String COLUMN_Rating = "_rating";
    public static final String COLUMN_ItemDescription = "_description";
    public static final String COLUMN_ItemImage = "_itemImage";


    //Profile image table
    public static final String TABLE_Profile_Image = "ProfileImage";
    public static final String COLUMN_UserID = "_userId";
    public static final String COLUMN_Image = "_image";


    /*public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }*/

    public static synchronized DBHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CreateListTableQuery = "CREATE TABLE " + TABLE_USER_LISTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_UserId + " INTEGER, " +
                COLUMN_Username + " TEXT, " +
                COLUMN_Name + " TEXT, " +
                COLUMN_Visibility + " INTEGER, " +
                COLUMN_Description + " TEXT " +
                ");";

        String CreateItemsTableQuery = "CREATE TABLE " + TABLE_Items + "(" +
                COLUMN_ItemID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ListID + " INTEGER, " +
                COLUMN_CreatorId + " INTEGER, " +
                COLUMN_Creator + " TEXT, " +
                COLUMN_ItemName + " TEXT, " +
                COLUMN_Rating + " DOUBLE, " +
                COLUMN_ItemDescription + " TEXT, " +
                COLUMN_ItemImage + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_ListID + ") REFERENCES " +
                TABLE_USER_LISTS + "(" + COLUMN_ID + ")"+
                ");";

        String CreateProfileImageTableQuery = "CREATE TABLE " + TABLE_Profile_Image + "(" +
                COLUMN_UserID + " INTEGER PRIMARY KEY, " +
                COLUMN_Image + " TEXT " +
                ");";

        db.execSQL(CreateListTableQuery);
        db.execSQL(CreateItemsTableQuery);
        db.execSQL(CreateProfileImageTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Items);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Profile_Image);
        onCreate(db);
    }

    //Add a new row to table lists
    public void addList(MyList ul){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ListID, ul.get_listId());
        values.put(COLUMN_UserId, ul.getUserId());
        values.put(COLUMN_Username, ul.get_username());
        values.put(COLUMN_Name, ul.get_name());
        values.put(COLUMN_Visibility, ul.get_visibility());
        values.put(COLUMN_Description, ul.get_description());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_USER_LISTS, null, values);
        db.close();
    }

    public void addItem(Item i){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ItemID, i.get_itemId());
        values.put(COLUMN_ListID, i.get_listId());
        values.put(COLUMN_CreatorId, i.get_creatorId());
        values.put(COLUMN_Creator, i.get_creatorUsername());
        values.put(COLUMN_ItemName, i.get_name());
        values.put(COLUMN_Rating, i.get_rating());
        values.put(COLUMN_Description, i.get_description());
        values.put(COLUMN_ItemImage, i.get_itemImage());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Items, null, values);
        db.close();
    }

    public void addProfilePicture(int userId, String encodedImage){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Profile_Image +
                " WHERE _userId = " + userId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdatePicture = "UPDATE " + TABLE_Profile_Image +
                    " SET " + COLUMN_Image + "=\"" + encodedImage + "\";";
            db.execSQL(UpdatePicture);
        }

        else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_UserID, userId);
            values.put(COLUMN_Image, encodedImage);
            db.insert(TABLE_Profile_Image, null, values);
        }
        c.close();
        db.close();
    }

    public void setTemp(String tempImage){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UserID, -1);
        values.put(COLUMN_Image, tempImage);
        db.insert(TABLE_Profile_Image, null, values);
        db.close();
    }

    public void deleteTemp(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_Profile_Image +
                " WHERE _userId = " + -1;
        db.execSQL(query);
        db.close();
    }

    public String getTemp(){
        String encodedImage = null;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Profile_Image +
                " WHERE _userId = " + -1;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        if(!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(COLUMN_Image)) != null) {
                encodedImage = c.getString(c.getColumnIndex(COLUMN_Image));
            }
        }
        c.close();
        db.close();
        return encodedImage;
    }


    public String getProfilePicture(int userId){
        String encodedImage = null;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Profile_Image +
                " WHERE _userId = " + userId;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.isAfterLast()) {
            System.out.println("NQMA NISHTO V BAZATA");
        }
        if(!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex(COLUMN_Image)) != null) {
                encodedImage = c.getString(c.getColumnIndex(COLUMN_Image));
            }
        }
        c.close();
        db.close();
        return encodedImage;
    }

    //Delete a list from the database
    /*public void deleteList(String listName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_USER_LISTS + " WHERE " + COLUMN_Name + "=\"" + listName + "\";");
    }*/

    // get the lists as list of objects
    public ArrayList<MyList> getLists(String currentUserUsername){
        // prepare the variables to store a row
        int id;
        int userId;
        String username="";
        String listName = "";
        int vis;
        String des = "";
        ArrayList<MyList> lists = new ArrayList<MyList>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USER_LISTS +
                       " WHERE " + COLUMN_Username + "=\"" + currentUserUsername + "\";";
        // cursor points to a location in the results
        Cursor c = db.rawQuery(query, null);
        // move to the first row
        c.moveToFirst();

        while (!c.isAfterLast()){

            id = c.getInt(c.getColumnIndex("_listId"));
            userId = c.getInt(c.getColumnIndex(COLUMN_UserId));
            if(c.getString(c.getColumnIndex("_username")) != null){
                username = c.getString(c.getColumnIndex("_username"));
            }
            if(c.getString(c.getColumnIndex("_name")) != null){
                listName = c.getString(c.getColumnIndex("_name"));
            }
            vis = c.getInt(c.getColumnIndex("_visibility"));
            if(c.getString(c.getColumnIndex("_description")) != null){
                des = c.getString(c.getColumnIndex("_description"));
            }

            MyList ul = new MyList(userId, username, listName, vis, des);
            ul.set_listId(id);
            lists.add(ul);
            c.moveToNext();
        }
        c.close();
        db.close();
        return lists;
    }

    public ArrayList<Item> getItems(int listID){
        // prepare the variables to store a row
        int id;
        int userId;
        String itemName = "";
        double rating;
        String des = "";
        String image = "";
        String creatorUsername = "";
        ArrayList<Item> itemsList = new ArrayList<Item>();

        SQLiteDatabase db = getWritableDatabase();
        // select the items of the selected list
        String query = "SELECT * FROM " + TABLE_Items  +
                " WHERE " + TABLE_Items + "._listId = " + listID;
        // cursor points to a location in the results
        Cursor c = db.rawQuery(query, null);
        // move to the first row
        c.moveToFirst();

        while (!c.isAfterLast()){

            id = c.getInt(c.getColumnIndex("_itemId"));
            userId = c.getInt(c.getColumnIndex(COLUMN_CreatorId));
            if(c.getString(c.getColumnIndex("_itemName")) != null){
                itemName = c.getString(c.getColumnIndex("_itemName"));
            }
            rating = c.getDouble(c.getColumnIndex("_rating"));
            if(c.getString(c.getColumnIndex("_description")) != null){
                des = c.getString(c.getColumnIndex("_description"));
            }

            if(c.getString(c.getColumnIndex(COLUMN_Creator)) != null){
                creatorUsername = c.getString(c.getColumnIndex(COLUMN_Creator));
            }

            if(c.getString(c.getColumnIndex(COLUMN_ItemImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ItemImage));
            }

            Item item = new Item(listID, userId, creatorUsername, itemName, rating, des, image);
            item.set_itemId(id);
            itemsList.add(item);
            c.moveToNext();
        }
        c.close();
        db.close();
        return itemsList;
    }
}