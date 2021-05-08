package org.woheller69.weather.services;

import android.app.IntentService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import androidx.core.app.JobIntentService;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.weather_api.IHttpRequestForForecast;
import org.woheller69.weather.weather_api.IHttpRequestForOneCallAPI;
import org.woheller69.weather.weather_api.open_weather_map.OwmHttpRequestForForecast;
import org.woheller69.weather.weather_api.open_weather_map.OwmHttpRequestForOneCallAPI;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * This class provides the functionality to fetch forecast data for a given city as a background
 * task.
 */
public class UpdateDataService extends JobIntentService {

    public static final String UPDATE_FORECAST_ACTION = "org.woheller69.weather.services.UpdateDataService.UPDATE_FORECAST_ACTION";
    public static final String UPDATE_ALL_ACTION = "org.woheller69.weather.services.UpdateDataService.UPDATE_ALL_ACTION";
    public static final String UPDATE_SINGLE_ACTION = "org.woheller69.weather.services.UpdateDataService.UPDATE_SINGLE_ACTION";

    public static final String CITY_ID = "cityId";
    public static final String SKIP_UPDATE_INTERVAL = "skipUpdateInterval";

    private PFASQLiteHelper dbHelper;
    private SharedPreferences prefManager;

    /**
     * Constructor.
     */
    public UpdateDataService() {
        super();
    }

    /**
     * @see IntentService#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = PFASQLiteHelper.getInstance(getApplicationContext());
        prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     *
     */
    @Override
    protected void onHandleWork(Intent intent) {
        if (!isOnline()) {
            Handler h = new Handler(getApplicationContext().getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        if (intent != null) {
            if (UPDATE_ALL_ACTION.equals(intent.getAction())) handleUpdateAll(intent);
            else if (UPDATE_FORECAST_ACTION.equals(intent.getAction()))
                handleUpdateForecastAction(intent);
            else if (UPDATE_SINGLE_ACTION.equals(intent.getAction())) handleUpdateSingle(intent);
        }
    }


    /**
     * Be careful, with using this. It can cause many calls to the API, because it wants to update everything if the update interval allows it.
     *
     * @param intent contains necessary parameters for the service work
     */
    private void handleUpdateAll(Intent intent) {
         List<CityToWatch> cities = dbHelper.getAllCitiesToWatch();
        for (CityToWatch c : cities) {
            handleUpdateForecastAction(intent, c.getCityId(),c.getLatitude(),c.getLongitude());
        }
    }

    private void handleUpdateSingle(Intent intent) {
        int cityId = intent.getIntExtra("cityId",-1);
        CityToWatch city = dbHelper.getCityToWatch(cityId);
        handleUpdateForecastAction(intent, cityId, city.getLatitude(), city.getLongitude());
    }

    private void handleUpdateForecastAction(Intent intent, int cityId, float lat, float lon) {
        boolean skipUpdateInterval = intent.getBooleanExtra(SKIP_UPDATE_INTERVAL, false);

        long timestamp = 0;
        long systemTime = System.currentTimeMillis() / 1000;
        long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);

        if (!skipUpdateInterval) {
            // check timestamp of the current forecasts
            List<Forecast> forecasts = dbHelper.getForecastsByCityId(cityId);
            if (forecasts.size() > 0) {
                timestamp = forecasts.get(0).getTimestamp();
            }
        }

        // Update if update forced or if a certain time has passed
        if (skipUpdateInterval || timestamp + updateInterval - systemTime <= 0) {
            //if forecastChoice = 1 (3h) perform both else only one call API
            int choice = Integer.parseInt(prefManager.getString("forecastChoice","1"));
            if (choice==1) {
                IHttpRequestForForecast forecastRequest = new OwmHttpRequestForForecast(getApplicationContext());
                forecastRequest.perform(lat, lon);
            }
            IHttpRequestForOneCallAPI forecastOneCallRequest = new OwmHttpRequestForOneCallAPI(getApplicationContext());
            forecastOneCallRequest.perform(lat,lon);
        }
    }

    private boolean isOnline() {
        try {
            InetAddress inetAddress = InetAddress.getByName("api.openweathermap.org");
            return inetAddress.isReachable(2000);
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    private void handleUpdateForecastAction(Intent intent) {
        int cityId = intent.getIntExtra(CITY_ID, -1);
        float lat =0;
        float lon =0;
        //get lat lon for cityID
        List<CityToWatch> citiesToWatch = dbHelper.getAllCitiesToWatch();
        for (int i = 0; i < citiesToWatch.size(); i++) {
            CityToWatch city = citiesToWatch.get(i);
            if (city.getCityId() == cityId) {
                lat = city.getLatitude();
                lon = city.getLongitude();
                break;
            }
        }
        handleUpdateForecastAction(intent, cityId, lat, lon);
    }
}
