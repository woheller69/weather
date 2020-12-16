package org.woheller69.weather.weather_api.open_weather_map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.weather_api.IDataExtractor;
import org.woheller69.weather.weather_api.IProcessHttpRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * This class processes the HTTP requests that are made to the OpenWeatherMap API requesting the
 * current weather for all stored cities.
 */
public class ProcessOwmForecastRequest implements IProcessHttpRequest {

    /**
     * Constants
     */
    private final String DEBUG_TAG = "process_forecast";

    /**
     * Member variables
     */
    private Context context;
    private PFASQLiteHelper dbHelper;

    /**
     * Constructor.
     *
     * @param context The context of the HTTP request.
     */
    public ProcessOwmForecastRequest(Context context) {
        this.context = context;
        this.dbHelper = PFASQLiteHelper.getInstance(context);
    }

    /**
     * Converts the response to JSON and updates the database. Note that for this method no
     * UI-related operations are performed.
     *
     * @param response The response of the HTTP request.
     */
    @Override
    public void processSuccessScenario(String response) {
        IDataExtractor extractor = new OwmDataExtractor();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray list = json.getJSONArray("list");
            int cityId = json.getJSONObject("city").getInt("id");

            List<Forecast> forecasts = new ArrayList<>();

            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
            int choice = Integer.parseInt(prefManager.getString("forecastChoice","1"));

            if (choice==1) {  //5day 3h forecasts
                dbHelper.deleteForecastsByCityId(cityId); //start with empty forecast list
            } else{  //load 48 1h forecasts and then append the 3h forecasts
                forecasts = dbHelper.getForecastsByCityId(cityId);
            }

            // Continue with inserting new records

            for (int i = 0; i < list.length(); i++) {
                String currentItem = list.get(i).toString();
                Forecast forecast = extractor.extractForecast(currentItem);
                // Data were not well-formed, abort
                if (forecast == null) {
                    final String ERROR_MSG = context.getResources().getString(R.string.convert_to_json_error);
                    Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                }
                // Could retrieve all data, so proceed
                else {
                    if ((choice==1) || (forecast.getForecastTime()>forecasts.get(47).getForecastTime())) {  //if 5day/3h mode or if ForecastTime > last time from OneCallAPI
                        if(choice==2){ //at the position where 1h forecast changes to 3h forecast the precipitation shown in 3h forecast could be duplicate (already included in previous 1h forecasts, and therefore needs substraction)
                            if (forecast.getForecastTime()==(forecasts.get(47).getForecastTime()+60*60*1000)) forecast.setRainVolume(forecast.getRainValue()-forecasts.get(47).getRainValue()-forecasts.get(46).getRainValue());
                            if (forecast.getForecastTime()==(forecasts.get(47).getForecastTime()+2*60*60*1000)) forecast.setRainVolume(forecast.getRainValue()-forecasts.get(47).getRainValue());
                        }
                        forecast.setCity_id(cityId);
                        // add it to the database
                        dbHelper.addForecast(forecast);
                        forecasts.add(forecast);
                    }
                }
            }

            ViewUpdater.updateForecasts(forecasts);
            //again update Weekforecasts (new forecasts might change some rain weather symbols, see CityWeatherAdapter checkSun() )
            List<WeekForecast> weekforecasts=dbHelper.getWeekForecastsByCityId(cityId);
            ViewUpdater.updateWeekForecasts(weekforecasts);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows an error that the data could not be retrieved.
     *
     * @param error The error that occurred while executing the HTTP request.
     */
    @Override
    public void processFailScenario(final VolleyError error) {
        Handler h = new Handler(this.context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getResources().getString(R.string.error_fetch_forecast), Toast.LENGTH_LONG).show();
            }
        });
    }

}
