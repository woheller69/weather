package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import org.woheller69.weather.preferences.AppPreferencesManager;

import org.woheller69.weather.R;


public class RainViewerActivity extends AppCompatActivity {

    private WebView webView;
    private float latitude;
    private float longitude;
    private static String API_KEY;

    @Override
    protected void onPause() {
        super.onPause();
        webView.destroy();   //clear webView memory
        finish();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_viewer);
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        API_KEY=prefManager.getOWMApiKey(getApplicationContext());
        latitude = getIntent().getFloatExtra("latitude",-1);
        longitude = getIntent().getFloatExtra("longitude",-1);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/rainviewer.html?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY);
    }
}
