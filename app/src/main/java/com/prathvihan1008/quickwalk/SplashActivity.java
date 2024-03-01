package com.prathvihan1008.quickwalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 210; // Splash screen duration in milliseconds (0.5seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Using a Handler to delay the transition to MainActivity
        new Handler().postDelayed(() -> {
            // Start MainActivity after the splash duration
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish(); // Close the splash activity to prevent going back to it
        }, SPLASH_DURATION);
    }
}