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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.wordofmouth.Interfaces.GetGCM;
import com.wordofmouth.Interfaces.GetUserCallback;
import com.wordofmouth.Other.Utilities;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.ObjectClasses.User;
import com.wordofmouth.SharedPreferences.UserLocalStore;
import java.io.IOException;

public class ActivityRegister extends AppCompatActivity implements View.OnClickListener{

    Button registerButton;
    EditText regnameField, regusernameField, regemailField, regpasswordField;
    TextView loginme;
    User user;
    Utilities utilities;
    LinearLayout registerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        utilities = Utilities.getInstance(this);
        regnameField = (EditText) findViewById(R.id.regnameField);
        regemailField = (EditText) findViewById(R.id.regemailField);
        regusernameField = (EditText) findViewById(R.id.regusernameField);
        regpasswordField = (EditText) findViewById(R.id.regpasswordField);
        registerButton = (Button) findViewById(R.id.registerButton);
        loginme = (TextView) findViewById(R.id.loginme);
        registerLayout = (LinearLayout) findViewById(R.id.registerLayout);
        registerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                utilities.hideKeyboard(v);
                return false;
            }
        });

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

                if(name.equals("") || email.equals("") || username.equals("") || password.equals("")){
                     showError("Please leave no field empty!");
                }

                else if(username.contains(" ")){
                     showError("username should be one word!");
                }

                else {
                    String generatedPassword = utilities.hashPassword(password);
                    user = new User(id, name, email, username, generatedPassword);

                    if (!isValidEmail(email)) {
                        showError("Invalid e-mail address!");
                    } else {
                        getGcmId();
                    }
                }
                break;

            case R.id.loginme:
                startActivity(new Intent(this, ActivityLogin.class));
                finish();
        }
    }

    private void getGcmId(){
        if(!isNetworkAvailable()){
            showError("Network error! Check your internet connection and try again!");
        }
        else {
            final ProgressDialog progressDialogGCM = new ProgressDialog(this,R.style.MyTheme);
            progressDialogGCM.setCancelable(false);
            progressDialogGCM.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            progressDialogGCM.show();
            GCMRequest gcmRequest = GCMRequest.getInstance(this);
            gcmRequest.getGCMidInBackground(new GetGCM() {
                @Override
                public void done(String gcmId) {
                    progressDialogGCM.dismiss();
                    registerUser(gcmId);
                }
            });
        }
    }

    private void registerUser(String gcmId){
        ServerRequests serverRequests = ServerRequests.getInstance(this);
        final ProgressDialog progressDialog = new ProgressDialog(this,R.style.MyTheme);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        progressDialog.show();
        serverRequests.storeUserDataInBackground(user, gcmId, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                progressDialog.dismiss();
                if (returnedUser != null) {
                    if (returnedUser.getUsername().equals("Timeout")) {
                        showError("Network error! Check your internet connection and try again!");
                    } else if (returnedUser.getUsername().equals("Exists")) {
                        showError("Username is already taken");
                    } else if (returnedUser.getUsername().equals("Email")) {
                        showError("This email was used for another registration");
                    } else {
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

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Get GcmId in Background
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
                } catch (IOException ex) {
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showError(String message){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage(message);
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }
}