package com.wordofmouth;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ServerRequests {

    ProgressDialog progressDialog;
    //public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://wordofmouth.netau.net/";

    public ServerRequests(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

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

            Map<String,String> dataToSend = new HashMap<>();
            dataToSend.put("name",user.getName());
            dataToSend.put("email",user.getEmail());
            dataToSend.put("username", user.getUsername());
            dataToSend.put("password", user.getPassword());

            //Encoded String - we will have to encode string by our custom method (Very easy)
            String encodedStr = getEncodedData(dataToSend);

            //Will be used if we want to read some data from server
            BufferedReader reader = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "Register.php");
                //Opening the connection (Not setting or using CONNECTION_TIMEOUT)
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                //To enable inputting values using POST method
                //(Basically, after this we can write the dataToSend to the body of POST method)
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                //Writing dataToSend to outputstreamwriter
                writer.write(encodedStr);
                //Sending the data to the server
                writer.flush();

                //Data Read Procedure
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();

                //Just check to the values received in Logcat
                Log.i("custom_Register_check", "The values received in the store part are as follows:");
                Log.i("custom_check",line);

                // if username exists
                if(line.equals("Username Already Exists")){
                    Log.i("ZAETO","VLQZAH");
                    returnedUser = new User(-1,"Exists","Exists","Exists","Exists");
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

            //Same return null, but if you want to return the read string (stored in line)
            //then change the parameters of AsyncTask and return that type, by converting
            //the string - to say JSON or user in your case
            return returnedUser;

        }

        private String getEncodedData(Map<String,String> data) {
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

            //Encoded String - we will have to encode string by our custom method (Very easy)
            String encodedStr = getEncodedData(dataToSend);

            //Will be used if we want to read some data from server
            BufferedReader reader = null;

            User returnedUser = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "FetchCredentials.php");
                //Opening the connection (Not setting or using CONNECTION_TIMEOUT)
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                //To enable inputting values using POST method
                //(Basically, after this we can write the dataToSend to the body of POST method)
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                //Writing dataToSend to outputstreamwriter
                writer.write(encodedStr);
                //Sending the data to the server - This much is enough to send data to server
                //But to read the response of the server, you will have to implement the procedure below
                writer.flush();


                //Data Read Procedure - Basically reading the data comming line by line
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) { //Read till there is something available
                    sb.append(line + "\n");     //Reading and saving line by line - not all at once
                }
                line = sb.toString();           //Saving complete data received in string, you can do it differently
                //Just check to the values received in Logcat
                Log.i("custom_Login_check", "The values received are as follows:");
                Log.i("custom_Login_check",line);

                JSONObject jResult = new JSONObject(line);

                if (jResult.length() == 0){
                    returnedUser = null;
                }
                else{
                    int id = jResult.getInt("id");
                    String name = jResult.getString("name");
                    String email = jResult.getString("email");
                    returnedUser = new User(id,name, email, user.username, user.password);
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

            //Same return null, but if you want to return the read string (stored in line)
            //then change the parameters of AsyncTask and return that type, by converting
            //the string - to say JSON or user in your case
            return returnedUser;

        }

        private String getEncodedData(Map<String,String> data) {
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

            //Encoded String - we will have to encode string by our custom method (Very easy)
            String encodedStr = getEncodedData(dataToSend);

            //Will be used if we want to read some data from server
            BufferedReader reader = null;

            User returnedUser = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "updateProfilePicture.php");
                //Opening the connection (Not setting or using CONNECTION_TIMEOUT)
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                //To enable inputting values using POST method
                //(Basically, after this we can write the dataToSend to the body of POST method)
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                //Writing dataToSend to outputstreamwriter
                writer.write(encodedStr);
                //Sending the data to the server - This much is enough to send data to server
                //But to read the response of the server, you will have to implement the procedure below
                writer.flush();


                //Data Read Procedure - Basically reading the data comming line by line
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) { //Read till there is something available
                    sb.append(line);     //Reading and saving line by line - not all at once
                }
                line = sb.toString();           //Saving complete data received in string, you can do it differently
                //Just check to the values received in Logcat
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
                        reader.close();     //Closing the
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //Same return null, but if you want to return the read string (stored in line)
            //then change the parameters of AsyncTask and return that type, by converting
            //the string - to say JSON or user in your case
            return returnedUser;

        }

        private String getEncodedData(Map<String,String> data) {
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
            Integer creator = list.get_creatorId();
            String creatorString = creator.toString();
            Integer visibility = list.get_visibility();
            String visibilityString = visibility.toString();

            dataToSend.put("creatorId", creatorString);
            dataToSend.put("name", list.get_name());
            dataToSend.put("visibility", visibilityString);
            dataToSend.put("description", list.get_description());

            //Encoded String - we will have to encode string by our custom method (Very easy)
            String encodedStr = getEncodedData(dataToSend);

            //Will be used if we want to read some data from server
            BufferedReader reader = null;

            MyList returnedList = null;

            //Connection Handling
            try {
                //Converting address String to URL
                URL url = new URL(SERVER_ADDRESS + "uploadList.php");
                //Opening the connection (Not setting or using CONNECTION_TIMEOUT)
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //Post Method
                con.setRequestMethod("POST");
                //To enable inputting values using POST method
                //(Basically, after this we can write the dataToSend to the body of POST method)
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                //Writing dataToSend to outputstreamwriter
                writer.write(encodedStr);
                //Sending the data to the server - This much is enough to send data to server
                //But to read the response of the server, you will have to implement the procedure below
                writer.flush();


                //Data Read Procedure - Basically reading the data comming line by line
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String line;
                while((line = reader.readLine()) != null) { //Read till there is something available
                    sb.append(line + "\n");     //Reading and saving line by line - not all at once
                }
                line = sb.toString();           //Saving complete data received in string, you can do it differently
                //Just check to the values received in Logcat
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
                        int creatorId = jResult.getInt("creatorId");
                        String name = jResult.getString("name");
                        int vis = jResult.getInt("visibility");
                        String description = jResult.getString("description");
                        returnedList = new MyList(creatorId, name, vis, description);
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

            //Same return null, but if you want to return the read string (stored in line)
            //then change the parameters of AsyncTask and return that type, by converting
            //the string - to say JSON or user in your case
            return returnedList;

        }

        private String getEncodedData(Map<String,String> data) {
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

        @Override
        protected void onPostExecute(MyList returnedList) {
            progressDialog.dismiss();
            getListId.done(returnedList);
            super.onPostExecute(returnedList);
        }
    }


}

