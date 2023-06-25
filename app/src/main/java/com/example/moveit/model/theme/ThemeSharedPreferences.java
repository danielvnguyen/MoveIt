package com.example.moveit.model.theme;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeSharedPreferences {
    public final SharedPreferences sharedPreferences;

    public ThemeSharedPreferences(Context context){
        sharedPreferences = context.getSharedPreferences( "PREFS", Context.MODE_PRIVATE );
    }

    public void putValue(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( key, value );
        editor.apply();
    }

    public String getValue(String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }
}
