package com.wordofmouth.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;

import com.wordofmouth.Other.DBHandler;
import com.wordofmouth.Interfaces.GetListId;
import com.wordofmouth.ObjectClasses.MyList;
import com.wordofmouth.R;
import com.wordofmouth.Other.ServerRequests;
import com.wordofmouth.SharedPreferences.UserLocalStore;

public class ActivityAddList extends AppCompatActivity implements View.OnClickListener{

    EditText listNameField;
    EditText listDescriptionField;
    UserLocalStore userLocalStore;
    Spinner dropDown;
    Button createNewListButton;
    DBHandler dbHandler;

    private String dropDownChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dbHandler= DBHandler.getInstance(this);
        userLocalStore = new UserLocalStore(this);
        listNameField = (EditText) findViewById(R.id.listNameField);
        listDescriptionField = (EditText) findViewById(R.id.listDescriptionField);
        dropDown = (Spinner) findViewById(R.id.dropDown);
        createNewListButton = (Button) findViewById(R.id.createNewListButton);

        String[] items = new String[]{"private", "public"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(ActivityAddList.this, android.R.layout.simple_spinner_dropdown_item, items);
        dropDown.setAdapter(adapter);
        dropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dropDownChoice = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dropDownChoice = "private";
            }
        });

        createNewListButton.setOnClickListener(this);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {

            case R.id.createNewListButton:
                if(listNameField.getText().toString().equals("")){
                    showErrorEmptyField();
                }

                else {
                    int currentUserId = userLocalStore.getUserLoggedIn().getId();
                    String currentUserUsername = userLocalStore.getUserLoggedIn().getUsername();
                    int visibility;
                    if (dropDownChoice.equals("private")) visibility = 0;
                    else visibility = 1;
                    MyList list = new MyList(currentUserId, currentUserUsername, listNameField.getText().toString(), visibility, listDescriptionField.getText().toString());
                    ServerRequests serverRequests = new ServerRequests(this);
                    serverRequests.UploadListAsyncTask(list, new GetListId() {
                        @Override
                        public void done(MyList returnedList) {
                            if (returnedList != null) {
                                dbHandler.addList(returnedList);
                                closeActivity();
                            } else showError();
                        }
                    });
                }
        }
    }

    public void closeActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showError(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("You have already created a list with that name!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

    private void showErrorEmptyField(){
        AlertDialog.Builder allertBuilder = new AlertDialog.Builder(this);
        allertBuilder.setMessage("Please enter a name for the list!");
        allertBuilder.setPositiveButton("OK", null);
        allertBuilder.show();
    }

}