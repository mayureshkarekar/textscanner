package com.getext.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getext.R;
import com.getext.firebase.FirebaseAnalyticsHelper;
import com.getext.keys.AppKeys;
import com.getext.utils.logger.Logger;
import com.getext.utils.ocr.TextRecognizer;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.util.Objects;

public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener, RequestListener<Drawable> {
    private final String TAG = ImagePreviewActivity.class.getSimpleName();
    private final int EDIT_IMAGE_REQUEST_CODE = 1;
    public static Bitmap mImageBitmap = null;
    private ImageView ivImage;
    private ProgressBar pbImagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_image_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_image) {
            BitmapDrawable imageBitmapDrawable = (BitmapDrawable) ivImage.getDrawable();
            mImageBitmap = imageBitmapDrawable.getBitmap();
            Intent editImageActivityIntent = new Intent(this, EditImageActivity.class);
            startActivityForResult(editImageActivityIntent, EDIT_IMAGE_REQUEST_CODE);
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EDIT_IMAGE, null);

            return true;
        }

        Logger.e(TAG, "No valid case found for id: " + item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_IMAGE_REQUEST_CODE) {
                Glide.with(this).load(mImageBitmap).listener(this).into(ivImage);
            } else {
                Logger.e(TAG, "Invalid request code: " + requestCode);
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageBitmap = null;
        Logger.d(TAG, "Image bitmap cleaned: " + mImageBitmap);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_proceed) {
            pbImagePreview.setVisibility(View.VISIBLE);
            extractTextFromBitmap(this);
            Bundle properties = new Bundle();
            properties.putInt(AppKeys.RECOGNITION_MODE, getIntent().getIntExtra(AppKeys.RECOGNITION_MODE, -1));
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SCAN_TEXT_STARTED, properties);
        } else {
            Logger.e(TAG, "Click not handled for id: " + view.getId());
        }
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        handleImageLoadingFailure("Glide failed to load image.", e);
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        if (pbImagePreview != null) {
            pbImagePreview.setVisibility(View.GONE);
        }
        return false;
    }

    private void initialize() {
        Toolbar toolbar = findViewById(R.id.tb_image_preview);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_24dp);
        }

        pbImagePreview = findViewById(R.id.pb_image_preview);
        ivImage = findViewById(R.id.iv_image);
        findViewById(R.id.btn_proceed).setOnClickListener(this);

        Intent intent = getIntent();
        int recognitionMode = intent.getIntExtra(AppKeys.RECOGNITION_MODE, -1);
        switch (recognitionMode) {
            case AppKeys.RECOGNITION_MODE_CAMERA: {
                try {
                    String capturedImagePath = intent.getStringExtra(AppKeys.CAPTURED_IMAGE_PATH);
                    Bitmap imageBitmap = rotateImageByOrientation(Objects.requireNonNull(capturedImagePath), BitmapFactory.decodeFile(capturedImagePath));
                    Glide.with(this).load(imageBitmap).listener(this).into(ivImage);
                } catch (Exception e) {
                    handleImageLoadingFailure("Captured image file path is null or empty.", e);
                }
            }
            break;

            case AppKeys.RECOGNITION_MODE_IMAGE: {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Uri pickedImageUri = (Uri) extras.get(AppKeys.PICKED_IMAGE_URI);
                    Glide.with(this).load(pickedImageUri).listener(this).into(ivImage);
                } else {
                    handleImageLoadingFailure("Intent extras are null.", null);
                }
            }
            break;

            default: {
                handleImageLoadingFailure("Invalid recognition mode.", null);
            }
            break;
        }
    }

    private void handleImageLoadingFailure(@NonNull String errorMsg, @Nullable Exception e) {
        Toast.makeText(this, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
        finish();
        if (e != null) {
            e.printStackTrace();
            String message = (e.getMessage() != null) ? e.getMessage() : errorMsg;
            FirebaseCrashlytics.getInstance().log("Handled Exception:" + message);

            Logger.e(TAG, "Image loading failed: " + errorMsg);

            Bundle properties = new Bundle();
            properties.putString(AppKeys.ACTION, "loading_image");
            properties.putString(AppKeys.EXCEPTION_MESSAGE, e.getMessage());
            properties.putString(AppKeys.SCREEN_NAME, ImagePreviewActivity.class.getSimpleName());
            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
        }
    }

    private Bitmap rotateImageByOrientation(@NonNull String imagePath, @NonNull Bitmap imageBitmap) throws IOException {
        ExifInterface exifInterface = new ExifInterface(imagePath);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                imageBitmap = rotateImage(imageBitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                imageBitmap = rotateImage(imageBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                imageBitmap = rotateImage(imageBitmap, 270);
                break;
        }

        return imageBitmap;
    }

    public Bitmap rotateImage(@NonNull Bitmap imageBitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
    }

    private void extractTextFromBitmap(Context context) {
        Runnable extractTextRunnable = () -> {
            try {
                BitmapDrawable imageBitmapDrawable = (BitmapDrawable) ivImage.getDrawable();
                Bitmap imageBitmap = imageBitmapDrawable.getBitmap();
                String recognizedText = TextRecognizer.recognizeText(context, imageBitmap);

                if (!isFinishing() && pbImagePreview != null) {
                    runOnUiThread(() -> {
                        pbImagePreview.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(recognizedText)) {
                            Intent scanResultActivityIntent = new Intent(context, ScanResultActivity.class);
                            scanResultActivityIntent.putExtra(AppKeys.RECOGNITION_MODE, getIntent().getIntExtra(AppKeys.RECOGNITION_MODE, -1));
                            scanResultActivityIntent.putExtra(AppKeys.RECOGNIZED_TEXT, recognizedText);
                            startActivity(scanResultActivityIntent);
                            finish();
                            Bundle properties = new Bundle();
                            properties.putBoolean(AppKeys.TEXT_FOUND, true);
                            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SCAN_TEXT_COMPLETED, properties);
                        } else {
                            Toast.makeText(context, R.string.no_text_found, Toast.LENGTH_SHORT).show();
                            Bundle properties = new Bundle();
                            properties.putBoolean(AppKeys.TEXT_FOUND, false);
                            FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.SCAN_TEXT_COMPLETED, properties);
                        }
                    });
                }
            } catch (Exception e) {
                if (!isFinishing() && pbImagePreview != null) {
                    runOnUiThread(() -> {
                        pbImagePreview.setVisibility(View.GONE);
                        Toast.makeText(context, R.string.failed_to_scan_image, Toast.LENGTH_SHORT).show();
                    });
                }

                e.printStackTrace();
                String message = (e.getMessage() != null) ? e.getMessage() : "Failed to scan image.";
                FirebaseCrashlytics.getInstance().log("Handled Exception:" + message);

                Bundle properties = new Bundle();
                properties.putString(AppKeys.ACTION, "scanning_text");
                properties.putString(AppKeys.EXCEPTION_MESSAGE, e.getMessage());
                properties.putString(AppKeys.SCREEN_NAME, ImagePreviewActivity.class.getSimpleName());
                FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
            }
        };

        Thread extractTextThread = new Thread(extractTextRunnable);
        extractTextThread.start();
    }
}