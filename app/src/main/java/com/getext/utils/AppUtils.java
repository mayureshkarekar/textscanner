package com.getext.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;

public class AppUtils {
    public static void copyTextToClipboard(@NonNull Context context, @NonNull String label, @NonNull String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
    }

    public static void openKeyboard(@NonNull Context context, @NonNull EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void closeKeyboardFromActivity(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null)
            view = new View(activity);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void closeKeyboardFromContext(@NonNull Context context, @NonNull View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDateFromTimestamp(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }
}