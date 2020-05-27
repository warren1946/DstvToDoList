package com.dstv.todolist.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferenceHelper {
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    // App Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;
    // Sharedpref file name
    private static final String PREF_NAME = "TodoPrefHelper";
    // All Shared Preferences Keys
    private static final String KEY_USE_DARK_THEME = "useDarkTheme";

    // Overloaded constructor
    public UserPreferenceHelper(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /*
    * Create theme mode
    * */
    public void CreateDarkTheme(boolean useDarkTheme){
        // Storing dark theme value as TRUE
        editor.putBoolean(KEY_USE_DARK_THEME, true);
        // commit changes
        editor.commit();
    }

    /*
    * Get theme
    * */
    public boolean UseDarkTheme(){
        return pref.getBoolean(KEY_USE_DARK_THEME, false);
    }
}
