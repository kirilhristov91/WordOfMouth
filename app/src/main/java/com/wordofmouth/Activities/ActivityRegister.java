package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{

    Button registerButton;
    EditText regnameField, regusernameField, regemailField, regpasswordField;
    TextView loginme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


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

                User user = new User(id,name, email, username, generatedPassword);

                if(!isValidEmail(email)){
                    showError();
                }

                else {
                    registerUser(user);
                }

                break;

            case R.id.loginme:
                startActivity(new Intent(this, ActivityLogin.class));

        }
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

    private void showUserTakenError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(ActivityRegister.this);
        allertBuilder.setMessage("Username is already taken");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }


    private void registerUser(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if(returnedUser!=null){
                    showUserTakenError();
                }
                else startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
            }
        });
    }

}
