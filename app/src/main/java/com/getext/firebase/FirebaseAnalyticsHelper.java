package com.getext.firebase;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getext.BuildConfig;
import com.getext.keys.AppKeys;
import com.getext.room.database.GetextDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsHelper {
    private static FirebaseAnalyticsHelper mFirebaseAnalyticsHelper;
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static FirebaseAnalyticsHelper getInstance() {
        synchronized (GetextDatabase.class) {
            if (mFirebaseAnalyticsHelper == null) {
                mFirebaseAnalyticsHelper = new FirebaseAnalyticsHelper();
            }
        }

        return mFirebaseAnalyticsHelper;
    }

    private static FirebaseAnalytics getFirebaseAnalytics(@NonNull Context context) {
        synchronized (GetextDatabase.class) {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
            }
        }

        return mFirebaseAnalytics;
    }

    public void logEvent(@NonNull Context context, @NonNull String eventName, @Nullable Bundle properties) {
        if (properties == null)
            properties = new Bundle();

        properties.putInt(AppKeys.APP_VERSION_CODE, BuildConfig.VERSION_CODE);
        properties.putString(AppKeys.APP_VERSION_NAME, BuildConfig.VERSION_NAME);
        properties.putString(AppKeys.DEVICE_BRAND, Build.BRAND);
        properties.putString(AppKeys.DEVICE_MANUFACTURER, Build.MANUFACTURER);
        properties.putString(AppKeys.DEVICE_MODEL, Build.MODEL);
        properties.putString(AppKeys.DEVICE_OS_NAME, Build.VERSION.CODENAME);
        properties.putInt(AppKeys.DEVICE_OS_VERSION, Build.VERSION.SDK_INT);

        getFirebaseAnalytics(context).logEvent(eventName, properties);
    }
}