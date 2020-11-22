package com.getext.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getext.R;
import com.getext.firebase.FirebaseAnalyticsHelper;
import com.getext.keys.AppKeys;
import com.getext.repository.RecognizedTextRepository;
import com.getext.room.entity.RecognizedText;
import com.getext.utils.AppUtils;
import com.getext.utils.logger.Logger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class ScanResultActivity extends AppCompatActivity {
    private final String TAG = ScanResultActivity.class.getSimpleName();
    private final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    private boolean is_shown_storage_permission_rationale = false;
    private EditText etRecognizedText;
    private ExtendedFloatingActionButton fabDone;
    private Menu mMenu;
    private InterstitialAd mIAdScanResultActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_scan_result, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                etRecognizedText.setEnabled(true);
                etRecognizedText.requestFocus();
                AppUtils.openKeyboard(this, etRecognizedText);

                mMenu.findItem(R.id.menu_edit).setVisible(false);
                mMenu.findItem(R.id.menu_copy).setVisible(false);
                mMenu.findItem(R.id.menu_share).setVisible(false);
                mMenu.findItem(R.id.menu_save_as).setVisible(false);
                mMenu.findItem(R.id.menu_done).setVisible(true);

                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EDIT_SCANNED_TEXT, null);

                return true;
            }

            case R.id.menu_done: {
                etRecognizedText.setEnabled(false);

                mMenu.findItem(R.id.menu_edit).setVisible(true);
                mMenu.findItem(R.id.menu_copy).setVisible(true);
                mMenu.findItem(R.id.menu_share).setVisible(true);
                mMenu.findItem(R.id.menu_save_as).setVisible(true);
                mMenu.findItem(R.id.menu_done).setVisible(false);

                return true;
            }

            case R.id.menu_copy: {
                String recognizedText = etRecognizedText.getText().toString().trim();
                if (!TextUtils.isEmpty(recognizedText)) {
                    AppUtils.copyTextToClipboard(this, "TextScanner", recognizedText);
                    Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.COPY_SCANNED_TEXT, null);
                } else {
                    Toast.makeText(this, R.string.no_text_to_copy, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            case R.id.menu_share: {
                String recognizedText = etRecognizedText.getText().toString().trim();
                if (!TextUtils.isEmpty(recognizedText)) {
                    Intent shareTextIntent = new Intent(android.content.Intent.ACTION_SEND);
                    shareTextIntent.setType("text/plain");
                    shareTextIntent.putExtra(Intent.EXTRA_TEXT, recognizedText);
                    startActivity(Intent.createChooser(shareTextIntent, "Share scanned text"));
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SHARE_SCANNED_TEXT, null);
                } else {
                    Toast.makeText(this, R.string.no_text_to_share, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            case R.id.menu_save_as: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    showSaveDialog();
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        showSaveDialog();
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        is_shown_storage_permission_rationale = true;
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        showPermissionsRationaleDialog(this, R.drawable.ic_folder_white_36dp, getString(R.string.storage_permission_description), permissions, STORAGE_PERMISSION_REQUEST_CODE);
                    } else {
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST_CODE);
                    }
                }

                return true;
            }

            default: {
                Logger.e(TAG, "No valid case found for id: " + item.getItemId());
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = getIntent();
        String recognizedTextString = etRecognizedText.getText().toString().trim();
        if (!TextUtils.isEmpty(recognizedTextString)) {
            RecognizedTextRepository recognizedTextRepository = new RecognizedTextRepository(getApplication());
            RecognizedText recognizedText = new RecognizedText();
            recognizedText.setRecognizedText(recognizedTextString);
            recognizedText.setRecognitionMode(intent.getIntExtra(AppKeys.RECOGNITION_MODE, -1));
            recognizedText.setTimestamp(System.currentTimeMillis() / 1000);

            if (intent.hasExtra(AppKeys.RECOGNIZED_TEXT_ID)) {
                int recognizedTextId = intent.getIntExtra(AppKeys.RECOGNIZED_TEXT_ID, -1);
                recognizedText.setId(recognizedTextId);
                recognizedTextRepository.updateRecognizedText(recognizedText);
                Logger.d(TAG, "Updating the record: " + recognizedTextId);
            } else {
                recognizedTextRepository.insertRecognizedText(recognizedText);
                Logger.d(TAG, "Inserting new the record.");
            }

            if (!intent.hasExtra(AppKeys.RECOGNIZED_TEXT_ID)) {
                if (mIAdScanResultActivity != null && mIAdScanResultActivity.isLoaded()) {
                    mIAdScanResultActivity.show();
                    Bundle properties = new Bundle();
                    properties.putString(AppKeys.AD_TYPE, AppKeys.AD_TYPE_INTERSTITIAL);
                    properties.putString(AppKeys.SCREEN_NAME, ScanResultActivity.class.getSimpleName());
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.AD_SHOWN, properties);
                    Logger.d(TAG, "Interstitial ad shown.");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            boolean shouldProcess = true;
            if (permissions.length > 0) {
                Bundle properties = new Bundle();
                for (int i = 0; i < grantResults.length; i++) {
                    String permission = permissions[i].substring(permissions[i].lastIndexOf(".") + 1).toLowerCase();
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        properties.putBoolean(permission, true);
                    } else {
                        shouldProcess = false;
                        properties.putBoolean(permission, false);
                    }
                }

                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.PERMISSION_RESULT, properties);
            }

            if (shouldProcess) {
                showSaveDialog();
            } else {
                if (!is_shown_storage_permission_rationale) {
                    String requestDescription = getString(R.string.storage_permission_description) + " " + getString(R.string.tap_settings_permission_value, "Storage");
                    showPermissionsSettingsDialog(this, R.drawable.ic_folder_white_36dp, requestDescription);
                }
            }

            is_shown_storage_permission_rationale = false;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            Logger.e(TAG, "Invalid permission request code: " + requestCode);
        }
    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.tb_scan_result);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        etRecognizedText = findViewById(R.id.et_recognized_text);
        etRecognizedText.setText(getIntent().getStringExtra(AppKeys.RECOGNIZED_TEXT));
        fabDone = findViewById(R.id.fab_done);
        fabDone.setOnClickListener((v) -> finish());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById(R.id.sc_recognized_text).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY) {
                    fabDone.hide();
                } else {
                    fabDone.show();
                }
            });
        }

        mIAdScanResultActivity = new InterstitialAd(this);
        mIAdScanResultActivity.setAdUnitId(getString(R.string.ad_unit_interstitial));
        mIAdScanResultActivity.loadAd(new AdRequest.Builder().build());
    }

    public static void showPermissionsRationaleDialog(@NonNull Activity activity, @DrawableRes int drawableResId, @NonNull String requestDescription, @NonNull String[] permissions, int permissionRequestCode) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, R.style.CustomDialogStyle);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_permission_request, null);
        ((ImageView) dialogView.findViewById(R.id.iv_permission_icon)).setImageResource(drawableResId);
        ((TextView) dialogView.findViewById(R.id.tv_permission_description)).setText(requestDescription);
        ((TextView) dialogView.findViewById(R.id.tv_continue)).setText(R.string.continue_label);
        ((TextView) dialogView.findViewById(R.id.tv_cancel)).setText(R.string.not_now);

        dialogBuilder.setView(dialogView);
        AlertDialog permissionDialog = dialogBuilder.create();
        dialogView.findViewById(R.id.tv_continue).setOnClickListener(view -> {
            ActivityCompat.requestPermissions(activity, permissions, permissionRequestCode);
            permissionDialog.dismiss();
        });
        dialogView.findViewById(R.id.tv_cancel).setOnClickListener(view -> permissionDialog.dismiss());

        permissionDialog.show();
    }

    public static void showPermissionsSettingsDialog(@NonNull Activity activity, @DrawableRes int drawableResId, @NonNull String requestDescription) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity, R.style.CustomDialogStyle);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_permission_request, null);
        ((ImageView) dialogView.findViewById(R.id.iv_permission_icon)).setImageResource(drawableResId);
        ((TextView) dialogView.findViewById(R.id.tv_permission_description)).setText(requestDescription);
        ((TextView) dialogView.findViewById(R.id.tv_continue)).setText(R.string.settings);
        ((TextView) dialogView.findViewById(R.id.tv_cancel)).setText(R.string.not_now);

        dialogBuilder.setView(dialogView);
        AlertDialog permissionDialog = dialogBuilder.create();
        dialogView.findViewById(R.id.tv_continue).setOnClickListener(view -> {
            Intent appDetailsIntent = new Intent();
            appDetailsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            appDetailsIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
            activity.startActivity(appDetailsIntent);
            permissionDialog.dismiss();
        });
        dialogView.findViewById(R.id.tv_cancel).setOnClickListener(view -> permissionDialog.dismiss());

        permissionDialog.show();
    }

    private void showSaveDialog() {
        Builder dialogBuilder = new Builder(this, R.style.CustomDialogStyle);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_save_file, null);
        final EditText etFilename = dialogView.findViewById(R.id.et_filename);
        final RadioButton rbText = dialogView.findViewById(R.id.rb_text);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        final Button btnSave = dialogView.findViewById(R.id.btn_save);

        dialogBuilder.setView(dialogView);
        AlertDialog saveDialog = dialogBuilder.create();

        btnCancel.setOnClickListener((view) -> saveDialog.cancel());
        btnSave.setOnClickListener((view) -> {
            String fileName = etFilename.getText().toString().trim();
            if (!TextUtils.isEmpty(fileName)) {
                if (rbText.isChecked()) {
                    saveTxtFile(fileName);
                } else {
                    savePdfFile(fileName);
                }

                saveDialog.cancel();
            }
        });

        saveDialog.show();
    }

    private void saveTxtFile(@NonNull String fileName) {
        try {
            File directory = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/TextScanner"));
            if (directory.exists() || directory.mkdir()) {
                File textFile = new File(directory, fileName + ".txt");
                FileWriter fileWriter = new FileWriter(textFile);
                fileWriter.append(etRecognizedText.getText().toString().trim());
                fileWriter.flush();
                fileWriter.close();
                Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SAVED_AS_TXT, null);
            } else {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_to_create_file, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log("Handled Exception:" + e.getMessage());

            Logger.e(TAG, "Failed to save file: " + e.getMessage());

            Bundle properties = new Bundle();
            properties.putString(AppKeys.ACTION, "save_txt_file");
            properties.putString(AppKeys.EXCEPTION_MESSAGE, e.getMessage());
            properties.putString(AppKeys.SCREEN_NAME, ScanResultActivity.class.getSimpleName());
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
        }
    }

    private void savePdfFile(@NonNull String fileName) {
        try {
            File directory = new File((Environment.getExternalStorageDirectory().getAbsolutePath() + "/TextScanner"));
            if (directory.exists() || directory.mkdir()) {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TextScanner/" + fileName + ".pdf"));
                document.open();
                Paragraph paragraph = new Paragraph(etRecognizedText.getText().toString().trim());
                paragraph.setAlignment(Paragraph.ALIGN_MIDDLE);
                paragraph.setIndentationLeft(1);
                paragraph.setIndentationRight(1);
                document.add(paragraph);
                document.close();
                Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT).show();
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SAVED_AS_PDF, null);
            } else {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_to_create_file, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log("Handled Exception:" + e.getMessage());

            Logger.e(TAG, "Failed to save file: " + e.getMessage());

            Bundle properties = new Bundle();
            properties.putString(AppKeys.ACTION, "save_pdf_file");
            properties.putString(AppKeys.EXCEPTION_MESSAGE, e.getMessage());
            properties.putString(AppKeys.SCREEN_NAME, ScanResultActivity.class.getSimpleName());
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
        }
    }
}