package org.woheller69.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;

import java.util.List;

public class ForecastCityActivity extends NavigationActivity implements IUpdateableCityUI {
    private WeatherPagerAdapter pagerAdapter;

    private MenuItem refreshActionButton;
    private MenuItem rainviewerButton;

    private int cityId = -1;
    private ViewPager viewPager;
    private TextView noCityText;

    @Override
    protected void onPause() {
        super.onPause();

        ViewUpdater.removeSubscriber(this);
        ViewUpdater.removeSubscriber(pagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewUpdater.addSubscriber(this);
        ViewUpdater.addSubscriber(pagerAdapter);

        if (pagerAdapter.getCount()>0) {  //only if at least one city is watched
            // if Intent contains cityId use this city, otherwise go to previous position
            cityId = getIntent().getIntExtra("cityId", pagerAdapter.getCityIDForPos(viewPager.getCurrentItem()));
            //TODO possible slowdown when opening Activity
            pagerAdapter.refreshSingleData(false, cityId);  //only update current tab at start
        }
        viewPager.setCurrentItem(pagerAdapter.getPosForCityID(cityId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_city);
        overridePendingTransition(0, 0);

        initResources();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {

                //Update current tab if outside update interval, show animation
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                PFASQLiteHelper database = PFASQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
                CurrentWeatherData currentWeather = database.getCurrentWeatherByCityId(pagerAdapter.getCityIDForPos(position));

                long timestamp = currentWeather.getTimestamp();
                long systemTime = System.currentTimeMillis() / 1000;
                long updateInterval = Long.parseLong(prefManager.getString("pref_updateInterval", "2")) * 60 * 60;

                if (timestamp + updateInterval - systemTime <= 0) {
                    startRefreshAnimation();
                    pagerAdapter.refreshSingleData(false, pagerAdapter.getCityIDForPos(position));
                }
                viewPager.setNextFocusRightId(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager, true);


        PFASQLiteHelper db = PFASQLiteHelper.getInstance(this);
        if (db.getAllCitiesToWatch().isEmpty()) {
            // no cities selected.. don't show the viewPager - rather show a text that tells the user that no city was selected
            viewPager.setVisibility(View.GONE);
            noCityText.setVisibility(View.VISIBLE);

        } else {
            noCityText.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            viewPager.setAdapter(pagerAdapter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initResources() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager());
        noCityText = findViewById(R.id.noCitySelectedText);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_weather;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_forecast_city, menu);

        final Menu m = menu;

        refreshActionButton = menu.findItem(R.id.menu_refresh);
        refreshActionButton.setActionView(R.layout.menu_refresh_action_view);
        refreshActionButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.performIdentifierAction(refreshActionButton.getItemId(), 0);
            }
        });
        rainviewerButton = menu.findItem(R.id.menu_rainviewer);
        rainviewerButton.setActionView(R.layout.menu_rainviewer_view);
        rainviewerButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.performIdentifierAction(rainviewerButton.getItemId(), 0);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        PFASQLiteHelper db = PFASQLiteHelper.getInstance(this);
        switch(id) {
            case R.id.menu_rainviewer:
                if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                    Intent intent = new Intent(this, RainViewerActivity.class);
                    intent.putExtra("latitude", pagerAdapter.getLatForPos((viewPager.getCurrentItem())));
                    intent.putExtra("longitude", pagerAdapter.getLonForPos((viewPager.getCurrentItem())));
                    startActivity(intent);
                    break;
                }
            case R.id.menu_refresh:
                 if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                     pagerAdapter.refreshSingleData(true, pagerAdapter.getCityIDForPos(viewPager.getCurrentItem()));
                     startRefreshAnimation();
                 }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void processNewWeatherData(CurrentWeatherData data) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void updateWeekForecasts(List<WeekForecast> forecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void updateForecasts(List<Forecast> forecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    public void startRefreshAnimation(){
        {

            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(500);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    refreshActionButton.getActionView().setActivated(false);
                    refreshActionButton.getActionView().setEnabled(false);
                    refreshActionButton.getActionView().setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    refreshActionButton.getActionView().setActivated(true);
                    refreshActionButton.getActionView().setEnabled(true);
                    refreshActionButton.getActionView().setClickable(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            refreshActionButton.getActionView().startAnimation(rotate);
        }
    }
}

