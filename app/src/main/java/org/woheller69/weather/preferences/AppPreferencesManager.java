package org.woheller69.weather.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;

/**
 * This class provides access and methods for relevant preferences.
 */
public class AppPreferencesManager {


    /**
     * Member variables
     */
    SharedPreferences preferences;

    /**
     * Constructor.
     *
     * @param preferences Source for the preferences to use.
     */
    public AppPreferencesManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isFirstTimeLaunch() {
        return preferences.getString("API_key_value", "").equals("");
    }

    /**
     * This method converts a given temperature value into the unit that was set in the preferences.
     *
     * @param temperature The temperature to convert into the unit that is set in the preferences.
     *                    Make sure to pass a value in celsius.
     * @return Returns the converted temperature.
     **
     */
    public float convertTemperatureFromCelsius(float temperature) {
        // 1 = Celsius (fallback), 2 = Fahrenheit
        int prefValue = Integer.parseInt(preferences.getString("temperatureUnit", "1"));
        if (prefValue == 1) {
            return temperature;
        } else {
            return (((temperature * 9) / 5) + 32);
        }
    }

    /**
     * This method converts a given distance value into the unit that was set in the preferences.
     *
     * @param kilometers The kilometers to convert into the unit that is set in the preferences.
     *                   Make sure to pass a value in kilometers.
     * @return Returns the converted distance.
     */
    public float convertDistanceFromKilometers(float kilometers) {
        // 1 = kilometers, 2 = miles
        int prefValue = Integer.parseInt(preferences.getString("distanceUnit", "1"));
        if (prefValue == 1) {
            return kilometers;
        } else {
            return convertKmInMiles(kilometers);
        }
    }

    /**
     * @return Returns true if miles was set as distance unit in the preferences else false.
     */
    public boolean isDistanceUnitMiles() {
        int prefValue = Integer.parseInt(preferences.getString("distanceUnit", "1"));
        return (prefValue == 2);
    }

    /**
     * Converts a kilometer value in miles.
     *
     * @param km The value to convert to miles.
     * @return Returns the converted value.
     */
    public float convertKmInMiles(float km) {
        // TODO: Is this the right class for the function???
        return (float) (km / 1.609344);
    }

    /**
     * Converts a miles value in kilometers.
     *
     * @param miles The value to convert to kilometers.
     * @return Returns the converted value.
     */
    public float convertMilesInKm(float miles) {
        // TODO: Is this the right class for the function???
        return (float) (miles * 1.609344);
    }

    /**
     * @return Returns "°C" in case Celsius is set and "°F" if Fahrenheit was selected.
     */
    public String getTemperatureUnit() {
        int prefValue = Integer.parseInt(preferences.getString("temperatureUnit", "1"));
        if (prefValue == 1) {
            return "°C";
        } else {
            return "°F";
        }
    }

    /**
     * @return Returns "km" in case kilometer is set and "mi" if miles was selected.
     * @param applicationContext
     */
    public String getDistanceUnit(Context applicationContext) {
        int prefValue = Integer.parseInt(preferences.getString("distanceUnit", "1"));
        if (prefValue == 1) {
            return applicationContext.getString(R.string.units_km);
        } else {
            return "mi";
        }
    }


    public String getOWMApiKey(Context context){
        String prefValue = preferences.getString("API_key_value", BuildConfig.DEFAULT_API_KEY);
        if (prefValue.equals(context.getString(R.string.settings_API_key_default))) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, context.getResources().getString(R.string.settings_title_API_key), Toast.LENGTH_LONG).show());
            return BuildConfig.DEFAULT_API_KEY;
        } else {
            return prefValue;
        }
    }

    public boolean showStarDialog() {
        int versionCode = preferences.getInt("versionCode",0);
        boolean askForStar=preferences.getBoolean("askForStar",true);

        if (!isFirstTimeLaunch() && BuildConfig.VERSION_CODE>versionCode && askForStar){ //not at first start, only after upgrade and only if use has not yet given a star or has declined
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
         return true;
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
          return false;
        }
    }

    public void setAskForStar(boolean askForStar){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("askForStar", askForStar);
        editor.apply();
    }
}
