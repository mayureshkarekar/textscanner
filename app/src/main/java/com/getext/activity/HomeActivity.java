package com.getext.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.getext.R;
import com.getext.activity.viewmodel.HomeActivityViewModel;
import com.getext.adapter.RecognizedTextAdapter;
import com.getext.firebase.FirebaseAnalyticsHelper;
import com.getext.keys.AppKeys;
import com.getext.room.entity.RecognizedText;
import com.getext.utils.AppUtils;
import com.getext.utils.logger.Logger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static android.widget.LinearLayout.VERTICAL;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, RecognizedTextAdapter.MenuItemClickListener {
    private final String TAG = HomeActivity.class.getSimpleName();
    private final int SCAN_FROM_CAMERA_REQUEST_CODE = AppKeys.RECOGNITION_MODE_CAMERA;
    private final int SCAN_FROM_IMAGE_REQUEST_CODE = AppKeys.RECOGNITION_MODE_IMAGE;
    public final int MAX_ADS_IN_LIST = 5;
    public static int totalListItems = 0;
    public static final int ITEMS_PER_AD = 4;
    private String mCapturedImageFilePath;
    private Animation animRotateClockWise, animRotateAntiClockWise, animScaleUp, animScaleDown;
    private FloatingActionButton fabShowHideScanMenus, fabScanFromCamera, fabScanFromImage;
    private RecyclerView rvRecognizedText;
    private HomeActivityViewModel homeActivityViewModel;
    private TextView tvScanFromCameraLabel, tvScanFromImageLabel, tvNoData;
    private List<AdView> adViewList;
    private boolean[] adViewIsLoadedStatusArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_rate_this_app: {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.getext")));
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.RATE_APP, null);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.unable_to_perform_this_action, Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            case R.id.menu_share_app: {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
                startActivity(Intent.createChooser(share, getString(R.string.share_this_app)));
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SHARE_APP, null);

                return true;
            }

            case R.id.menu_privacy_policy: {
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.READ_PRIVACY_POLICY, null);

                return true;
            }

            default: {
                Logger.e(TAG, "No valid case found for id: " + item.getItemId());
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SCAN_FROM_CAMERA_REQUEST_CODE: {
                    if (!TextUtils.isEmpty(mCapturedImageFilePath)) {
                        Intent imagePreviewActivityIntent = new Intent(this, ImagePreviewActivity.class);
                        imagePreviewActivityIntent.putExtra(AppKeys.RECOGNITION_MODE, AppKeys.RECOGNITION_MODE_CAMERA);
                        imagePreviewActivityIntent.putExtra(AppKeys.CAPTURED_IMAGE_PATH, mCapturedImageFilePath);
                        startActivity(imagePreviewActivityIntent);
                    } else {
                        Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                        Logger.e(TAG, "Image path null or empty.");
                        Bundle properties = new Bundle();
                        properties.putString(AppKeys.RECOGNITION_MODE, AppKeys.SCAN_FROM_CAMERA);
                        FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.IMAGE_LOADING_FAILED, properties);
                    }
                }
                break;

                case SCAN_FROM_IMAGE_REQUEST_CODE: {
                    if (data != null && data.getData() != null) {
                        Intent imagePreviewActivityIntent = new Intent(this, ImagePreviewActivity.class);
                        imagePreviewActivityIntent.putExtra(AppKeys.RECOGNITION_MODE, AppKeys.RECOGNITION_MODE_IMAGE);
                        imagePreviewActivityIntent.putExtra(AppKeys.PICKED_IMAGE_URI, data.getData());
                        startActivity(imagePreviewActivityIntent);
                    } else {
                        Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                        Logger.e(TAG, "Image intent data is null.");
                        Bundle properties = new Bundle();
                        properties.putString(AppKeys.RECOGNITION_MODE, AppKeys.SCAN_FROM_IMAGE);
                        FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.IMAGE_LOADING_FAILED, properties);
                    }
                }
                break;

                default: {
                    Logger.e(TAG, "Invalid request code: " + requestCode);
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_show_hide_scan_menus: {
                showHideScanMenus();
            }
            break;

            case R.id.tv_scan_from_camera_label:
            case R.id.fab_scan_from_camera: {
                hideScanMenus();
                startCaptureImageActivity();
            }
            break;

            case R.id.tv_scan_from_image_label:
            case R.id.fab_scan_from_image: {
                hideScanMenus();
                startPickImageActivity();
            }
            break;

            default: {
                Logger.e(TAG, "Click not handled for id: " + view.getId());
            }
            break;
        }
    }

    @Override
    public void onMenuItemClickListener(int itemId, @Nullable Object object) {
        switch (itemId) {
            case R.id.menu_copy: {
                if (object instanceof RecognizedText) {
                    RecognizedText recognizedText = (RecognizedText) object;
                    String scannedTextString = recognizedText.getRecognizedText();
                    AppUtils.copyTextToClipboard(HomeActivity.this, "TextScanner", scannedTextString);
                    Toast.makeText(HomeActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.COPY_SCANNED_TEXT, null);
                }
            }
            break;

            case R.id.menu_edit: {
                if (object instanceof RecognizedText) {
                    RecognizedText recognizedText = (RecognizedText) object;
                    int EDIT_REQUEST_CODE = AppKeys.RECOGNITION_MODE_EDIT;
                    Intent scanResultActivity = new Intent(this, ScanResultActivity.class);
                    scanResultActivity.putExtra(AppKeys.RECOGNIZED_TEXT_ID, recognizedText.getId());
                    scanResultActivity.putExtra(AppKeys.RECOGNIZED_TEXT, recognizedText.getRecognizedText());
                    scanResultActivity.putExtra(AppKeys.RECOGNITION_MODE, recognizedText.getRecognitionMode());
                    startActivityForResult(scanResultActivity, EDIT_REQUEST_CODE);
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EDIT_SCANNED_TEXT, null);
                }
            }
            break;

            case R.id.menu_delete: {
                if (object instanceof RecognizedText) {
                    RecognizedText recognizedText = (RecognizedText) object;
                    homeActivityViewModel.deleteRecognizedText(recognizedText);
                    FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.DELETE_SCANNED_TEXT, null);
                }
            }
            break;

            case R.id.menu_delete_all: {
                homeActivityViewModel.deleteAllRecognizedTexts();
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.DELETE_ALL_SCANNED_TEXTS, null);
            }
            break;

            default: {
                Logger.e(TAG, "Click not handled for id: " + itemId);
            }
            break;
        }
    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.tb_home);
        setSupportActionBar(toolbar);

        animRotateClockWise = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_clockwise);
        animRotateAntiClockWise = AnimationUtils.loadAnimation(this, R.anim.anim_rotate_anti_clockwise);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.anim_scale_up);
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.anim_scale_down);

        tvNoData = findViewById(R.id.tv_no_data);
        fabShowHideScanMenus = findViewById(R.id.fab_show_hide_scan_menus);
        tvScanFromCameraLabel = findViewById(R.id.tv_scan_from_camera_label);
        tvScanFromImageLabel = findViewById(R.id.tv_scan_from_image_label);
        fabScanFromCamera = findViewById(R.id.fab_scan_from_camera);
        fabScanFromImage = findViewById(R.id.fab_scan_from_image);
        fabShowHideScanMenus.setColorFilter(Color.WHITE);
        fabScanFromCamera.setColorFilter(Color.WHITE);
        fabScanFromImage.setColorFilter(Color.WHITE);
        fabShowHideScanMenus.setOnClickListener(this);
        tvScanFromCameraLabel.setOnClickListener(this);
        fabScanFromCamera.setOnClickListener(this);
        tvScanFromImageLabel.setOnClickListener(this);
        fabScanFromImage.setOnClickListener(this);

        initAds();

        RecognizedTextAdapter adapter = new RecognizedTextAdapter(this);
        adapter.setMenuItemClickListener(this);
        rvRecognizedText = findViewById(R.id.rv_recognized_text);
        rvRecognizedText.setAdapter(adapter);
        rvRecognizedText.addItemDecoration(new DividerItemDecoration(this, VERTICAL));
        rvRecognizedText.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (fabScanFromCamera.getVisibility() == VISIBLE) {
                        hideScanMenus();
                    }
                    fabShowHideScanMenus.hide();
                } else {
                    fabShowHideScanMenus.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        homeActivityViewModel = new ViewModelProvider(this).get(HomeActivityViewModel.class);
        homeActivityViewModel.getAllRecognizedTexts().observe(this, recognizedTextList -> updateRecognizedTextList(adapter, recognizedTextList));
    }

    private void showScanMenus() {
        fabShowHideScanMenus.startAnimation(animRotateClockWise);
        fabScanFromImage.startAnimation(animScaleUp);
        fabScanFromImage.setVisibility(View.VISIBLE);
        tvScanFromImageLabel.startAnimation(animScaleUp);
        tvScanFromImageLabel.setVisibility(View.VISIBLE);
        fabScanFromCamera.startAnimation(animScaleUp);
        fabScanFromCamera.setVisibility(View.VISIBLE);
        tvScanFromCameraLabel.startAnimation(animScaleUp);
        tvScanFromCameraLabel.setVisibility(View.VISIBLE);
    }

    private void hideScanMenus() {
        fabShowHideScanMenus.startAnimation(animRotateAntiClockWise);
        fabScanFromCamera.startAnimation(animScaleDown);
        fabScanFromCamera.setVisibility(View.GONE);
        tvScanFromImageLabel.startAnimation(animScaleDown);
        tvScanFromImageLabel.setVisibility(View.GONE);
        fabScanFromImage.startAnimation(animScaleDown);
        fabScanFromImage.setVisibility(View.GONE);
        tvScanFromCameraLabel.startAnimation(animScaleDown);
        tvScanFromCameraLabel.setVisibility(View.GONE);
    }

    private void showHideScanMenus() {
        int fabScanFromCameraVisibility = fabScanFromCamera.getVisibility();
        if (fabScanFromCameraVisibility == View.VISIBLE) {
            hideScanMenus();
        } else {
            showScanMenus();
        }
    }

    private void initAds() {
        adViewList = new ArrayList<>();
        adViewIsLoadedStatusArray = new boolean[MAX_ADS_IN_LIST];

        for (int i = 0; i < MAX_ADS_IN_LIST; i++) {
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.LARGE_BANNER);
            adView.setAdUnitId(getString(R.string.ad_unit_banner_main));
            adViewList.add(i, adView);
        }

        loadAd(0);
    }

    private void loadAd(int index) {
        if (index >= adViewList.size())
            return;

        AdView adView = adViewList.get(index);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adViewIsLoadedStatusArray[index] = true;
                Logger.d(TAG, "Banner Ad loaded for index: " + index);
                loadAd(index + 1);
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

    private void startCaptureImageActivity() {
        try {
            String fileName = String.valueOf(System.currentTimeMillis() / 1000);
            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
            mCapturedImageFilePath = imageFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(captureImageIntent, SCAN_FROM_CAMERA_REQUEST_CODE);
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SCAN_FROM_CAMERA, null);
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_to_open_camera, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().log("Handled Exception:" + e.getMessage());

            Logger.e(TAG, "Failed to capture image. Path: " + mCapturedImageFilePath);

            Bundle properties = new Bundle();
            properties.putString(AppKeys.ACTION, "capture_image");
            properties.putString(AppKeys.EXCEPTION_MESSAGE, e.getMessage());
            properties.putString(AppKeys.SCREEN_NAME, HomeActivity.class.getSimpleName());
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
        }
    }

    private void startPickImageActivity() {
        Intent actionIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(actionIntent, SCAN_FROM_IMAGE_REQUEST_CODE);
        FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SCAN_FROM_IMAGE, null);
    }

    private void updateRecognizedTextList(RecognizedTextAdapter recognizedTextAdapter, List<RecognizedText> recognizedTextList) {
        List<Object> listItems = new ArrayList<>(recognizedTextList);

        if (recognizedTextList.size() > 0) {
            rvRecognizedText.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);

            int adViewListIndex = 0;
            for (int i = 0; i < recognizedTextList.size(); i += ITEMS_PER_AD) {
                if (i == 0)
                    continue;

                if (adViewListIndex >= adViewList.size())
                    break;

                if (adViewIsLoadedStatusArray[adViewListIndex]) {
                    listItems.add(i, adViewList.get(adViewListIndex++));
                }
            }

        } else {
            tvNoData.setVisibility(View.VISIBLE);
            fabShowHideScanMenus.show();
            rvRecognizedText.setVisibility(View.GONE);
        }

        recognizedTextAdapter.submitList(listItems);

        if (listItems.size() > 8 && totalListItems < listItems.size())
            rvRecognizedText.postDelayed(() -> {
                if (rvRecognizedText != null)
                    rvRecognizedText.smoothScrollToPosition(0);
            }, 500);

        totalListItems = listItems.size();

        Logger.d(TAG, "Item list updated. Total items: " + listItems.size());
    }
}