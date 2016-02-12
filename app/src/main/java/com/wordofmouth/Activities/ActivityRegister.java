package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.wordofmouth.Interfaces.GetGCM;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{

    Button registerButton;
    EditText regnameField, regusernameField, regemailField, regpasswordField;
    TextView loginme;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regnameField = (EditText) findViewById(R.id.regnameField);
        regemailField = (EditText) findViewById(R.id.regemailField);
        regusernameField = (EditText) findViewById(R.id.regusernameField);
        regpasswordField = (EditText) findViewById(R.id.regpasswordField);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginme = (TextView) findViewById(R.id.loginme);

        registerButton.setOnClickListener(this);
        loginme.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.registerButton:
                //TODO set check for empty name field
                int id=0;
                String name = regnameField.getText().toString();
                String email = regemailField.getText().toString();
                String username = regusernameField.getText().toString();
                String password = regpasswordField.getText().toString();

                String generatedPassword = null;
                try {
                    // Create MessageDigest instance for MD5
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    //Add password bytes to digest
                    md.update(password.getBytes());
                    //Get the hash's bytes
                    byte[] bytes = md.digest();
                    //This bytes[] has bytes in decimal format;
                    //Convert it to hexadecimal format
                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i< bytes.length ;i++)
                    {
                        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    //Get complete hashed password in hex format
                    generatedPassword = sb.toString();
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }
                System.out.println("REGISTER " + generatedPassword);

                user = new User(id,name, email, username, generatedPassword);

                if(!isValidEmail(email)){
                    showError();
                }

                else {
                    getGcmId();
                }

                break;

            case R.id.loginme:
                startActivity(new Intent(this, ActivityLogin.class));

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityRegister.this);
        allertBuilder.setMessage("Invalid e-mail address!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showConnectionError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityRegister.this);
        allertBuilder.setMessage("Network error! Check your internet connection and try again!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showUserTakenError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityRegister.this);
        allertBuilder.setMessage("Username is already taken");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void getGcmId(){
        if(!isNetworkAvailable()){
            showConnectionError();
        }
        else {
            String gcmId = "";
            GCMRequest gcmRequest = GCMRequest.getInstance(this);
            gcmRequest.getGCMidInBackground(new GetGCM() {
                @Override
                public void done(String gcmId) {
                    registerUser(gcmId);
                }
            });
        }
    }

    private void registerUser(String gcmId){
        ServerRequests serverRequests = ServerRequests.getInstance(this);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        serverRequests.storeUserDataInBackground(user, gcmId, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                progressDialog.dismiss();
                if (returnedUser != null) {
                    if (returnedUser.getUsername().equals("Timeout")) {
                        showConnectionError();
                    } else if (returnedUser.getUsername().equals("Exists")){
                        showUserTakenError();
                    }
                    else{
                        login(returnedUser);
                    }
                }
            }
        });
    }


    private void login(User returnedUser){
        UserLocalStore userLocalStore = UserLocalStore.getInstance(this);
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

////////////////////////////////////////////
    private static class GCMRequest{

        static Context context;
        private static final String SENDER_ID = "260188412151";
        private static GCMRequest INSTANCE = null;

        public static synchronized GCMRequest getInstance(Context context){
            if(INSTANCE == null){
                INSTANCE = new GCMRequest(context.getApplicationContext());
            }
            return INSTANCE;
        }

        // Constructor
        private GCMRequest(Context context){
            this.context = context;
        }

        public void getGCMidInBackground(GetGCM getGCM){
            new getGcmIdAsyncTask(getGCM).execute();
        }

        private static class getGcmIdAsyncTask extends AsyncTask<Void, Void, String> {
            GetGCM getGCM;

            public getGcmIdAsyncTask(GetGCM getGCM) {
                this.getGCM = getGCM;
            }

            @Override
            protected String doInBackground(Void... params) {
                String gcmId = "";
                GoogleCloudMessaging gcm;
                // get gcm registration ID
                try {
                    gcm = GoogleCloudMessaging.getInstance(context);
                    gcmId = null;
                    gcmId = gcm.register(SENDER_ID);
                    //msg = "Device registered, registration ID=" + gcmId;
                } catch (IOException ex) {
                    //msg = "Error :" + ex.getMessage();
                    ex.printStackTrace();
                }
                return gcmId;
            }

            @Override
            protected void onPostExecute(String gcmId) {
                getGCM.done(gcmId);
                super.onPostExecute(gcmId);
            }

        }
    }
}