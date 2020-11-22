package com.getext.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.getext.R;
import com.getext.firebase.FirebaseAnalyticsHelper;
import com.getext.keys.AppKeys;
import com.getext.utils.logger.Logger;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = EditImageActivity.class.getSimpleName();
    private CropImageView ivImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        initialize();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel: {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            break;

            case R.id.iv_rotate: {
                ivImage.rotateImage(90);
            }
            break;

            case R.id.btn_done: {
                ImagePreviewActivity.mImageBitmap = ivImage.getCroppedImage();
                setResult(Activity.RESULT_OK);
                finish();
            }
            break;

            default: {
                Logger.e(TAG, "Click not handled for id: " + view.getId());
            }
            break;
        }
    }

    private void initialize() {
        if (ImagePreviewActivity.mImageBitmap != null) {
            ivImage = findViewById(R.id.iv_image);
            ivImage.setImageBitmap(ImagePreviewActivity.mImageBitmap);

            findViewById(R.id.btn_cancel).setOnClickListener(this);
            findViewById(R.id.iv_rotate).setOnClickListener(this);
            findViewById(R.id.btn_done).setOnClickListener(this);
        } else {
            handleImageLoadingFailure("Image bitmap is null.");
        }
    }

    private void handleImageLoadingFailure(@NonNull String errorMsg) {
        Toast.makeText(this, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
        FirebaseCrashlytics.getInstance().log("Handled Exception:" + errorMsg);

        Logger.e(TAG, "Image loading failed: " + errorMsg);

        Bundle properties = new Bundle();
        properties.putString(AppKeys.ACTION, "loading_image");
        properties.putString(AppKeys.EXCEPTION_MESSAGE, errorMsg);
        properties.putString(AppKeys.SCREEN_NAME, EditImageActivity.class.getSimpleName());
        FirebaseAnalyticsHelper.getInstance().logEvent(this, AppKeys.EXCEPTION_CAUGHT, properties);
    }
}