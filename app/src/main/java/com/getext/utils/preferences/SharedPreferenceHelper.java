package com.getext.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getext.keys.AppKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceHelper {
    private static SharedPreferences sharedPreferences;
    private static Editor sharedPreferencesEditor;

    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getApplicationContext().getSharedPreferences(AppKeys.SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    private static Editor getSharedPreferencesEditor(@NonNull Context context) {
        if (sharedPreferencesEditor == null)
            sharedPreferencesEditor = getSharedPreferences(context).edit();
        return sharedPreferencesEditor;
    }

    public static void putString(@NonNull Context context, @NonNull String key, @Nullable String value) {
        getSharedPreferencesEditor(context).putString(key, value).apply();
    }

    @Nullable
    public static String getString(@NonNull Context context, @NonNull String key, @Nullable String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    public static void putStringSet(@NonNull Context context, @NonNull String key, @Nullable Set<String> values) {
        getSharedPreferencesEditor(context).putStringSet(key, values).apply();
    }

    @Nullable
    public static Set<String> getStringSet(@NonNull Context context, @NonNull String key, @Nullable Set<String> defValues) {
        return getSharedPreferences(context).getStringSet(key, defValues);
    }

    public static void putInt(@NonNull Context context, @NonNull String key, int value) {
        getSharedPreferencesEditor(context).putInt(key, value).apply();
    }

    public static int getInt(@NonNull Context context, @NonNull String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }

    public static void putLong(@NonNull Context context, @NonNull String key, long value) {
        getSharedPreferencesEditor(context).putLong(key, value).apply();
    }

    public static long getLong(@NonNull Context context, @NonNull String key, long defValue) {
        return getSharedPreferences(context).getLong(key, defValue);
    }

    public static void putFloat(@NonNull Context context, @NonNull String key, float value) {
        getSharedPreferencesEditor(context).putFloat(key, value).apply();
    }

    public static float getFloat(@NonNull Context context, @NonNull String key, float defValue) {
        return getSharedPreferences(context).getFloat(key, defValue);
    }

    public static void putBoolean(@NonNull Context context, @NonNull String key, boolean value) {
        getSharedPreferencesEditor(context).putBoolean(key, value).apply();
    }

    public static boolean getBoolean(@NonNull Context context, @NonNull String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    public static void put(@NonNull Context context, @NonNull HashMap<String, Object> preferences) {
        Set<String> keys = preferences.keySet();

        if (keys.size() == 0)
            return;

        Editor sharedPreferences = getSharedPreferencesEditor(context);

        for (String key : keys) {
            Object value = preferences.get(key);

            if (value instanceof String) {
                sharedPreferences.putString(key, (String) value);
            } else if (value instanceof Integer) {
                sharedPreferences.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                sharedPreferences.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                sharedPreferences.putFloat(key, (Float) value);
            } else if (value instanceof Boolean) {
                sharedPreferences.putBoolean(key, (Boolean) value);
            } else {
                sharedPreferences.putString(key, String.valueOf(value));
            }
        }

        sharedPreferences.apply();
    }

    @NonNull
    public static HashMap<String, Object> get(@NonNull Context context, @NonNull HashMap<String, Object> defPreferences) {
        Set<String> keys = defPreferences.keySet();
        HashMap<String, Object> preferences = new HashMap<>();

        if (keys.size() == 0)
            return preferences;

        SharedPreferences sharedPreferences = getSharedPreferences(context);

        for (String key : keys) {
            Object defValue = defPreferences.get(key);

            if (defValue instanceof String) {
                preferences.put(key, sharedPreferences.getString(key, (String) defValue));
            } else if (defValue instanceof Integer) {
                preferences.put(key, sharedPreferences.getInt(key, (Integer) defValue));
            } else if (defValue instanceof Long) {
                preferences.put(key, sharedPreferences.getLong(key, (Long) defValue));
            } else if (defValue instanceof Float) {
                preferences.put(key, sharedPreferences.getFloat(key, (Float) defValue));
            } else if (defValue instanceof Boolean) {
                preferences.put(key, sharedPreferences.getBoolean(key, (Boolean) defValue));
            } else {
                preferences.put(key, sharedPreferences.getString(key, String.valueOf(defValue)));
            }
        }

        return preferences;
    }

    public static Map<String, ?> getAll(@NonNull Context context) {
        return getSharedPreferences(context).getAll();
    }

    public static boolean contains(@NonNull Context context, @NonNull String key) {
        return getSharedPreferences(context).contains(key);
    }

    public static void remove(@NonNull Context context, @NonNull String key) {
        getSharedPreferencesEditor(context).remove(key).apply();
    }

    public static void clear(@NonNull Context context) {
        getSharedPreferencesEditor(context).clear().apply();
    }
}