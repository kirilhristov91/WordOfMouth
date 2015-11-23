package com.wordofmouth;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;
    EditText usernameField, passwordField;
    TextView registerNow;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
                User user = new User(username, password);
                authenticate(user);
                break;
            case R.id.registerNow:
                startActivity(new Intent(this, Register.class));
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
                    System.out.println("NA LOGIN SLED SERVER RESULT - " + returnedUser.username + " " + returnedUser.password);
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(Login.this);
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
