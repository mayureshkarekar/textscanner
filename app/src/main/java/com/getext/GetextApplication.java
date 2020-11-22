package com.getext;

import androidx.multidex.MultiDexApplication;

import com.getext.keys.AppKeys;
import com.getext.utils.logger.Logger;
import com.getext.utils.preferences.SharedPreferenceHelper;
import com.google.android.gms.ads.MobileAds;

public class GetextApplication extends MultiDexApplication {
    private final String TAG = GetextApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this, initializationStatus -> Logger.d(TAG, "Mobile ads initialized."));
        Logger.i(TAG, "Firebase Token: " + SharedPreferenceHelper.getString(this, AppKeys.FCM_TOKEN, null));
    }
}