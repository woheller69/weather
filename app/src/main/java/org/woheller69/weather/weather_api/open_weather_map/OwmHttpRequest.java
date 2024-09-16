package org.woheller69.weather.weather_api.open_weather_map;

import android.content.Context;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OwmHttpRequest {

    /**
     * Joins the city IDs of the given cities by separating is using commas.
     *
     * @param cities A list of cities to build the groupID for.
     * @return Returns a comma-separated list of city IDs.
     */
    protected String joinCityIDs(List<CityToWatch> cities) {
        List<Integer> cityIDs = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            cityIDs.add(cities.get(i).getCityId());
        }
        return TextUtils.join(",", cityIDs);
    }

    /**
     * Builds the URL for the OpenWeatherMap API that can be used to query the weather for a single
     * city.
     *
     *
     * @param context
     * @param lat The latitude of the city to get the forecast data for.
     * @param lon The longitude of the city to get the forecast data for.
     * @param useMetric If set to true, temperature values will be in Celsius.
     * @return Returns the URL that can be used to query the weather for the given city.
     */
    protected String getUrlForQueryingSingleCity(Context context, float lat, float lon, boolean useMetric) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
                return String.format(
                "%sweather?lat=%s&lon=%s%s&appid=%s",
                        BuildConfig.BASE_URL,
                        lat,
                        lon,
                (useMetric) ? "&units=metric" : "",
                prefManager.getOWMApiKey(context)
        );
    }

    /**
     * Builds the URL for the OpenWeatherMap API that can be used to query the weather forecast
     * for a single city.
     *
     * @param lat The latitude of the city to get the forecast data for.
     * @param lon The longitude of the city to get the forecast data for.
     * @return Returns the URL that can be used to query weather forecasts for the given city using
     * OpenWeatherMap.
     */
    protected String getUrlForQueryingForecast(Context context, float lat, float lon) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        return String.format(
                "%sforecast?lat=%s&lon=%s&units=metric&appid=%s",
                BuildConfig.BASE_URL,
                lat,
                lon,
                prefManager.getOWMApiKey(context)
        );
    }

    /**
     * Builds the URL for the OpenWeatherMap API that can be used to query the weather forecast
     * via One Call API.
     *
     * @param lat The latitude of the city to get the forecast data for.
     * @param lon The longitude of the city to get the forecast data for.
     * @return Returns the URL that can be used to query weather forecasts for the given city using
     * OpenWeatherMap.
     */
    protected String getUrlForQueryingOneCallAPI(Context context, float lat, float lon) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
        return String.format(
                "%sonecall?lat=%s&lon=%s&units=metric&exclude=alerts&appid=%s",
                BuildConfig.BASE_URL,
                lat,
                lon,
                prefManager.getOWMApiKey(context)
        );
    }

    /**
     * Builds the URL for the OpenWeatherMap API that can be used to query the weather for several
     * cities within a given rectangle.
     *
     * @param boundingBox An array consisting of four value: The first value is the left longitude,
     *                    second bottom latitude, third right longitude, fourth top latitude.
     * @param mapZoom     The map zoom which determines the granularity of the OpenWeatherMap response.
     *                    Lower values will return less cities.
     * @return Returns the URL for the OpenWeatherMap API to retrieve the weather data of cities
     * within the specified bounding box.
     */
    public String getUrlForQueryingRadiusSearch(Context context, double[] boundingBox, int mapZoom) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        return String.format(
                "%sbox/city?bbox=%s,%s,%s,%s,%s&cluster=yes&appid=%s",
                BuildConfig.BASE_URL,
                boundingBox[0],
                boundingBox[1],
                boundingBox[2],
                boundingBox[3],
                mapZoom,
                prefManager.getOWMApiKey(context)
        );
    }

}
