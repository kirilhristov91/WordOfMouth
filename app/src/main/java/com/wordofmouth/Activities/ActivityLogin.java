package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
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
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        registerNow = (TextView) findViewById(R.id.registerNow);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(this);
        registerNow.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);

    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.loginButton:
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();


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
                System.out.println("LOGIN " + generatedPassword);

                User user = new User(username, generatedPassword);
                authenticate(user);
                break;
            case R.id.registerNow:
                startActivity(new Intent(this, ActivityRegister.class));
                break;
        }
    }


    private void authenticate(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if(returnedUser == null){
                    showError();
                }else{
                    System.out.println("NA LOGIN SLED SERVER RESULT - " + returnedUser.getUsername() + " " + returnedUser.getPassword());
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityLogin.this);
        allertBuilder.setMessage("Incorrect username or password");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void logUserIn(User user){
        userLocalStore.storeUserData(user);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
    }

}
