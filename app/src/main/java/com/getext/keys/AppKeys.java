package com.getext.keys;

import com.getext.BuildConfig;

public class AppKeys {
    // Shared preferences.
    public static final String SHARED_PREFERENCES_FILE_NAME = BuildConfig.APPLICATION_ID;
    public static final String FCM_TOKEN = "fcm_token";

    // Database.
    public static final String GETEXT_DATABASE_NAME = "getext_database";

    // Recognized text table.
    public static final String RECOGNIZED_TEXT_TABLE_NAME = "recognized_text";
    public static final String ID_COLUMN_NAME = "id";
    public static final String MODE_COLUMN_NAME = "mode";
    public static final String TEXT_COLUMN_NAME = "text";
    public static final String TIMESTAMP_COLUMN_NAME = "timestamp";

    // Recognition modes.
    public static final int RECOGNITION_MODE_CAMERA = 1;
    public static final int RECOGNITION_MODE_IMAGE = 2;
    public static final int RECOGNITION_MODE_EDIT = 3;

    // Miscellaneous keys.
    public static final String CAPTURED_IMAGE_PATH = "captured_image_path";
    public static final String PICKED_IMAGE_URI = "picked_image_uri";
    public static final String RECOGNIZED_TEXT_ID = ID_COLUMN_NAME;
    public static final String RECOGNIZED_TEXT = TEXT_COLUMN_NAME;
    public static final String RECOGNITION_MODE = MODE_COLUMN_NAME;

    // Firebase Analytics event names.
    public static final String SCAN_FROM_CAMERA = "scan_from_camera";
    public static final String SCAN_FROM_IMAGE = "scan_from_image";
    public static final String IMAGE_LOADING_FAILED = "image_loading_failed";
    public static final String EDIT_SCANNED_TEXT = "edit_scanned_text";
    public static final String COPY_SCANNED_TEXT = "copy_scanned_text";
    public static final String SHARE_SCANNED_TEXT = "share_scanned_text";
    public static final String DELETE_SCANNED_TEXT = "delete_scanned_text";
    public static final String DELETE_ALL_SCANNED_TEXTS = "delete_all_scanned_texts";
    public static final String EDIT_IMAGE = "edit_image";
    public static final String SCAN_TEXT_STARTED = "scan_text_started";
    public static final String SCAN_TEXT_COMPLETED = "scan_text_completed";
    public static final String PERMISSION_RESULT = "permission_result";
    public static final String SAVED_AS_TXT = "saved_as_txt";
    public static final String SAVED_AS_PDF = "saved_as_pdf";
    public static final String EXCEPTION_CAUGHT = "exception_caught";
    public static final String RATE_APP = "rate_app";
    public static final String SHARE_APP = "share_app";
    public static final String READ_PRIVACY_POLICY = "read_privacy_policy";
    public static final String AD_SHOWN = "ad_shown";

    // Firebase Analytics event properties.
    public static final String APP_VERSION_CODE = "app_version_code";
    public static final String APP_VERSION_NAME = "app_version_name";
    public static final String DEVICE_BRAND = "device_brand";
    public static final String DEVICE_MANUFACTURER = "device_manufacturer";
    public static final String DEVICE_MODEL = "device_model";
    public static final String DEVICE_OS_NAME = "device_os_name";
    public static final String DEVICE_OS_VERSION = "device_os_version";
    public static final String TEXT_FOUND = "text_found";
    public static final String ACTION = "action";
    public static final String EXCEPTION_MESSAGE = "exception_message";
    public static final String SCREEN_NAME = "screen_name";
    public static final String AD_TYPE = "ad_type";

    // Firebase Analytics event property values.
    public static final String AD_TYPE_INTERSTITIAL = "ad_type_interstitial";
}