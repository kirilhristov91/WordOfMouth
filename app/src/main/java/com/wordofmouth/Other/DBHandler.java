package com.wordofmouth.Other;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.Notification;
import com.wordofmouth.ObjectClasses.Shared;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper{

    private static DBHandler sInstance;

    //if updating the database change the version:
    private static final int DATABASE_VERSION = 27;
    private static final String DATABASE_NAME = "WOM.db";

    //Lists table
    public static final String TABLE_List = "List";
    public static final String COLUMN_ID = "_listId";
    public static final String COLUMN_UserId = "_userId";
    public static final String COLUMN_Username = "_username";
    public static final String COLUMN_Name = "_name";
    public static final String COLUMN_Description = "_description";
    public static final String COLUMN_ListImage = "_listImage";
    public static final String COLUMN_HasNewContent = "_hasNewContent";

    // Items table
    public static final String TABLE_Item = "Item";
    public static final String COLUMN_ItemID = "_itemId";
    public static final String COLUMN_ListID = "_listId";
    public static final String COLUMN_CreatorId = "_creatorId";
    public static final String COLUMN_Creator = "_creatorUsername";
    public static final String COLUMN_ItemName = "_itemName";
    public static final String COLUMN_Rating = "_rating";
    public static final String COLUMN_RatingCounter = "_ratingCounter";
    public static final String COLUMN_ItemDescription = "_description";
    public static final String COLUMN_ItemImage = "_itemImage";
    public static final String COLUMN_Seen = "_seen";

    //Profile image table (only local)
    public static final String TABLE_Profile_Image = "ProfileImage";
    public static final String COLUMN_UserID = "_userId";
    public static final String COLUMN_Image = "_image";

    //Notification table (only local)
    public static final String TABLE_Notification = "Notification";
    public static final String COLUMN_NotificationID = "_notificationId";
    public static final String COLUMN_NotificationListId = "_notificationListId";
    public static final String COLUMN_NotificationMsg = "_notificationMsg";
    public static final String COLUMN_NotificationDate = "_notificationDate";
    public static final String COLUMN_NotificationAccepted = "_notificationAccepted";

    // Table SharedWith
    public static final String TABLE_SharedWith = "SharedWith";
    public static final String COLUMN_Key = "_id";
    public static final String COLUMN_SharedListId = "_listId";
    public static final String COLUMN_SharedWithUsername = "_username";


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

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CreateListTableQuery = "CREATE TABLE " + TABLE_List + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_UserId + " INTEGER, " +
                COLUMN_Username + " TEXT, " +
                COLUMN_Name + " TEXT, " +
                COLUMN_Description + " TEXT, " +
                COLUMN_ListImage + " TEXT, " +
                COLUMN_HasNewContent + " INTEGER " +
                ");";

        String CreateItemTableQuery = "CREATE TABLE " + TABLE_Item + "(" +
                COLUMN_ItemID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ListID + " INTEGER, " +
                COLUMN_CreatorId + " INTEGER, " +
                COLUMN_Creator + " TEXT, " +
                COLUMN_ItemName + " TEXT, " +
                COLUMN_Rating + " DOUBLE, " +
                COLUMN_RatingCounter + " INTEGER, " +
                COLUMN_ItemDescription + " TEXT, " +
                COLUMN_ItemImage + " TEXT, " +
                COLUMN_Seen + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_ListID + ") REFERENCES " +
                TABLE_List + "(" + COLUMN_ID + ")"+
                ");";

        String CreateProfileImageTableQuery = "CREATE TABLE " + TABLE_Profile_Image + "(" +
                COLUMN_UserID + " INTEGER PRIMARY KEY, " +
                COLUMN_Image + " TEXT " +
                ");";

        String CreateNotificationTableQuery = "CREATE TABLE " + TABLE_Notification + "(" +
                COLUMN_NotificationID + " INTEGER PRIMARY KEY, " +
                COLUMN_NotificationListId + " INTEGER, " +
                COLUMN_NotificationMsg + " TEXT, " +
                COLUMN_NotificationDate + " TEXT, " +
                COLUMN_NotificationAccepted + " INTEGER " +
                ");";

        String CreateSharedWithTableQuery = "CREATE TABLE " + TABLE_SharedWith + "(" +
                COLUMN_Key + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SharedListId + " INTEGER, " +
                COLUMN_SharedWithUsername + " TEXT " +
                ");";

        db.execSQL(CreateListTableQuery);
        db.execSQL(CreateItemTableQuery);
        db.execSQL(CreateProfileImageTableQuery);
        db.execSQL(CreateNotificationTableQuery);
        db.execSQL(CreateSharedWithTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Item);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_List);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Profile_Image);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Notification);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SharedWith);
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
        db.insert(TABLE_List, null, values);
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
        values.put(COLUMN_Seen, i.getSeen());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Item, null, values);
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
            values.put(COLUMN_Seen, items.get(i).getSeen());
            db.insert(TABLE_Item, null, values);
        }
        db.close();
    }

    public void addNotification(Notification notification){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NotificationListId, notification.getListId());
        values.put(COLUMN_NotificationMsg, notification.getMsg());
        values.put(COLUMN_NotificationDate, notification.getDate());
        values.put(COLUMN_NotificationAccepted, notification.getAccepted());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_Notification, null, values);
        db.close();
    }

    public void addUserToSharedWith(int listId, String username){
        ContentValues values = new ContentValues();
        values.put(COLUMN_SharedListId, listId);
        values.put(COLUMN_SharedWithUsername, username);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_SharedWith, null, values);
        db.close();
    }

    public void addMultipleUsersToSharedWith(int listId, ArrayList<String> usernames){
        SQLiteDatabase db = getWritableDatabase();
        for(int i = 0; i<usernames.size();i++){
            ContentValues values = new ContentValues();
            values.put(COLUMN_SharedListId, listId);
            values.put(COLUMN_SharedWithUsername, usernames.get(i));
            db.insert(TABLE_SharedWith, null, values);
        }
        db.close();
    }

    public ArrayList<Shared> getUsernames(){
        ArrayList<Shared> toReturn = new ArrayList<>();
        int listId;
        String username ="";
        SQLiteDatabase db = getWritableDatabase();
        String getUsernames = "SELECT * FROM " + TABLE_SharedWith;

        Cursor c = db.rawQuery(getUsernames, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {

            listId = c.getInt(c.getColumnIndex(COLUMN_SharedListId));
            if(c.getString(c.getColumnIndex(COLUMN_SharedWithUsername)) != null){
                username = c.getString(c.getColumnIndex(COLUMN_SharedWithUsername));
            }
            toReturn.add(new Shared(listId, username));
            c.moveToNext();
        }
        c.close();
        db.close();
        return toReturn;
    }

    public void updateRating(Item item){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Item +
                " WHERE " + COLUMN_ItemID + " = " + item.get_itemId();
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_Item +
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
        String query = "SELECT * FROM " + TABLE_List +
                " WHERE " + COLUMN_ID + " = " + listId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_List +
                    " SET " + COLUMN_HasNewContent + " = " + toPut +
                    " WHERE " + COLUMN_ID + " = " + listId + ";";
            db.execSQL(UpdateRating);
        }
        c.close();
        db.close();
    }

    public void updateAccepted(int notificationId){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Notification +
                " WHERE " + COLUMN_NotificationID + " = " + notificationId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_Notification +
                    " SET " + COLUMN_NotificationAccepted + " = " + 1 +
                    " WHERE " + COLUMN_NotificationID + " = " + notificationId + ";";
            db.execSQL(UpdateRating);
        }
        c.close();
        db.close();
    }


    public void updateSeen(int itemId){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Item +
                " WHERE " + COLUMN_ItemID + " = " + itemId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            String UpdateRating = "UPDATE " + TABLE_Item +
                    " SET " + COLUMN_Seen + " = " + 1 +
                    " WHERE " + COLUMN_ItemID + " = " + itemId + ";";
            db.execSQL(UpdateRating);
        }
        c.close();
        db.close();
    }

    public ArrayList<Notification> getNotifications(){
        ArrayList<Notification> notificationsList= new ArrayList<>();
        int id;
        int lid;
        String message="";
        String date = "";
        int accepted;

        SQLiteDatabase db = getWritableDatabase();
        String get_not = "SELECT * FROM " + TABLE_Notification +
                " ORDER BY " + COLUMN_NotificationID + " DESC";
        Cursor c = db.rawQuery(get_not, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            id = c.getInt(c.getColumnIndex(COLUMN_NotificationID));
            lid = c.getInt(c.getColumnIndex(COLUMN_NotificationListId));
            if(c.getString(c.getColumnIndex(COLUMN_NotificationMsg)) != null){
                message = c.getString(c.getColumnIndex(COLUMN_NotificationMsg));
            }

            if(c.getString(c.getColumnIndex(COLUMN_NotificationDate)) != null){
                date = c.getString(c.getColumnIndex(COLUMN_NotificationDate));
            }

            accepted = c.getInt(c.getColumnIndex(COLUMN_NotificationAccepted));

            Notification n = new Notification(lid, message, date, accepted);
            n.setId(id);
            notificationsList.add(n);
            c.moveToNext();
        }
        c.close();
        db.close();
        return notificationsList;
    }

    ////////////////////////////////////////////////////////////////////
    // Profile Picture methods
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

    public String getTemp(){
        String encodedImage = null;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Profile_Image +
                " WHERE " + COLUMN_UserId + " = " + -1;

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

    public void deleteTemp(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + TABLE_Profile_Image +
                " WHERE " + COLUMN_UserId + " = " + -1;
        db.execSQL(query);
        db.close();
    }

    public String getProfilePicture(int userId){
        String encodedImage = null;
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_Profile_Image +
                " WHERE " + COLUMN_UserId + " = " + userId;

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
    // end of Profile Picture methods
    ////////////////////////////////////////////////////////////////////

    // get the lists as list of objects
    public ArrayList<MyList> getLists(String currentUserUsername, int flag){
        // prepare the variables to store a row
        int id;
        int userId;
        String username="";
        String listName = "";
        String image="";
        String des = "";
        ArrayList<MyList> lists = new ArrayList<>();

        String myLists = "SELECT * FROM " + TABLE_List +
                       " WHERE " + COLUMN_Username + "=\"" + currentUserUsername + "\"" +
                       " ORDER BY " + COLUMN_ID + " DESC;";
        String sharedLists = "SELECT * FROM " + TABLE_List +
                       " WHERE " + COLUMN_Username + "!=\"" + currentUserUsername + "\"" +
                       " ORDER BY " + COLUMN_ID + " DESC;";

        String queryToExecute;
        if(flag == 0){
            queryToExecute = myLists;
        }
        else {
           queryToExecute = sharedLists;
        }

        SQLiteDatabase db = getWritableDatabase();
        // cursor points to a location in the results
        Cursor c = db.rawQuery(queryToExecute, null);
        // move to the first row
        c.moveToFirst();

        while (!c.isAfterLast()){

            id = c.getInt(c.getColumnIndex(COLUMN_ID));
            userId = c.getInt(c.getColumnIndex(COLUMN_UserId));
            if(c.getString(c.getColumnIndex(COLUMN_Username)) != null){
                username = c.getString(c.getColumnIndex(COLUMN_Username));
            }
            if(c.getString(c.getColumnIndex(COLUMN_Name)) != null){
                listName = c.getString(c.getColumnIndex(COLUMN_Name));
            }
            if(c.getString(c.getColumnIndex(COLUMN_Description)) != null){
                des = c.getString(c.getColumnIndex(COLUMN_Description));
            }
            if(c.getString(c.getColumnIndex(COLUMN_ListImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ListImage));
            }
            int hasNewC = c.getInt(c.getColumnIndex(COLUMN_HasNewContent));
            MyList ul = new MyList(userId, username, listName, des, image);
            ul.set_listId(id);
            ul.setHasNewContent(hasNewC);
            lists.add(ul);
            c.moveToNext();
        }
        c.close();
        db.close();
        return lists;
    }

    public ArrayList getSeens (int listID){
        Integer seen;
        ArrayList<Integer> toReturn= new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT " + COLUMN_Seen + " FROM " + TABLE_Item +
                " WHERE " + COLUMN_ListID + " = " + listID;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        while (!c.isAfterLast()){
            seen = c.getInt(c.getColumnIndex(COLUMN_Seen));
            toReturn.add(seen);
            c.moveToNext();
        }
        c.close();
        db.close();
        return toReturn;
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
        int seen;
        ArrayList<Item> itemsList = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        // select the items of the selected list
        String query = "SELECT * FROM " + TABLE_Item +
                " WHERE " + COLUMN_ListID + " = " + listID +
                " ORDER BY " + COLUMN_ItemID + " DESC;";
        // cursor points to a location in the results
        Cursor c = db.rawQuery(query, null);
        // move to the first row
        c.moveToFirst();

        while (!c.isAfterLast()){

            id = c.getInt(c.getColumnIndex(COLUMN_ItemID));
            userId = c.getInt(c.getColumnIndex(COLUMN_CreatorId));
            if(c.getString(c.getColumnIndex(COLUMN_ItemName)) != null){
                itemName = c.getString(c.getColumnIndex(COLUMN_ItemName));
            }
            rating = c.getDouble(c.getColumnIndex(COLUMN_Rating));
            ratingCounter = c.getInt(c.getColumnIndex(COLUMN_RatingCounter));
            if(c.getString(c.getColumnIndex(COLUMN_ItemDescription)) != null){
                des = c.getString(c.getColumnIndex(COLUMN_ItemDescription));
            }

            if(c.getString(c.getColumnIndex(COLUMN_Creator)) != null){
                creatorUsername = c.getString(c.getColumnIndex(COLUMN_Creator));
            }

            if(c.getString(c.getColumnIndex(COLUMN_ItemImage)) != null){
                image = c.getString(c.getColumnIndex(COLUMN_ItemImage));
            }
            seen = c.getInt(c.getColumnIndex(COLUMN_Seen));

            Item item = new Item(listID, userId, creatorUsername, itemName, rating, ratingCounter, des, image);
            item.set_itemId(id);
            item.setSeen(seen);
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
        int seen;
        Item item = null;

        SQLiteDatabase db = getWritableDatabase();
        // select the items of the selected list
        String query = "SELECT * FROM " + TABLE_Item +
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
            seen = c.getInt(c.getColumnIndex(COLUMN_Seen));
            item = new Item(listId, userId, creatorUsername, itemName, rating, ratingCounter, des, image);
            item.set_itemId(id);
            item.setSeen(seen);
        }
        c.close();
        db.close();
        return item;
    }
}