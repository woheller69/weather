package org.woheller69.weather.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.ui.util.AutoCompleteCityTextViewGenerator;
import org.woheller69.weather.ui.util.AutoSuggestAdapter;
import org.woheller69.weather.ui.util.MyConsumer;
import org.woheller69.weather.ui.util.photonApiCall;
import org.woheller69.weather.weather_api.IHttpRequestForRadiusSearch;
import org.woheller69.weather.weather_api.open_weather_map.OwmHttpRequestForRadiusSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity provides the functionality to search the best weather around a given location.
 */
public class RadiusSearchActivity extends NavigationActivity {

    /**
     * Visual components
     */
    private AppPreferencesManager prefManager;
    private SharedPreferences sharedPreferences;
    private AutoCompleteTextView edtLocation;
    private SeekBar sbEdgeLength;
    private TextView tvEdgeLengthValue;
    private SeekBar sbNumReturns;
    private TextView tvNumReturnsValue;
    private Button btnSearch;

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    String url="https://photon.komoot.io/api/?q=";
    String lang="default";

    private AutoCompleteCityTextViewGenerator cityTextViewGenerator;
    private int LIMIT_LENGTH = 8;

    int edgeRange;
    int minEdgeLength;

    int numberOfReturnsRange;
    int minNumberOfReturns;

