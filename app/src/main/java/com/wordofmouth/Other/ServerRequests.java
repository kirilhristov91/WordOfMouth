package com.wordofmouth.Other;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wordofmouth.Interfaces.GetFeedbackResponse;
import com.wordofmouth.Interfaces.GetItemId;
import com.wordofmouth.Interfaces.GetItems;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.Interfaces.GetPasswordResetResponse;
import com.wordofmouth.Interfaces.GetRateResponce;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.Interfaces.GetUsernames;
import com.wordofmouth.Interfaces.GetUsers;
import com.wordofmouth.Interfaces.SendInviteResponse;
import com.wordofmouth.ObjectClasses.Item;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.ObjectClasses.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerRequests {

    private static ServerRequests INSTANCE = null;
    private static final int CONNECTION_TIMEOUT = 1000 * 5;
    private static final String SERVER_ADDRESS = "http://wordofmouth.netau.net/";


    public static synchronized ServerRequests getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new ServerRequests(context.getApplicationContext());
        }
        return INSTANCE;
    }

    // Constructor
    private ServerRequests(Context context){}

    // methods
    public void storeUserDataInBackground(User user, String gcmId, GetUserCallback userCallback){
        new StoreUserDataAsyncTask(user, gcmId, userCallback).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallback userCallback){
        new FetchUserDataAsyncTask(user, userCallback).execute();
    }

    public void UploadProfilePictureAsyncTask(String username, String image, GetUserCallback userCallback){
        new UploadProfilePictureAsyncTask(username, image, userCallback).execute();
    }

    public void UploadListAsyncTask(MyList list, GetListId getListId){
        new UploadListAsyncTask(list, getListId).execute();
    }

    public void UploadItemAsyncTask(Item item, GetItemId getItemId){
        new UploadItemAsyncTask(item, getItemId).execute();
    }

    public void fetchUsersInBackground(String requestedName, int currentUserId, GetUsers getUsers){
        new FetchUsersAsyncTask(requestedName, currentUserId, getUsers).execute();
    }

    public void inviteInBackground(int listId, int currentUserId, int invitedUserId, SendInviteResponse sendInviteResponse){
        new inviteAsyncTask(listId, currentUserId, invitedUserId, sendInviteResponse).execute();
    }

    public void downloadListInBackgroudn(int listId, int userId, GetListId getListId){
        new downloadListAsyncTask(listId, userId, getListId).execute();
    }

    public void downloadItemsInBackgroudn(int listId, GetItems getItems){
        new downloadItemsAsyncTask(listId, getItems).execute();
    }

    public void downloadNewItemInBackgroudn(int itemId, GetItemId getItemId){
        new downloadNewItemAsyncTask(itemId, getItemId).execute();
    }

    public void rateInBackground(int listId, int itemId, int userId, double rating, GetRateResponce getRateResponce){
        new rateInBackgroundAsyncTask(listId, itemId, userId, rating, getRateResponce).execute();
    }

    public void sendFeedbackInBackground(String feedback, GetFeedbackResponse getFeedbackResponse){
        new sendFeedbackAsyncTask(feedback, getFeedbackResponse).execute();
    }

    public void downloadUsernamesInBackground(int listId, GetUsernames getUsernames){
        new downloadUsernamesAsyncTask(listId, getUsernames).execute();
    }

    public void resetPasswordInBackground(String email, GetPasswordResetResponse getPasswordResetResponse){
        new resetPasswordAsyncTask(email, getPasswordResetResponse).execute();
    }

    // method to encode the data needed to be sent o the server
    private static String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(data.get(key),"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");
            sb.append(key + "=" + value);
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    private static class StoreUserDataAsyncTask extends AsyncTask<Void, Void, User>{
        User user;
        GetUserCallback userCallback;
        String gcmId;

        public StoreUserDataAsyncTask(User user, String gcmId, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
            this.gcmId = gcmId;
        }


        @Override
        protected User doInBackground(Void... params) {
            // prepare the data to send
            Map<String,String> dataToSend = new HashMap<>();
            dataToSend.put("name",user.getName());
            dataToSend.put("gcmId", gcmId);
            dataToSend.put("email",user.getEmail());
            dataToSend.put("username", user.getUsername());
            dataToSend.put("password", user.getPassword());

            User returnedUser = null;
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "Register.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                //Writing dataToSend to outputstreamwriter
                writer.write(encodedStr);
                //Sending the data to the server
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();

                    //Just check to the values received in Logcat
                    Log.i("custom_Register_check", "The values received in the store part are as follows:");
                    Log.i("custom_check", line);

                    // if username exists
                    if (line.equals("Username Already Exists")) {
                        Log.i("ZAETO", "VLQZAH");
                        returnedUser = new User(-1, "Exists", "Exists", "Exists", "Exists");
                    }

                    if (line.equals("Email Already Exists")) {
                        returnedUser = new User(-1, "Email", "Email", "Email", "Email");
                    }

                    else {
                        JSONObject jResult = new JSONObject(line);
                        int id = jResult.getInt("id");
                        returnedUser = new User(id, user.getName(), user.getEmail(), user.getUsername(), user.getPassword());
                    }

                }

                else {
                    returnedUser = new User(-1, "Timeout", "Timeout", "Timeout", "Timeout");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    private static class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User>{
        User user;
        GetUserCallback userCallback;

        public FetchUserDataAsyncTask(User user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }


        @Override
        protected User doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            dataToSend.put("username", user.getUsername());
            dataToSend.put("password", user.getPassword());
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            User returnedUser = null;

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "FetchCredentials.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("custom_Login_check", "The values received are as follows:");
                    Log.i("custom_Login_check", line);

                    JSONObject jResult = new JSONObject(line);

                    if (jResult.length() == 0) {
                        returnedUser = null;
                    } else {
                        int id = jResult.getInt("id");
                        String gcmId = jResult.getString("gcmId");
                        String name = jResult.getString("name");
                        String email = jResult.getString("email");
                        returnedUser = new User(id, name, email, user.getUsername(), user.getPassword());
                        returnedUser.setGcmId(gcmId);
                    }
                }

                else {
                    returnedUser = new User(-1, "Timeout", "Timeout", "Timeout", "Timeout");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedUser;

        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    private static class UploadProfilePictureAsyncTask extends AsyncTask<Void, Void, User>{
        String username;
        GetUserCallback userCallback;
        String image;

        public UploadProfilePictureAsyncTask(String username, String image, GetUserCallback userCallback) {
            this.username = username;
            this.userCallback = userCallback;
            this.image = image;
        }


        @Override
        protected User doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            dataToSend.put("username", username);
            dataToSend.put("picture", image );

            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            User returnedUser = null;

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "updateProfilePicture.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                    Log.i("UploadPicture_check", "The values received:");
                    Log.i("custom_check", line);

                    if (line.equals("failure")) {
                        Log.i("Fail", "could not upload the picture");
                        returnedUser = new User(-1, "failure", "failure", "failure", "failure");
                    }
                }

                else {
                    returnedUser = new User(-1, "Timeout", "Timeout", "Timeout", "Timeout");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private static class UploadListAsyncTask extends AsyncTask<Void, Void, MyList>{
        MyList list;
        GetListId getListId;

        public UploadListAsyncTask(MyList list, GetListId getListId) {
            this.list = list;
            this.getListId = getListId;
        }

        @Override
        protected MyList doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer uId = list.getUserId();
            String userIdString = uId.toString();


            dataToSend.put("userId", userIdString);
            dataToSend.put("username", list.get_username());
            dataToSend.put("name", list.get_name());
            dataToSend.put("description", list.get_description());
            dataToSend.put("image", list.getImage());
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            MyList returnedList = null;

            try {
                URL url = new URL(SERVER_ADDRESS + "uploadList.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("custom_ListUpload_check", "The values received are as follows:");
                    Log.i("custom_ListUpload_check", line);

                    if (line.equals("You have already created a list with that name!\n")) {
                        returnedList = null;
                    } else {
                        JSONObject jResult = new JSONObject(line);

                        if (jResult.length() == 0) {
                            returnedList = null;
                        } else {
                            int id = jResult.getInt("id");
                            int userId = jResult.getInt("userId");
                            String username = jResult.getString("username");
                            String name = jResult.getString("name");
                            String description = jResult.getString("description");
                            String image = jResult.getString("image");
                            returnedList = new MyList(userId, username, name, description, image);
                            returnedList.set_listId(id);
                        }
                    }
                }

                else {
                    returnedList = new MyList(-1, "Timeout", "Timeout", "Timeout", "Timeout");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedList;
        }

        @Override
        protected void onPostExecute(MyList returnedList) {
            getListId.done(returnedList);
            super.onPostExecute(returnedList);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private static class UploadItemAsyncTask extends AsyncTask<Void, Void, Item>{
        Item item;
        GetItemId getItemId;

        public UploadItemAsyncTask(Item item, GetItemId getItemId) {
            this.item = item;
            this.getItemId = getItemId;
        }

        @Override
        protected Item doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer uId = item.get_creatorId();
            String userIdString = uId.toString();
            Integer listId = item.get_listId();
            String listIdString = listId.toString();
            Double rating = item.get_rating();
            String ratingString = rating.toString();

            dataToSend.put("userId", userIdString);
            dataToSend.put("listId", listIdString);
            dataToSend.put("username", item.get_creatorUsername());
            dataToSend.put("name", item.get_name());
            dataToSend.put("rating", ratingString);
            dataToSend.put("description", item.get_description());
            dataToSend.put("picture", item.get_itemImage());

            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            Item returnedItem = null;

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "uploadItem.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("custom_ItemUpload_check", "The values received are as follows:");
                    Log.i("custom_ItemUpload_check", line);

                    if (line.equals("An item with that name for that list already exists!\n")) {
                        returnedItem = null;
                    } else {
                        JSONObject jResult = new JSONObject(line);

                        if (jResult.length() == 0) {
                            returnedItem = null;
                        } else {
                            int id = jResult.getInt("id");
                            int lId = jResult.getInt("listId");
                            int creatorId = jResult.getInt("userId");
                            String username = jResult.getString("username");
                            String name = jResult.getString("name");
                            double r = jResult.getDouble("rating");
                            int ratingCounter = jResult.getInt("ratingCounter");
                            String description = jResult.getString("description");
                            String image = jResult.getString("picture");
                            returnedItem = new Item(lId, creatorId, username, name, rating, ratingCounter, description, image);
                            returnedItem.set_itemId(id);
                            returnedItem.setSeen(1);
                        }
                    }
                }
                else return new Item(-1, -1, "Timeout", "Timeout", -1, 1, "Timeout", "Timeout");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedItem;
        }

        @Override
        protected void onPostExecute(Item returnedItem) {
            getItemId.done(returnedItem);
            super.onPostExecute(returnedItem);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class FetchUsersAsyncTask extends AsyncTask<Void, Void, ArrayList<User>>{
        String name;
        GetUsers getUsers;
        int currentUserId;

        public FetchUsersAsyncTask(String query, int currentUserId, GetUsers getUsers) {
            this.name = query;
            this.currentUserId = currentUserId;
            this.getUsers = getUsers;
        }


        @Override
        protected ArrayList<User> doInBackground(Void... params) {

            ArrayList<User> returnedUsers = new ArrayList<User>();

            Map<String,String> dataToSend = new HashMap<>();
            Integer cuid = currentUserId;
            String cuidString = cuid.toString();

            dataToSend.put("name", name);
            dataToSend.put("id", cuidString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "fetchUsers.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("fetchUsers", "The values received are as follows:");
                    Log.i("fetchUsers", line);

                    if (!line.equals("null\n")) {
                        JSONArray array = new JSONArray(line);
                        System.out.println(array.length());
                        for (int n = 0; n < array.length(); n++) {
                            JSONObject jResult = array.getJSONObject(n);
                            System.out.println(jResult);
                            String user = jResult.getString("user");
                            jResult = new JSONObject(user);
                            int id = jResult.getInt("id");
                            String username = jResult.getString("username");
                            String name = jResult.getString("name");
                            String picture = jResult.getString("picture");
                            System.out.println(id + " " + username + " " + name);
                            returnedUsers.add(new User(id, name, username, picture));
                        }
                    }
                }
                else{
                    returnedUsers.add(new User(-1, "Timeout", "Timeout", "Timeout", "Timeout"));
                    return returnedUsers;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedUsers;
        }

        @Override
        protected void onPostExecute(ArrayList<User> returnedUsers) {
            getUsers.done(returnedUsers);
            super.onPostExecute(returnedUsers);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    private static class inviteAsyncTask extends AsyncTask<Void, Void, String>{
        //new inviteAsyncTask(listId, userId, sendInviteResponse).execute();
        int listId;
        int currentUserId;
        int invitedUserId;
        SendInviteResponse sendInviteResponse;

        public inviteAsyncTask(int listId, int currentUserId, int invitedUserId, SendInviteResponse sendInviteResponse) {
            this.listId = listId;
            this.currentUserId = currentUserId;
            this.invitedUserId = invitedUserId;
            this.sendInviteResponse = sendInviteResponse;
        }


        @Override
        protected String doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer lid = listId;
            String lidString = lid.toString();
            Integer cuid = currentUserId;
            String currentIdString = cuid.toString();
            Integer iuid = invitedUserId;
            String invitedIdString = iuid.toString();

            dataToSend.put("listId", lidString);
            dataToSend.put("currentUserId", currentIdString);
            dataToSend.put("invitedUserId", invitedIdString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            String response="";

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "invite.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("invite", "The values received are as follows:");
                    Log.i("invite", line);

                    response = line;
                }
                else {
                    return "Timeout";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            sendInviteResponse.done(response);
            super.onPostExecute(response);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class downloadListAsyncTask extends AsyncTask<Void, Void, MyList>{
        int listId;
        int userId;
        GetListId getListId;

        public downloadListAsyncTask(int listId, int userId, GetListId getListId) {
            this.listId = listId;
            this.userId = userId;
            this.getListId = getListId;
        }

        @Override
        protected MyList doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer lid = listId;
            String lidString = lid.toString();
            Integer uid = userId;
            String uidString = uid.toString();

            dataToSend.put("listId", lidString);
            dataToSend.put("userId", uidString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            MyList returnedList = null;

            try {
                URL url = new URL(SERVER_ADDRESS + "downloadList.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("downloadList", "The values received are as follows:");
                    Log.i("downloadList", line);

                    if (line.equals("Could not update table Shared\n")) {
                        return new MyList(-1, "UpdError", "UpdError", "UpdError", "UpdError");
                    }

                    else {
                        JSONObject jResult = new JSONObject(line);

                        //if (jResult.length() == 0) {
                          //  returnedList = null;
                        //} else {
                        int id = jResult.getInt("id");
                        int userId = jResult.getInt("userId");
                        String username = jResult.getString("username");
                        String name = jResult.getString("name");
                        String description = jResult.getString("description");
                        String image = jResult.getString("image");
                        returnedList = new MyList(userId, username, name, description, image);
                        returnedList.set_listId(id);
                        //}
                    }
                }

                else {
                    returnedList = new MyList(-1, "Timeout", "Timeout", "Timeout", "Timeout");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedList;
        }

        @Override
        protected void onPostExecute(MyList returnedList) {
            getListId.done(returnedList);
            super.onPostExecute(returnedList);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class downloadItemsAsyncTask extends AsyncTask<Void, Void, ArrayList<Item>>{
        int listId;
        GetItems getItems;

        public downloadItemsAsyncTask(int listId, GetItems getItems) {
            this.listId = listId;
            this.getItems = getItems;
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer lid = listId;
            String lidString = lid.toString();
            ArrayList<Item> items = new ArrayList<Item>();
            dataToSend.put("listId", lidString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            try {
                URL url = new URL(SERVER_ADDRESS + "downloadItems.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("downloadItems", "The values received are as follows:");
                    Log.i("downloadItems", line);

                    if (!line.equals("null\n")) {
                        JSONArray array = new JSONArray(line);
                        for (int n = 0; n < array.length(); n++) {
                            JSONObject jResult = array.getJSONObject(n);
                            String item = jResult.getString("item");
                            jResult = new JSONObject(item);
                            int id = jResult.getInt("id");
                            int lId = jResult.getInt("listId");
                            int creatorId = jResult.getInt("userId");
                            String username = jResult.getString("username");
                            String name = jResult.getString("name");
                            double r = jResult.getDouble("rating");
                            int ratingCounter = jResult.getInt("ratingCounter");
                            String description = jResult.getString("description");
                            String image = jResult.getString("picture");
                            Item itemToAdd = new Item(lId, creatorId, username, name, r, ratingCounter, description, image);
                            itemToAdd.set_itemId(id);
                            itemToAdd.setSeen(0);
                            items.add(itemToAdd);
                        }
                    }
                } else items.add(new Item(-1, -1, "Timeout", "Timeout", -1, 1, "Timeout", "Timeout"));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            getItems.done(items);
            super.onPostExecute(items);
        }
    }


    ////////////////////////////////////////////////////////////////////////
    private static class downloadNewItemAsyncTask extends AsyncTask<Void, Void, Item>{
        int itemId;
        GetItemId getItemId;

        public downloadNewItemAsyncTask(int itemId, GetItemId getItemId) {
            this.itemId = itemId;
            this.getItemId = getItemId;
        }

        @Override
        protected Item doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer iId = itemId;
            String itemIdString = iId.toString();

            dataToSend.put("itemId", itemIdString);

            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            Item returnedItem = null;

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "downloadNewItem.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("downloadNewItem", "The values received are as follows:");
                    Log.i("downloadNewItem", line);

                    JSONObject jResult = new JSONObject(line);

                    if (jResult.length() == 0) {
                        returnedItem = null;
                    } else {
                        int id = jResult.getInt("id");
                        int lId = jResult.getInt("listId");
                        int creatorId = jResult.getInt("userId");
                        String username = jResult.getString("username");
                        String name = jResult.getString("name");
                        double r = jResult.getDouble("rating");
                        int ratingCounter = jResult.getInt("ratingCounter");
                        String description = jResult.getString("description");
                        String image = jResult.getString("picture");
                        returnedItem = new Item(lId, creatorId, username, name, r, ratingCounter, description, image);
                        returnedItem.set_itemId(id);
                        returnedItem.setSeen(0);
                    }
                }
                else return new Item(-1, -1, "Timeout", "Timeout", -1, 1, "Timeout", "Timeout");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnedItem;
        }

        @Override
        protected void onPostExecute(Item returnedItem) {
            getItemId.done(returnedItem);
            super.onPostExecute(returnedItem);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class rateInBackgroundAsyncTask extends AsyncTask<Void, Void, String>{
        int listId;
        int userId;
        int itemId;
        double rating;
        GetRateResponce getRateResponce;

        public rateInBackgroundAsyncTask(int listId, int itemId, int userId, double rating, GetRateResponce getRateResponce) {
            this.listId = listId;
            this.itemId = itemId;
            this.userId = userId;
            this.rating = rating;
            this.getRateResponce = getRateResponce;
        }


        @Override
        protected String doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer lid = listId;
            String lidString = lid.toString();
            Integer iid = itemId;
            String iidString = iid.toString();
            Integer uid = userId;
            String uidString = uid.toString();
            Double r = rating;
            String rString = r.toString();

            dataToSend.put("listId", lidString);
            dataToSend.put("itemId", iidString);
            dataToSend.put("userId", uidString);
            dataToSend.put("rating", rString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            String response="";

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "updateRating.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("rate", "The values received are as follows:");
                    Log.i("rate", line);

                    response = line;
                }
                else {
                    return "Timeout";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            getRateResponce.done(response);
            super.onPostExecute(response);
        }
    }



    ////////////////////////////////////////////////////////////////////////////////
    private static class sendFeedbackAsyncTask extends AsyncTask<Void, Void, String>{
        String feedback;
        GetFeedbackResponse getFeedbackResponse;

        public sendFeedbackAsyncTask(String feedback, GetFeedbackResponse getFeedbackResponse) {
            this.feedback = feedback;
            this. getFeedbackResponse = getFeedbackResponse;
        }


        @Override
        protected String doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();

            dataToSend.put("feedback", feedback);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            String response="";

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "sendFeedback.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("sendFeedback", "The values received are as follows:");
                    Log.i("sendFeedback", line);

                    response = line;
                }
                else {
                    return "Timeout";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            getFeedbackResponse.done(response);
            super.onPostExecute(response);
        }
    }


    ////////////////////////////////////////////////////////////////////////
    private static class downloadUsernamesAsyncTask extends AsyncTask<Void, Void, ArrayList<String>>{
        int listId;
        GetUsernames getUsernames;

        public downloadUsernamesAsyncTask(int listId, GetUsernames getUsernames) {
            this.listId = listId;
            this.getUsernames = getUsernames;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();
            Integer lId = listId;
            String listIdString = lId.toString();

            dataToSend.put("listId", listIdString);

            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            ArrayList<String> usernames = new ArrayList<>();

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "downloadUsernames.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    line = sb.toString();
                    Log.i("downloadUsernames", "The values received are as follows:");
                    Log.i("downloadUsernames", line);

                    if (!line.equals("null\n")) {
                        JSONArray array = new JSONArray(line);
                        for (int n = 0; n < array.length(); n++) {
                            JSONObject jResult = array.getJSONObject(n);
                            String singleUsername = jResult.getString("singleUsername");
                            jResult = new JSONObject(singleUsername);
                            String username = jResult.getString("username");
                            usernames.add(username);
                        }
                    }
                }
                else usernames.add("Error: Timeout");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return usernames;
        }
        @Override
        protected void onPostExecute(ArrayList<String> usernames) {
            getUsernames.done(usernames);
            super.onPostExecute(usernames);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class resetPasswordAsyncTask extends AsyncTask<Void, Void, String>{
        String email;
        GetPasswordResetResponse getPasswordResetResponse;

        public resetPasswordAsyncTask(String email, GetPasswordResetResponse getPasswordResetResponse) {
            this.email = email;
            this.getPasswordResetResponse = getPasswordResetResponse;
        }


        @Override
        protected String doInBackground(Void... params) {

            Map<String,String> dataToSend = new HashMap<>();

            dataToSend.put("email", email);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            String response="";

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "resetPassword.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setConnectTimeout(CONNECTION_TIMEOUT);
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Data Read Procedure
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                    Log.i("resetPassword", "The values received are as follows:");
                    Log.i("resetPassword", line);

                    if(line.length()>0) {

                        response = line;
                    }
                    else response = "Timeout";
                }
                else {
                    return "Timeout";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            getPasswordResetResponse.done(response);
            super.onPostExecute(response);
        }
    }

}