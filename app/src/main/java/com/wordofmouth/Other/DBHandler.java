package com.wordofmouth.Other;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.Notification;

import java.util.ArrayList;


public class DBHandler extends SQLiteOpenHelper{

    private static DBHandler sInstance;

    //if updating the database change the version:
    private static final int DATABASE_VERSION = 22;
    private static final String DATABASE_NAME = "WOM.db";

    //Lists table
    public static final String TABLE_USER_LISTS = "UserLists";
    public static final String COLUMN_ID = "_listId";
    public static final String COLUMN_UserId = "_userId";
    public static final String COLUMN_Username = "_username";
    public static final String COLUMN_Name = "_name";
    public static final String COLUMN_Description = "_description";
    public static final String COLUMN_ListImage = "_listImage";
    public static final String COLUMN_HasNewContent = "_hasNewContent";

    // Items table
    public static final String TABLE_Items = "Items";
    public static final String COLUMN_ItemID = "_itemId";
    public static final String COLUMN_ListID = "_listId";
    public static final String COLUMN_CreatorId = "_creatorId";
    public static final String COLUMN_Creator = "_creatorUsername";
    public static final String COLUMN_ItemName = "_itemName";
    public static final String COLUMN_Rating = "_rating";
    public static final String COLUMN_RatingCounter = "_ratingCounter";
    public static final String COLUMN_ItemDescription = "_description";
    public static final String COLUMN_ItemImage = "_itemImage";

    //Profile image table (only local)
    public static final String TABLE_Profile_Image = "ProfileImage";
    public static final String COLUMN_UserID = "_userId";
    public static final String COLUMN_Image = "_image";

    //Notification table (only local)
    public static final String TABLE_Notifications = "Notifications";
    public static final String COLUMN_NotificationID = "_notificationId";
    public static final String COLUMN_NotificationListId = "_notificationListId";
    public static final String COLUMN_NotificationUserId = "_notificationUsertId";
    public static final String COLUMN_NotificationMsg = "_notificationMsg";
    public static final String COLUMN_NotificationDate = "_notificationDate";
    public static final String COLUMN_NotificationAccepted = "_notificationAccepted";

    /*public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }*/

    public static synchronized DBHandler getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
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


    /*public static DBHandler getDBHandlerForAsyncTask(){
        return sInstance;
    }*/

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CreateListTableQuery = "CREATE TABLE " + TABLE_USER_LISTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_UserId + " INTEGER, " +
                COLUMN_Username + " TEXT, " +
                COLUMN_Name + " TEXT, " +
                COLUMN_Description + " TEXT, " +
                COLUMN_ListImage + " TEXT, " +
                COLUMN_HasNewContent + " INTEGER, " +
                ");";

        String CreateItemsTableQuery = "CREATE TABLE " + TABLE_Items + "(" +
                COLUMN_ItemID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ListID + " INTEGER, " +
                COLUMN_CreatorId + " INTEGER, " +
                COLUMN_Creator + " TEXT, " +
                COLUMN_ItemName + " TEXT, " +
                COLUMN_Rating + " DOUBLE, " +
                COLUMN_RatingCounter + " INTEGER, " +
                COLUMN_ItemDescription + " TEXT, " +
                COLUMN_ItemImage + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_ListID + ") REFERENCES " +
                TABLE_USER_LISTS + "(" + COLUMN_ID + ")"+
                ");";

        String CreateProfileImageTableQuery = "CREATE TABLE " + TABLE_Profile_Image + "(" +
                COLUMN_UserID + " INTEGER PRIMARY KEY, " +
                COLUMN_Image + " TEXT " +
                ");";

        String CreateNoticationsTableQuery = "CREATE TABLE " + TABLE_Notifications + "(" +
                COLUMN_NotificationID + " INTEGER PRIMARY KEY, " +
                COLUMN_NotificationListId + " INTEGER, " +
                COLUMN_NotificationUserId + " INTEGER, " +
                COLUMN_NotificationMsg + " TEXT, " +
                COLUMN_NotificationDate + " TEXT, " +
                COLUMN_NotificationAccepted + " INTEGER " +
                ");";

