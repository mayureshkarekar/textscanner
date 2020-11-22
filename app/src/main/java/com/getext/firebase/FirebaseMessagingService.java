package com.getext.firebase;

import androidx.annotation.NonNull;

import com.getext.keys.AppKeys;
import com.getext.utils.logger.Logger;
import com.getext.utils.preferences.SharedPreferenceHelper;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private final String TAG = FirebaseMessagingService.class.getSimpleName();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SharedPreferenceHelper.putString(getApplicationContext(), AppKeys.FCM_TOKEN, token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logger.d(TAG, "Message from: " + remoteMessage.getFrom());
    }
}