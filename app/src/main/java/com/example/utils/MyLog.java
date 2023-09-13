package com.example.utils;

import android.util.Log;

public class MyLog {
    public static final boolean isDebug = true;

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }
}
