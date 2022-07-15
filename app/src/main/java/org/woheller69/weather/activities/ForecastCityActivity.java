package org.woheller69.weather.activities;

import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;
import org.woheller69.weather.widget.WeatherWidget;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class ForecastCityActivity extends NavigationActivity implements IUpdateableCityUI {
    private WeatherPagerAdapter pagerAdapter;
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;
    private static MenuItem updateLocationButton;
    private static MenuItem refreshActionButton;
    private MenuItem rainviewerButton;

    private int cityId = -1;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView noCityText;
    Context context;

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
            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,false,false, (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position)));
            tabLayoutMediator.attach();
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
            if (viewPager2.getCurrentItem()!=pagerAdapter.getPosForCityID(cityId)) viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId),false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
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
        viewPager2 = findViewById(R.id.viewPager2);
        reduceViewpager2DragSensitivity(viewPager2,2);
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
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateLocationButton = menu.findItem(R.id.menu_update_location);
        PFASQLiteHelper db = PFASQLiteHelper.getInstance(this);
        if(prefManager.getBoolean("pref_GPS", true)==TRUE && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !db.getAllCitiesToWatch().isEmpty()) {
            updateLocationButton.setVisible(true);
            updateLocationButton.setActionView(R.layout.menu_update_location_view);
            updateLocationButton.getActionView().clearAnimation();
            if (locationListenerGPS!=null) {  //GPS still trying to get new location
                if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                    startUpdateLocatationAnimation();
                }
            }
            updateLocationButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(updateLocationButton.getItemId(), 0));
        }else{
            if (locationListenerGPS!=null) {
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
            }
            locationListenerGPS=null;
            if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                updateLocationButton.getActionView().clearAnimation();
            }
        }

        refreshActionButton = menu.findItem(R.id.menu_refresh);
        refreshActionButton.setActionView(R.layout.menu_refresh_action_view);
        refreshActionButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(refreshActionButton.getItemId(), 0));

        rainviewerButton = menu.findItem(R.id.menu_rainviewer);
        rainviewerButton.setActionView(R.layout.menu_rainviewer_view);
        rainviewerButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(rainviewerButton.getItemId(), 0));

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
        } else if (id==R.id.menu_update_location) {
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (prefManager.getBoolean("pref_GPS", true) == TRUE && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (locationListenerGPS == null) {
                        Log.d("GPS", "Listener null");
                        locationListenerGPS = new LocationListener() {
                            @Override
                            public void onLocationChanged(android.location.Location location) {
                                Log.d("GPS", "Location changed");
                                PFASQLiteHelper db = PFASQLiteHelper.getInstance(context);
                                CityToWatch city = db.getCityToWatch(WeatherWidget.getWidgetCityID(context));
                                city.setLatitude((float) location.getLatitude());
                                city.setLongitude((float) location.getLongitude());
                                city.setCityName(String.format(Locale.getDefault(), "%.2f° / %.2f°", location.getLatitude(), location.getLongitude()));
                                db.updateCityToWatch(city);
                                db.deleteForecastsByCityId(WeatherWidget.getWidgetCityID(context));
                                tabLayout.getTabAt(0).setText(city.getCityName());
                                WeatherPagerAdapter.refreshSingleData(getApplicationContext(), true, WeatherWidget.getWidgetCityID(context));
                                ForecastCityActivity.startRefreshAnimation();
                                if (locationListenerGPS != null)
                                    locationManager.removeUpdates(locationListenerGPS);
                                locationListenerGPS = null;
                                if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                                    updateLocationButton.getActionView().clearAnimation();
                                }
                            }

                            @Deprecated
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                            }
                        };
                        Log.d("GPS", "Request Updates");
                        ForecastCityActivity.startUpdateLocatationAnimation();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListenerGPS);
                    }
                }
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

    public static void startUpdateLocatationAnimation(){
        {
            if(updateLocationButton !=null && updateLocationButton.getActionView() != null) {
                Animation blink = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                blink.setDuration(1000);
                blink.setRepeatCount(Animation.INFINITE);
                blink.setInterpolator(new LinearInterpolator());
                blink.setRepeatMode(Animation.REVERSE);
                blink.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        updateLocationButton.getActionView().setActivated(false);
                        updateLocationButton.getActionView().setEnabled(false);
                        updateLocationButton.getActionView().setClickable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        updateLocationButton.getActionView().setActivated(true);
                        updateLocationButton.getActionView().setEnabled(true);
                        updateLocationButton.getActionView().setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                updateLocationButton.getActionView().startAnimation(blink);
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

