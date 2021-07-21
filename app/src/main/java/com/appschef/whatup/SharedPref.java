package com.appschef.whatup;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;


public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting_note", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getNightMode() {
        return sharedPreferences.getBoolean("night_mode", false);
    }

    public void setNightMode(Boolean state) {
        editor.putBoolean("night_mode", state);
        editor.apply();
    }
    public String getChats(String who) {
        return sharedPreferences.getString(who, "");
    }

    public void setChats(String who,String chats) {
        editor.putString(who,chats);
        editor.apply();
    }
    public Set<String> getWhos() {
        Set<String> strings=null;
        return sharedPreferences.getStringSet("who",strings);
    }

    public void setWhos(Set<String> strings) {
        editor.putStringSet("who",strings);
        editor.apply();
    }

    public void saveToPref(String str, Boolean b) {
        editor.putBoolean("in_code", b);
        editor.putString("code", str);
        editor.apply();
    }

    public String getCode() {
        return sharedPreferences.getString("code", "");
    }

    public Boolean getIn_Code() {
        return sharedPreferences.getBoolean("in_code", false);
    }

}