    /**
     * Other components
     */
    private PFASQLiteHelper dbHelper;
    private City dropdownSelectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radius_search);
        overridePendingTransition(0, 0);


        dbHelper = PFASQLiteHelper.getInstance(this);
        initialize();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_radius;
    }

    /**
     * Initializes the visual components / the view.
     */
    private void initialize() {
        // Constants
        final int MAX_EDGE_LENGTH_IN_KM = 150;
        final int MIN_EDGE_LENGTH_IN_KM = 20;
        final int MAX_NUMBER_OF_RETURNS = 10;
        final int MIN_NUMBER_OF_RETURNS = 2;
        final int DEFAULT_NUMBER_OF_RETURNS = 3;
        final String FORMAT_EDGE_LENGTH_VALUE = "%s %s";

        // Values which are necessary down below
        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
        edgeRange = Math.round(prefManager.convertDistanceFromKilometers(MAX_EDGE_LENGTH_IN_KM - MIN_EDGE_LENGTH_IN_KM));
        minEdgeLength = Math.round(prefManager.convertDistanceFromKilometers(MIN_EDGE_LENGTH_IN_KM));
        numberOfReturnsRange = MAX_NUMBER_OF_RETURNS - MIN_NUMBER_OF_RETURNS;
        minNumberOfReturns = MIN_NUMBER_OF_RETURNS;

        // Visual components
        cityTextViewGenerator = new AutoCompleteCityTextViewGenerator(this, dbHelper);
        edtLocation = (AutoCompleteTextView) findViewById(R.id.radius_search_edt_location);


        if(sharedPreferences.getString("pref_citySearch", "1").equals("1")) {

            // Option 1: Search city list
            cityTextViewGenerator.generate(edtLocation, LIMIT_LENGTH, EditorInfo.IME_ACTION_SEARCH, new MyConsumer<City>() {
                @Override
                public void accept(City city) {
                    dropdownSelectedCity = city;
                    if (dropdownSelectedCity != null) {
                        //Hide keyboard to have more space
                        final InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edtLocation.getWindowToken(),0);
                    }
                    enableOkButton(city != null);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    handleOnButtonSearchClick();
                }
            });
        }else {

            // Option 2: Search photon API
            autoSuggestAdapter = new AutoSuggestAdapter(getBaseContext(),
                    R.layout.list_item_autocomplete);
            edtLocation.setThreshold(2);
            edtLocation.setAdapter(autoSuggestAdapter);
            edtLocation.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            dropdownSelectedCity = autoSuggestAdapter.getObject(position);
                            enableOkButton(Boolean.TRUE);
                            //Hide keyboard to have more space
                            final InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(edtLocation.getWindowToken(),0);
                        }
                    });

            edtLocation.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int
                        count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                    handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                            AUTO_COMPLETE_DELAY);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.what == TRIGGER_AUTO_COMPLETE) {
                        if (!TextUtils.isEmpty(edtLocation.getText())) {
                            makeApiCall(edtLocation.getText().toString());
                        }
                    }
                    return false;
                }
            });

        }



        sbEdgeLength = (SeekBar)

                findViewById(R.id.radius_search_sb_edge_length);

        tvEdgeLengthValue = (TextView)

                findViewById(R.id.radius_search_tv_edge_length_value);

        sbNumReturns = (SeekBar)

                findViewById(R.id.radius_search_sb_num_returns);

        tvNumReturnsValue = (TextView)

                findViewById(R.id.radius_search_tv_num_returns_value);

        btnSearch = (Button)

                findViewById(R.id.radius_search_btn_search);

        // Set properties of seek bars and the text of the corresponding text views
        sbEdgeLength.setMax(edgeRange);
        sbEdgeLength.setProgress(((edgeRange + minEdgeLength) >> 1) - minEdgeLength);
        tvEdgeLengthValue.setText(
                String.format(FORMAT_EDGE_LENGTH_VALUE, sbEdgeLength.getProgress() + minEdgeLength, prefManager.getDistanceUnit(getApplicationContext()))
        );

        sbNumReturns.setMax(numberOfReturnsRange);
        sbNumReturns.setProgress(DEFAULT_NUMBER_OF_RETURNS - minNumberOfReturns);
        tvNumReturnsValue.setText(String.valueOf(sbNumReturns.getProgress() + minNumberOfReturns));

        // On change of the seek bars set the text of the corresponding text views
        sbEdgeLength.setOnSeekBarChangeListener(new

                OnSeekBarEdgeLengthChange()

        );
        sbNumReturns.setOnSeekBarChangeListener(new

                OnSeekBarNumberOfReturnsChange()

        );

        // Set the click event on the button
        btnSearch.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             handleOnButtonSearchClick();
                                         }
                                     }

        );
    }

    private void enableOkButton(Boolean enabled) {
        btnSearch.setEnabled(enabled);
        if (enabled) {
            btnSearch.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_fullwidth,null));
        } else {
            btnSearch.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_disabled,null));
        }
    }

    /**
     * This method handles the click event on the 'Search' button.
     */

    private void handleOnButtonSearchClick() {
        // Retrieve all necessary inputs (convert the edgeLength if necessary)
        int edgeLength = sbEdgeLength.getProgress() + minEdgeLength;
        int numberOfReturnCities = sbNumReturns.getProgress() + minNumberOfReturns;
        if (prefManager.isDistanceUnitMiles()) {
            edgeLength = Math.round(prefManager.convertMilesInKm(edgeLength));
        }

        // Procedure for retrieving the city (only necessary if no item from the drop down list
        // was selected)

        if (dropdownSelectedCity == null) {
            cityTextViewGenerator.getCityFromText(true);
            if (dropdownSelectedCity == null) {
                return;
            }
        }

        IHttpRequestForRadiusSearch radiusSearchRequest = new OwmHttpRequestForRadiusSearch(getApplicationContext());
        radiusSearchRequest.perform(dropdownSelectedCity.getLatitude(),dropdownSelectedCity.getLongitude(), edgeLength, numberOfReturnCities);
    }

    /**
     * Implements the logic for the SeekBar to set the edge length.
     */
    private class OnSeekBarEdgeLengthChange implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String text = String.format("%s %s", progress + minEdgeLength, prefManager.getDistanceUnit(getApplicationContext()));
            tvEdgeLengthValue.setText(text);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    }

    /**
     * Implements the logic for the SeekBar to set the number of returned cities.
     */
    private class OnSeekBarNumberOfReturnsChange implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvNumReturnsValue.setText(String.valueOf(progress + minNumberOfReturns));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    }



    private void makeApiCall(String text) {
        photonApiCall.make(getApplicationContext(), text, url,lang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                List<City> cityList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("features");
                    for (int i = 0; i < array.length(); i++) {
                        City city =new City();
                        String citystring="";
                        JSONObject jsonFeatures = array.getJSONObject(i);
                        JSONObject jsonProperties = jsonFeatures.getJSONObject("properties");
                        JSONObject jsonGeometry=jsonFeatures.getJSONObject("geometry");
                        JSONArray jsonCoordinates=jsonGeometry.getJSONArray("coordinates");
                        String name="";
                        if (jsonProperties.has("name")) {
                            name=jsonProperties.getString("name");
                            citystring=citystring+name+", ";
                        }
                        String postcode="";
                        if (jsonProperties.has("postcode")) {
                            postcode=jsonProperties.getString("postcode");
                            citystring=citystring+postcode+", ";
                        }
                        String cityname=name;
                        if (jsonProperties.has("city")) {
                            cityname=jsonProperties.getString("city");
                            citystring=citystring+cityname+", ";
                        }
                        String state="";
                        if (jsonProperties.has("state")) {
                            state=jsonProperties.getString("state");
                            citystring=citystring+state+", ";
                        }
                        String countrycode="";
                        if (jsonProperties.has("countrycode")) {
                            countrycode=jsonProperties.getString("countrycode");
                            citystring=citystring+countrycode;
                        }

                        city.setCityName(cityname);
                        city.setCountryCode(countrycode);
                        city.setLatitude((float) jsonCoordinates.getDouble(1));
                        city.setLongitude((float) jsonCoordinates.getDouble(0));
                        cityList.add(city);
                        stringList.add(citystring);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList,cityList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

}
