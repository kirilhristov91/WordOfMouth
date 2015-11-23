package com.wordofmouth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Register extends AppCompatActivity implements View.OnClickListener{

    Button registerButton;
    EditText regnameField, regusernameField, regemailField, regpasswordField;
    TextView loginme;

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
                int id=0;
                String name = regnameField.getText().toString();
                String email = regemailField.getText().toString();
                String username = regusernameField.getText().toString();
                String password = regpasswordField.getText().toString();

                User user = new User(id,name, email, username, password);
                registerUser(user);

                break;
            case R.id.loginme:
                startActivity(new Intent(this, Login.class));

        }
    }

    private void registerUser(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

}
