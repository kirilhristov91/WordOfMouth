package com.wordofmouth.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.wordofmouth.ObjectClasses.User;

public class UserLocalStore {

    public static final String SP_NAME = "userDetails";

    public SharedPreferences getUserLocalDatabase() {
        return userLocalDatabase;
    }

    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context){
        userLocalDatabase = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserData(User user){
        SharedPreferences.Editor editor = userLocalDatabase.edit();
        editor.putInt("id", user.getId());
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("username", user.getUsername());
        editor.putString("password", user.getPassword());
        editor.apply();
    }

    public User getUserLoggedIn(){
        int id = userLocalDatabase.getInt("id", 0);
        String name = userLocalDatabase.getString("name", "");
        String email = userLocalDatabase.getString("email", "");
        String username = userLocalDatabase.getString("username", "");
        String password = userLocalDatabase.getString("password", "");

        User u = new User(id,name, email, username, password);
        return u;
    }

    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor editor = userLocalDatabase.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.apply();
    }

    public boolean getIfLoggedIn(){
        if (userLocalDatabase.getBoolean("loggedIn", false)){
            return true;
        }
        else return false;
    }

    public void clearUserData(){
        SharedPreferences.Editor editor = userLocalDatabase.edit();
        editor.clear();
        editor.apply();
    }
}