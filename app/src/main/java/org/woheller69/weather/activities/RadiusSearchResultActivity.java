package org.woheller69.weather.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.radius_search.RadiusSearchItem;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;
import org.woheller69.weather.weather_api.ValueDeriver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RadiusSearchResultActivity extends AppCompatActivity {

    /**
     * Visual components
     */
    private ListView listViewResult;
    private WebView webView;

    /**
     * Member variables
     */
    List<String> itemsToDisplay;
    ArrayAdapter<String> itemsAdapter;




    @Override
    protected void onPause() {
        super.onPause();
        webView.destroy();   //clear webView memory
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radius_search_result);

        // Retrieve the data to display
        Bundle bundle = getIntent().getExtras();
        ArrayList<RadiusSearchItem> resultList = bundle.getParcelableArrayList("resultList");
        itemsToDisplay = getItemsToDisplay(resultList);

        //find center
        double lat,lon,lat_min, lat_max, lon_min,lon_max;
        lat = resultList.get(0).getLat();
        lon = resultList.get(0).getLon();
        lat_max=lat_min=lat;
        lon_max=lon_min=lon;
        for (int i = 0; i < resultList.size(); i++) {
            lat = resultList.get(i).getLat();
            lon = resultList.get(i).getLon();
            if (lat<lat_min)lat_min=lat;
            if (lat>lat_max)lat_max=lat;
            if (lon<lon_min)lon_min=lon;
            if (lon>lon_max)lon_max=lon;
        }

        initialize();
        webView = findViewById(R.id.webViewRadiussearch);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebViewClient(resultList));
        webView.loadUrl("file:///android_asset/radiussearch.html?latmin=" + lat_min+ "&lonmin=" + lon_min +"&latmax="+lat_max+"&lonmax="+lon_max);


    }
    private class CustomWebViewClient extends WebViewClient {
        private ArrayList<RadiusSearchItem> resultList;

        public CustomWebViewClient(ArrayList<RadiusSearchItem> resultList) {  //custom client needed for passing resultList
            this.resultList = resultList;
        }
        @Override
            public void onPageFinished(WebView view, String url) {                 // page loading completes
            double lat,lon,temp;
            int cat;
            int unit;
            AppPreferencesManager prefManager =
                    new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
            for (int i = 0; i < resultList.size(); i++) {
                lat = resultList.get(i).getLat();
                lon = resultList.get(i).getLon();

                temp = Math.round(prefManager.convertTemperatureFromCelsius((float) resultList.get(i).getTemperature())*10.0)/10.0;  //round 1 digit
                cat = resultList.get(i).getWeatherCategory();
                 if (prefManager.getWeatherUnit() =="°C") unit=0;
                else unit=1;
                
                webView.loadUrl("javascript:addMarker("+ lat + ","+ lon + "," + temp + "," + unit + "," + cat + ");");
            }
        }
    }

    /**
     * Initialized the components of this activity.
     */
    private void initialize() {
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemsToDisplay);
        listViewResult = (ListView) findViewById(R.id.activity_radius_search_result_list_view);
        listViewResult.setAdapter(itemsAdapter);
    }

    private List<String> getItemsToDisplay(List<RadiusSearchItem> resultList) {
        List<String> itemsToDisplay = new ArrayList<>();
        IApiToDatabaseConversion.WeatherCategories category;
        ValueDeriver deriver = new ValueDeriver(getApplicationContext());

        DecimalFormat decimalFormatter = new DecimalFormat("0.0");
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));

        for (int i = 0; i < resultList.size(); i++) {
            category = IApiToDatabaseConversion.getLabelForValue(resultList.get(i).getWeatherCategory());
            itemsToDisplay.add(String.format(
                    "%s. %s, %s %s %s",
                    i + 1,
                    resultList.get(i).getCityName(),
                    deriver.getWeatherDescriptionByCategory(category),
                    decimalFormatter.format(prefManager.convertTemperatureFromCelsius((float) resultList.get(i).getTemperature())),
                    prefManager.getWeatherUnit()
            ));
        }
        return itemsToDisplay;
    }

}

