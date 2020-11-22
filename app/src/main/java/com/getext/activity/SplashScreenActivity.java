package com.getext.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.getext.R;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar_Fullscreen);
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}