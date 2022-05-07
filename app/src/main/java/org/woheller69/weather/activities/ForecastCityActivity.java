package org.woheller69.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
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

import java.lang.reflect.Field;
import java.util.List;

public class ForecastCityActivity extends NavigationActivity implements IUpdateableCityUI {
    private WeatherPagerAdapter pagerAdapter;

    private static MenuItem refreshActionButton;
    private MenuItem rainviewerButton;

    private int cityId = -1;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
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

        initResources();

        PFASQLiteHelper db = PFASQLiteHelper.getInstance(this);
        if (db.getAllCitiesToWatch().isEmpty()) {
            // no cities selected.. don't show the viewPager - rather show a text that tells the user that no city was selected
            viewPager2.setVisibility(View.GONE);
            noCityText.setVisibility(View.VISIBLE);

        } else {
            noCityText.setVisibility(View.GONE);
            viewPager2.setVisibility(View.VISIBLE);
            viewPager2.setAdapter(pagerAdapter);
        }

        ViewUpdater.addSubscriber(this);
        ViewUpdater.addSubscriber(pagerAdapter);

        if (pagerAdapter.getItemCount()>0) {  //only if at least one city is watched
             //if pagerAdapter has item with current cityId go there, otherwise use cityId from current item
            if (pagerAdapter.getPosForCityID(cityId)==0) cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());
            CurrentWeatherData currentWeather = db.getCurrentWeatherByCityId(cityId);

            long timestamp = currentWeather.getTimestamp();
            long systemTime = System.currentTimeMillis() / 1000;
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);

            if (timestamp + updateInterval - systemTime <= 0) {
                WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, cityId); //only update current tab at start
                ForecastCityActivity.startRefreshAnimation();
            }
        }
        viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,false,false, (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position)));
        tabLayoutMediator.attach();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_city);
        overridePendingTransition(0, 0);

        initResources();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Update current tab if outside update interval, show animation
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                PFASQLiteHelper database = PFASQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
                CurrentWeatherData currentWeather = database.getCurrentWeatherByCityId(pagerAdapter.getCityIDForPos(position));

                long timestamp = currentWeather.getTimestamp();
                long systemTime = System.currentTimeMillis() / 1000;
                long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);

                if (timestamp + updateInterval - systemTime <= 0) {
                    WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(position));
                    ForecastCityActivity.startRefreshAnimation();
                }
                viewPager2.setNextFocusRightId(position);
                cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());  //save current cityId for next resume
            }

        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("cityId")) cityId = intent.getIntExtra("cityId",-1);
    }

    private void initResources() {
        viewPager2 = findViewById(R.id.viewPager);
        reduceViewpager2DragSensitivity(viewPager2,3);
        tabLayout = findViewById(R.id.tab_layout);
        pagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager(),getLifecycle());
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
        if (id==R.id.menu_rainviewer) {
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                Intent intent = new Intent(this, RainViewerActivity.class);
                intent.putExtra("latitude", pagerAdapter.getLatForPos((viewPager2.getCurrentItem())));
                intent.putExtra("longitude", pagerAdapter.getLonForPos((viewPager2.getCurrentItem())));
                CurrentWeatherData currentWeather = db.getCurrentWeatherByCityId(pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem()));
                intent.putExtra("timezoneseconds",currentWeather.getTimeZoneSeconds());
                startActivity(intent);
            }
        }else if (id==R.id.menu_refresh){
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem()));
                ForecastCityActivity.startRefreshAnimation();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void processNewCurrentWeatherData(CurrentWeatherData data) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void processNewForecasts(List<Forecast> forecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    public static void startRefreshAnimation(){
        {
            if(refreshActionButton !=null && refreshActionButton.getActionView() != null) {
                RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(500);
                rotate.setRepeatCount(5);
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

    //https://devdreamz.com/question/348298-how-to-modify-sensitivity-of-viewpager
    private void reduceViewpager2DragSensitivity(ViewPager2 viewPager, int sensitivity) {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView") ;
            ff.setAccessible(true);
            RecyclerView recyclerView =  (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop") ;
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView,touchSlop*sensitivity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

