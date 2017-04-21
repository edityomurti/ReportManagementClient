package com.solusi247.reportmanagement.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.solusi247.reportmanagement.activity.LoginActivity;
import com.solusi247.reportmanagement.util.SharedPreference;

import java.util.HashMap;

/**
 * Created by chy47 on 13/07/16.
 */
public class SessionManager {

    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context context;

    int PRIVATE_MODE = 1;

    private static final String PREFER_NAME = "ReportManagement";

    private static final String IS_USER_LOGIN = "IsUserLogggedIn";

    public static final String KEY_USER_ID = "user_id";

    public static final String KEY_NAME = "name";

    public static final String KEY_EMAIL = "email";

    public SessionManager (Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //create login session
    public void createUserLoginSession(String user_id, String name, String email) {
        //Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        //Storing user_id in pref
        editor.putString(KEY_USER_ID, user_id);

        //Storing name in pref
        editor.putString(KEY_NAME, name);

        //Storing email in pref
        editor.putString(KEY_EMAIL, email);

        //commit changes
        editor.commit();
    }

    public boolean checkLogin(){
        //Check login status
        if(!this.isUserLoggedIn()){
            //user not logged in, redirect him to Login Activity
            Intent i = new Intent(context, LoginActivity.class);

            //Closing all the Activities from stack
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            //Add new flag to start new aActivity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(i);

            return true;
        }
        return false;
    }

    public HashMap<String, String> getUserDetails(){

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        //user_id
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        //user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        //email
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        return user;
    }

    //Cleaer session details
    public void logoutUser(){
        //Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        //After logout redirect user to LoginActivity
        Intent i = new Intent(context, LoginActivity.class);

        //Closing all the activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Add new flag to start activity
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }

    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

//    public void setPreferences(Context context, String key, String value)
//    {
//        SharedPreferences.Editor editor = context.getSharedPreferences("Solusi247",Context.MODE_PRIVATE).edit();
//        editor.putString(key, value);
//        editor.commit();
//    }
//
//    public  String getPreferences(Context context, String key) {
//
//        SharedPreferences prefs = context.getSharedPreferences("Solusi247",	Context.MODE_PRIVATE);
//        String position = prefs.getString(key, "");
//        return position;
//    }

}
