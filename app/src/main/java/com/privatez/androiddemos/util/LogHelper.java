package com.privatez.androiddemos.util;

import android.util.Log;

/**
 * Created by private on 2017/2/8.
 */

public class LogHelper {
    public static final String TAG = "lhy";

    public static boolean isDebug = true;

    private LogHelper() {

    }

    public static void log(String msg) {
        if (isDebug) {
            log(TAG, msg);
        }
    }

    public static void log(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }
}
