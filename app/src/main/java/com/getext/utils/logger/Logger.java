package com.getext.utils.logger;

import android.util.Log;

import androidx.annotation.NonNull;

import com.getext.BuildConfig;

public class Logger {
    public static void v(@NonNull final String tag, @NonNull final String msg) {
        if (BuildConfig.DEBUG)
            Log.v(tag, msg);
    }

    public static void i(@NonNull final String tag, @NonNull final String msg) {
        if (BuildConfig.DEBUG)
            Log.i(tag, msg);
    }

    public static void d(@NonNull final String tag, @NonNull final String msg) {
        if (BuildConfig.DEBUG)
            Log.d(tag, msg);
    }

    public static void w(@NonNull final String tag, @NonNull final String msg) {
        if (BuildConfig.DEBUG)
            Log.w(tag, msg);
    }

    public static void e(@NonNull final String tag, @NonNull final String msg) {
        if (BuildConfig.DEBUG)
            Log.e(tag, msg);
    }
}