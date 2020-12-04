package org.woheller69.weather.weather_api.open_weather_map;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
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

            dbHelper.deleteForecastsByCityId(cityId);
            List<Forecast> forecasts = new ArrayList<>();
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
                    forecast.setCity_id(cityId);
                    // add it to the database
                    dbHelper.addForecast(forecast);
                    forecasts.add(forecast);
                }
            }
            /*
            //make current weather another Forecast because the data can be used
            CurrentWeatherData weatherData = dbHelper.getCurrentWeatherByCityId(cityId);
            Forecast current = new Forecast(0, cityId, weatherData.getTimestamp()*1000L,
                    weatherData.getTimestamp()*1000L, weatherData.getWeatherID(), weatherData.getTemperatureCurrent(),
                    weatherData.getHumidity(), weatherData.getPressure(), weatherData.getWindSpeed(),
                    weatherData.getWindDirection(), 0);
            forecasts.add(0,current);
            Log.d("forecast", "timestamp!: "+weatherData.getTimestamp());
            dbHelper.addForecast(current);
            */

            ViewUpdater.updateForecasts(forecasts);

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
