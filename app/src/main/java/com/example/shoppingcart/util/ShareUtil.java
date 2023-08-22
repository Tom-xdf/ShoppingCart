package com.example.shoppingcart.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ShareUtil {

    private static ShareUtil mUtil;
    private SharedPreferences preferences;

    public static ShareUtil getInstance(Context ctx) {
        if (mUtil == null) {
            mUtil = new ShareUtil();
            mUtil.preferences = ctx.getSharedPreferences("shopping", Context.MODE_PRIVATE);
        }
        return mUtil;
    }

    public void writeBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

}