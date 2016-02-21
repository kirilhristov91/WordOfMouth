package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;
    EditText usernameField, passwordField;
    TextView registerNow;
    TextView forgotPassword;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        registerNow = (TextView) findViewById(R.id.registerNow);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(this);
        registerNow.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        userLocalStore = UserLocalStore.getInstance(this);

    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.loginButton:

                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();
                if(username.equals("") || password.equals("")){
                    showError("Incorrect username or password");
                }

                else {
                    String generatedPassword = null;
                    try {
                        // Hash the password in MD5 format
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        md.update(password.getBytes());
                        byte[] bytes = md.digest();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < bytes.length; i++) {
                            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                        }
                        generatedPassword = sb.toString();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    System.out.println("LOGIN " + generatedPassword);

                    User user = new User(username, generatedPassword);
                    authenticate(user);
                }
                break;
            case R.id.registerNow:
                startActivity(new Intent(this, ActivityRegister.class));
                finish();
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this,ActivityPasswordReset.class));
                break;
        }
    }

    private void authenticate(User user){
        if(!isNetworkAvailable()){
            showError("Network error! Check your internet connection and try again!");
        }
        else {
            ServerRequests serverRequests = ServerRequests.getInstance(this);

            final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.show();

            serverRequests.fetchUserDataInBackground(user, new GetUserCallback() {
                @Override
                public void done(User returnedUser) {
                    progressDialog.dismiss();
                    if (returnedUser == null) {
                        showError("Incorrect username or password");
                    } else {
                        if (returnedUser.getUsername().equals("Timeout")) {
                            showError("Network error! Check your internet connection and try again!");
                        } else {
                            logUserIn(returnedUser);
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    private void logUserIn(User user){
        userLocalStore.storeUserData(user);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityLogin.this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }
}
