package com.wordofmouth.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.wordofmouth.Interfaces.GetUser;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityLogin extends BaseActivity implements View.OnClickListener {

    Button loginButton;
    EditText usernameField, passwordField;
    TextView registerNow, forgotPassword;
    LinearLayout loginLayout;
    UserLocalStore userLocalStore;
    Utilities utilities;

    @Override
    public boolean usesToolbar() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        utilities = Utilities.getInstance(this);

        // link the GUI elements
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        registerNow = (TextView) findViewById(R.id.registerNow);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        loginButton = (Button) findViewById(R.id.loginButton);

        // set listeners to GUI elements
        loginLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });

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
                    // if the provided information is valid
                     // hash the password
                    String generatedPassword = utilities.hashPassword(password);
                     // create user object
                    User user = new User(username, generatedPassword);
                     // authenticate user
                    authenticate(user);
                }
                break;
            case R.id.registerNow:
                startActivity(new Intent(this, ActivityRegister.class));
                break;
            case R.id.forgotPassword:
                startActivity(new Intent(this,ActivityPasswordReset.class));
                break;
        }
    }

    private void authenticate(User user){
        // check for network
        if(!isNetworkAvailable()){
            showError("Network error! Check your internet connection and try again!");
        }
        else {
            ServerRequests serverRequests = ServerRequests.getInstance(this);

            // show progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialog.show();

            // check if the provided data matches the user data on the server
            serverRequests.fetchUserDataInBackground(user, new GetUser() {
                @Override
                public void done(User returnedUser) {
                    progressDialog.dismiss();
                    if (returnedUser == null) {
                        showError("Incorrect username or password");
                    } else {
                        if (returnedUser.getUsername().equals("Timeout")) {
                           showError("Network error! Check your internet connection and try again!");
                        } else {
                            // if the user information matches - sign the user in
                            logUserIn(returnedUser);
                        }
                    }
                }
            });
        }
    }

    private void logUserIn(User user){
        userLocalStore.storeUserData(user);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
        System.gc();
    }
}
