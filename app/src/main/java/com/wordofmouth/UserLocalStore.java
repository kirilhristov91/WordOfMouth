package com.wordofmouth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {

    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context){
        userLocalDatabase = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserData(User user){
        SharedPreferences.Editor editor = userLocalDatabase.edit();
        editor.putInt("id", user.id);
        editor.putString("name", user.name);
        editor.putString("email", user.email);
        editor.putString("username", user.username);
        editor.putString("password", user.password);
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