package com.spark.util;

import com.spark.bluetoothdemo.BuildConfig;

import android.util.Log;

/**
 * Created by WENFUMAN on 2014/11/20.
 */
public class Trace {
    public final static void e(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG)
            Log.e(tag, msg, tr);
    }

    public final static void e(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.e(tag, msg);
    }

    public final static void d(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg);
    }

    public final static void i(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.i(tag, msg);
    }

    public final static void w(String tag, String msg) {
        if (BuildConfig.DEBUG)
            Log.w(tag, msg);
    }

}
