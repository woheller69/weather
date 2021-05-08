package org.woheller69.weather.activities;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.woheller69.weather.R;


public class SettingsActivity extends NavigationActivity {

    @Override
    protected void onRestart() {
        super.onRestart();

        recreate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        overridePendingTransition(0, 0);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @Override
            public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                setPreferencesFromResource(R.xml.pref_general, rootKey);
            }
    }
}
