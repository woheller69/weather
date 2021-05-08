package org.woheller69.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.TaskStackBuilder;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;


import org.woheller69.weather.R;

import static java.lang.Boolean.TRUE;

/**
 * Created by Chris on 04.07.2016.
 */
public class NavigationActivity extends AppCompatActivity implements OnNavigationItemSelectedListener {

    // delay to launch nav drawer item, to allow close animation to play
    static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    // Helper
    private Handler mHandler;
    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler();


        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    protected int getNavigationDrawerID() {
        return 0;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        return goToNavigationItem(itemId);
    }

    protected boolean goToNavigationItem(final int itemId) {

        if (itemId == getNavigationDrawerID()) {
            // just close drawer because we are already in this activity
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // delay transition so the drawer can close
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawer(GravityCompat.START);

        selectNavigationItem(itemId);


        return true;
    }

    // set active navigation item
    private void selectNavigationItem(int itemId) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            boolean b = itemId == mNavigationView.getMenu().getItem(i).getItemId();
            mNavigationView.getMenu().getItem(i).setChecked(b);
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * {@code AndroidManifest.xml} to find out the parent activity names for each activity.
     *
     * @param intent
     */
    private void createBackStack(Intent intent) {
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(intent);
        builder.startActivities();
    }

    private void callDrawerItem(final int itemId) {

        Intent intent;
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefManager.getBoolean("pref_DarkMode", false)==TRUE) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (itemId==R.id.nav_weather) {
            intent = new Intent(this, ForecastCityActivity.class);
            startActivity(intent);
        }else if (itemId==R.id.nav_manage){
            intent = new Intent(this, ManageLocationsActivity.class);
            startActivity(intent);
        }else if (itemId==R.id.nav_radius) {
            intent = new Intent(this, RadiusSearchActivity.class);
            createBackStack(intent);
        }else if (itemId==R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
            createBackStack(intent);
        }else if(itemId==R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
            createBackStack(intent);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        selectNavigationItem(getNavigationDrawerID());

    }


}
