package com.wordofmouth.Other;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.wordofmouth.Interfaces.GetItemId;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.Interfaces.GetUserCallback;
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

    ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 6;
    public static final String SERVER_ADDRESS = "http://wordofmouth.netau.net/";
    public static final String SENDER_ID = "260188412151";
    Context context;
    String gcmId;
    String msg;
    GoogleCloudMessaging gcm;

    // Constructor
    public ServerRequests(Context context){
        this.context = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    // methods
    public void storeUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallback).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallback userCallback){
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallback).execute();
    }

    public void UploadProfilePictureAsyncTask(String username, String image, GetUserCallback userCallback){
        progressDialog.show();
        progressDialog.setMessage("Uploading Profile Picture to Server...");
        new UploadProfilePictureAsyncTask(username, image, userCallback).execute();
    }

    public void UploadListAsyncTask(MyList list, GetListId getListId){
        progressDialog.show();
        progressDialog.setMessage("Uploading List to Server...");
        new UploadListAsyncTask(list, getListId).execute();
    }

    public void UploadItemAsyncTask(Item item, GetItemId getItemId){
        progressDialog.show();
        progressDialog.setMessage("Uploading Item to Server...");
        new UploadItemAsyncTask(item, getItemId).execute();
    }

    public void fetchUsersInBackground(String requestedName, int currentUserId, GetUsers getUsers){
        progressDialog.show();
        progressDialog.setMessage("Fetching users matching the name or username you entered...");
        new FetchUsersAsyncTask(requestedName, currentUserId, getUsers).execute();
    }

    public void inviteInBackground(int listId, int currentUserId, int invitedUserId, SendInviteResponse sendInviteResponse){
        progressDialog.show();
        progressDialog.setMessage("Inviting the selected user to the current list");
        new inviteAsyncTask(listId, currentUserId, invitedUserId, sendInviteResponse).execute();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // method to encode the data needed to be sent o the server
    public String getEncodedData(Map<String,String> data) {
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

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, User>{
        User user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }


        @Override
        protected User doInBackground(Void... params) {

            User returnedUser = null;

            if(!isNetworkAvailable()){
                System.out.println("VLQZAH TUKA na avalable network");
                return new User(-1, "Timeout", "Timeout", "Timeout", "Timeout");
            }
            // get gcm registration ID
            try {

                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                gcmId = null;
                gcmId = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + gcmId;
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();

            }

            // prepare the data to send
            Map<String,String> dataToSend = new HashMap<>();
            dataToSend.put("name",user.getName());
            dataToSend.put("gcmId", gcmId);
            dataToSend.put("email",user.getEmail());
            dataToSend.put("username", user.getUsername());
            dataToSend.put("password", user.getPassword());

            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "Register.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //con.setConnectTimeout(5000);

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
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User>{
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
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("custom_Login_check", "The values received are as follows:");
                Log.i("custom_Login_check",line);

                JSONObject jResult = new JSONObject(line);

                if (jResult.length() == 0){
                    returnedUser = null;
                }
                else{
                    int id = jResult.getInt("id");
                    String gcmId = jResult.getString("gcmId");
                    String name = jResult.getString("name");
                    String email = jResult.getString("email");
                    returnedUser = new User(id,name, email, user.getUsername(), user.getPassword());
                    returnedUser.setGcmId(gcmId);
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
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    public class UploadProfilePictureAsyncTask extends AsyncTask<Void, Void, User>{
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
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                Log.i("UploadPicture_check", "The values received:");
                Log.i("custom_check",line);

                if(line.equals("failure")){
                    Log.i("Fail","could not upload the picture");
                    returnedUser = new User(-1,"failure","failure","failure","failure");
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
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    public class UploadListAsyncTask extends AsyncTask<Void, Void, MyList>{
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
            Integer visibility = list.get_visibility();
            String visibilityString = visibility.toString();


            dataToSend.put("userId", userIdString);
            dataToSend.put("username", list.get_username());
            dataToSend.put("name", list.get_name());
            dataToSend.put("visibility", visibilityString);
            dataToSend.put("description", list.get_description());
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;
            MyList returnedList = null;

            try {
                URL url = new URL(SERVER_ADDRESS + "uploadList.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("custom_ListUpload_check", "The values received are as follows:");
                Log.i("custom_ListUpload_check",line);

                if(line.equals("You have already created a list with that name!\n")){
                    returnedList = null;
                }

                else {
                    JSONObject jResult = new JSONObject(line);

                    if (jResult.length() == 0) {
                        returnedList = null;
                    } else {
                        int id = jResult.getInt("id");
                        int userId = jResult.getInt("userId");
                        String username = jResult.getString("username");
                        String name = jResult.getString("name");
                        int vis = jResult.getInt("visibility");
                        String description = jResult.getString("description");
                        returnedList = new MyList(userId, username, name, vis, description);
                        returnedList.set_listId(id);
                    }
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
            progressDialog.dismiss();
            getListId.done(returnedList);
            super.onPostExecute(returnedList);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    public class UploadItemAsyncTask extends AsyncTask<Void, Void, Item>{
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
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                //Data Read Procedure
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("custom_ListUpload_check", "The values received are as follows:");
                Log.i("custom_ListUpload_check",line);

                if(line.equals("An item with that name for that list already exists!\n")){
                    returnedItem = null;
                }

                else {
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
                        String description = jResult.getString("description");
                        String image = jResult.getString("picture");
                        returnedItem = new Item(lId, creatorId, username, name, rating, description, image);
                        returnedItem.set_itemId(id);
                    }
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
            return returnedItem;
        }

        @Override
        protected void onPostExecute(Item returnedItem) {
            progressDialog.dismiss();
            getItemId.done(returnedItem);
            super.onPostExecute(returnedItem);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    public class FetchUsersAsyncTask extends AsyncTask<Void, Void, ArrayList<User>>{
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

            Map<String,String> dataToSend = new HashMap<>();
            Integer cuid = currentUserId;
            String cuidString = cuid.toString();

            dataToSend.put("name", name);
            dataToSend.put("id", cuidString);
            String encodedStr = getEncodedData(dataToSend);
            BufferedReader reader = null;

            ArrayList<User> returnedUsers = new ArrayList<User>();

            //Connection Handling
            try {
                URL url = new URL(SERVER_ADDRESS + "fetchUsers.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                //Data Read Procedure
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("fetchUsers", "The values received are as follows:");
                Log.i("fetchUsers",line);

                if(!line.equals("null\n")) {
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
            progressDialog.dismiss();
            getUsers.done(returnedUsers);
            super.onPostExecute(returnedUsers);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    public class inviteAsyncTask extends AsyncTask<Void, Void, String>{
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
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(encodedStr);
                writer.flush();

                //Data Read Procedure
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                line = sb.toString();
                Log.i("fetchUsers", "The values received are as follows:");
                Log.i("fetchUsers",line);

                response = line;
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
            progressDialog.dismiss();
            sendInviteResponse.done(response);
            super.onPostExecute(response);
        }
    }
}