        db.execSQL(CreateListTableQuery);
        db.execSQL(CreateItemsTableQuery);
        db.execSQL(CreateProfileImageTableQuery);
        db.execSQL(CreateNoticationsTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Items);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_LISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Profile_Image);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Notifications);
        onCreate(db);
    }

    //Add a new row to table lists
    public void addList(MyList ul){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, ul.get_listId());
        values.put(COLUMN_UserId, ul.getUserId());
        values.put(COLUMN_Username, ul.get_username());
        values.put(COLUMN_Name, ul.get_name());
        values.put(COLUMN_Description, ul.get_description());
        values.put(COLUMN_ListImage, ul.getImage());
        values.put(COLUMN_HasNewContent, ul.getHasNewContent());
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
        values.put(COLUMN_RatingCounter, i.getRatingCounter());
        values.put(COLUMN_Description, i.get_description());
        values.put(COLUMN_ItemImage, i.get_itemImage());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Items, null, values);
        db.close();
    }

    public void addMultipleItems(ArrayList<Item> items){
        SQLiteDatabase db = getWritableDatabase();
        for(int i = 0; i<items.size();i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_ItemID, items.get(i).get_itemId());
            values.put(COLUMN_ListID, items.get(i).get_listId());
            values.put(COLUMN_CreatorId, items.get(i).get_creatorId());
            values.put(COLUMN_Creator, items.get(i).get_creatorUsername());
            values.put(COLUMN_ItemName, items.get(i).get_name());
            values.put(COLUMN_Rating, items.get(i).get_rating());
            values.put(COLUMN_Description, items.get(i).get_description());
            values.put(COLUMN_ItemImage, items.get(i).get_itemImage());
            db.insert(TABLE_Items, null, values);
        }
        db.close();
    }


    public void updateRating(Item item){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Items +
                " WHERE " + COLUMN_ItemID + " = " + item.get_itemId();
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_Items +
                    " SET " + COLUMN_Rating + " = " + item.get_rating() + "," +
                    COLUMN_RatingCounter + " = " + item.getRatingCounter() +
                    " WHERE " + COLUMN_ItemID + " = " + item.get_itemId() + ";";
            db.execSQL(UpdateRating);
        }
        c.close();
        db.close();
    }

    public void updateHasNewContent(int listId, int toPut){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USER_LISTS +
                " WHERE " + COLUMN_ID + " = " + listId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_USER_LISTS +
                    " SET " + COLUMN_HasNewContent + " = " + toPut +
                    " WHERE " + COLUMN_ID + " = " + listId + ";";
            db.execSQL(UpdateRating);
        }
        c.close();
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

    public void addNotification(Notification notification){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NotificationListId, notification.getListId());
        values.put(COLUMN_NotificationUserId, notification.getUserId());
        values.put(COLUMN_NotificationMsg, notification.getMsg());
        values.put(COLUMN_NotificationDate, notification.getDate());
        values.put(COLUMN_NotificationAccepted, notification.getAccepted());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Notifications, null, values);
        db.close();
    }

    public ArrayList<Notification> getNotifications(int userId){
        ArrayList<Notification> notificationsList= new ArrayList<Notification>();
        int id;
        int lid;
        int uid;
        String message="";
        String date = "";
        int accepted;

        SQLiteDatabase db = getWritableDatabase();
        String get_not = "SELECT * FROM " + TABLE_Notifications +
                " WHERE " + COLUMN_NotificationUserId + " = " + userId +
                " ORDER BY " + COLUMN_NotificationID + " DESC";
        Cursor c = db.rawQuery(get_not, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            id = c.getInt(c.getColumnIndex(COLUMN_NotificationID));
            lid = c.getInt(c.getColumnIndex(COLUMN_NotificationListId));
            uid = c.getInt(c.getColumnIndex(COLUMN_NotificationUserId));
            if(c.getString(c.getColumnIndex(COLUMN_NotificationMsg)) != null){
                message = c.getString(c.getColumnIndex(COLUMN_NotificationMsg));
            }

            if(c.getString(c.getColumnIndex(COLUMN_NotificationDate)) != null){
                date = c.getString(c.getColumnIndex(COLUMN_NotificationDate));
            }

            accepted = c.getInt(c.getColumnIndex(COLUMN_NotificationAccepted));

            Notification n = new Notification(lid, uid, message, date, accepted);
            n.setId(id);
            notificationsList.add(n);
            c.moveToNext();
        }
        c.close();
        db.close();
        return notificationsList;
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
        /*if (c.isAfterLast()) {
            System.out.println("NQMA NISHTO V BAZATA");
        }*/
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
        String image="";
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
            if(c.getString(c.getColumnIndex("_description")) != null){
                des = c.getString(c.getColumnIndex("_description"));
            }
            if(c.getString(c.getColumnIndex(COLUMN_ListImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ListImage));
            }

            MyList ul = new MyList(userId, username, listName, des, image);
            ul.set_listId(id);
            lists.add(ul);
            c.moveToNext();
        }
        c.close();
        db.close();
        return lists;
    }

    // get the lists as list of objects
    public ArrayList<MyList> getSharedLists(String currentUserUsername){
        int id;
        int userId;
        String username="";
        String listName = "";
        String image="";
        String des = "";
        ArrayList<MyList> lists = new ArrayList<MyList>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_USER_LISTS +
                " WHERE " + COLUMN_Username + "!=\"" + currentUserUsername + "\";";
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
            if(c.getString(c.getColumnIndex("_description")) != null){
                des = c.getString(c.getColumnIndex("_description"));
            }
            if(c.getString(c.getColumnIndex(COLUMN_ListImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ListImage));
            }

            MyList ul = new MyList(userId, username, listName, des, image);
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
        int ratingCounter;
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
            ratingCounter = c.getInt(c.getColumnIndex(COLUMN_RatingCounter));
            if(c.getString(c.getColumnIndex("_description")) != null){
                des = c.getString(c.getColumnIndex("_description"));
            }

            if(c.getString(c.getColumnIndex(COLUMN_Creator)) != null){
                creatorUsername = c.getString(c.getColumnIndex(COLUMN_Creator));
            }

            if(c.getString(c.getColumnIndex(COLUMN_ItemImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ItemImage));
            }

            Item item = new Item(listID, userId, creatorUsername, itemName, rating, ratingCounter, des, image);
            item.set_itemId(id);
            itemsList.add(item);
            c.moveToNext();
        }
        c.close();
        db.close();
        return itemsList;
    }


    public Item getItem(int itemId){
        // prepare the variables to store a row
        int id;
        int listId;
        int userId;
        String itemName = "";
        double rating;
        int ratingCounter;
        String des = "";
        String image = "";
        String creatorUsername = "";
        Item item = null;

        SQLiteDatabase db = getWritableDatabase();
        // select the items of the selected list
        String query = "SELECT * FROM " + TABLE_Items  +
                " WHERE " + COLUMN_ItemID + "=" + itemId;
        // cursor points to a location in the results
        Cursor c = db.rawQuery(query, null);
        // move to the first row
        c.moveToFirst();

        if (!c.isAfterLast()){

            id = c.getInt(c.getColumnIndex(COLUMN_ItemID));
            listId = c.getInt(c.getColumnIndex(COLUMN_ListID));
            userId = c.getInt(c.getColumnIndex(COLUMN_CreatorId));
            if(c.getString(c.getColumnIndex(COLUMN_Creator)) != null){
                creatorUsername = c.getString(c.getColumnIndex(COLUMN_Creator));
            }
            if(c.getString(c.getColumnIndex(COLUMN_ItemName)) != null){
                itemName = c.getString(c.getColumnIndex(COLUMN_ItemName));
            }
            rating = c.getDouble(c.getColumnIndex(COLUMN_Rating));
            ratingCounter = c.getInt(c.getColumnIndex(COLUMN_RatingCounter));
            if(c.getString(c.getColumnIndex(COLUMN_ItemDescription)) != null){
                des = c.getString(c.getColumnIndex(COLUMN_ItemDescription));
            }

            if(c.getString(c.getColumnIndex(COLUMN_ItemImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ItemImage));
            }

            item = new Item(listId, userId, creatorUsername, itemName, rating, ratingCounter, des, image);
            item.set_itemId(id);
        }
        c.close();
        db.close();
        return item;
    }
}