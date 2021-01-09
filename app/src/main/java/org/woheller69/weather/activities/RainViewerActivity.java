package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import org.woheller69.weather.preferences.AppPreferencesManager;

import org.woheller69.weather.R;


public class RainViewerActivity extends AppCompatActivity {

    private WebView webView;
    private Button btnPrev, btnStartStop, btnNext;

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
        String API_KEY = prefManager.getOWMApiKey(getApplicationContext());
        float latitude = getIntent().getFloatExtra("latitude", -1);
        float longitude = getIntent().getFloatExtra("longitude", -1);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/rainviewer.html?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {  //register buttons when loading of page finished
                super.onPageFinished(webView, url);
                btnNext = (Button) findViewById(R.id.rainviewer_next);
                btnPrev = (Button) findViewById(R.id.rainviewer_prev);
                btnStartStop = (Button) findViewById(R.id.rainviewer_startstop);

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition + 1);");
                    }
                });

                btnPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition - 1);");
                    }
                });

                btnStartStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:playStop();");
                    }
                });

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
