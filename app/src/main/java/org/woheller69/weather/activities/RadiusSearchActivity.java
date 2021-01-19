package org.woheller69.weather.activities;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.ui.util.AutoCompleteCityTextViewGenerator;
import org.woheller69.weather.ui.util.MyConsumer;
import org.woheller69.weather.weather_api.IHttpRequestForRadiusSearch;
import org.woheller69.weather.weather_api.open_weather_map.OwmHttpRequestForRadiusSearch;

/**
 * This activity provides the functionality to search the best weather around a given location.
 */
public class RadiusSearchActivity extends NavigationActivity {

    /**
     * Visual components
     */
    private AppPreferencesManager prefManager;
    private AutoCompleteTextView edtLocation;
    private SeekBar sbEdgeLength;
    private TextView tvEdgeLengthValue;
    private SeekBar sbNumReturns;
    private TextView tvNumReturnsValue;
    private Button btnSearch;

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
        edgeRange = Math.round(prefManager.convertDistanceFromKilometers(MAX_EDGE_LENGTH_IN_KM - MIN_EDGE_LENGTH_IN_KM));
        minEdgeLength = Math.round(prefManager.convertDistanceFromKilometers(MIN_EDGE_LENGTH_IN_KM));
        numberOfReturnsRange = MAX_NUMBER_OF_RETURNS - MIN_NUMBER_OF_RETURNS;
        minNumberOfReturns = MIN_NUMBER_OF_RETURNS;

        // Visual components
        cityTextViewGenerator = new AutoCompleteCityTextViewGenerator(this, dbHelper);
        edtLocation = (AutoCompleteTextView) findViewById(R.id.radius_search_edt_location);
        cityTextViewGenerator.generate(edtLocation, LIMIT_LENGTH, EditorInfo.IME_ACTION_SEARCH, new MyConsumer<City>() {
            @Override
            public void accept(City city) {
                dropdownSelectedCity = city;
                if(dropdownSelectedCity!=null) {
                    //Hide keyboard to have more space
                    final InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                enableOkButton(city != null);
            }
        }, new Runnable() {
            @Override
            public void run() {
                handleOnButtonSearchClick();
            }
        });

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
        radiusSearchRequest.perform(dropdownSelectedCity.getCityId(), edgeLength, numberOfReturnCities);
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

}
