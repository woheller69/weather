package org.woheller69.weather.weather_api.open_weather_map;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.preference.PreferenceManager;

import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.NavigationActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.weather_api.IDataExtractor;
import org.woheller69.weather.weather_api.IProcessHttpRequest;
import org.woheller69.weather.widget.WeatherWidget5day;
import static org.woheller69.weather.database.PFASQLiteHelper.getWidgetCityID;

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
    public void processSuccessScenario(String response, int cityId) {
        IDataExtractor extractor = new OwmDataExtractor();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray list = json.getJSONArray("list");
            JSONObject jsoncity = json.getJSONObject("city");
            JSONObject coord = jsoncity.getJSONObject("coord");
            float lat = (float)coord.getDouble("lat");
            float lon = (float)coord.getDouble("lon");
             //          Log.d("URL JSON",Float.toString(lat));
             //          Log.d("URL JSON",Float.toString(lon));

            ArrayList<Integer> CityIDList = new ArrayList<Integer>();

                List<Forecast> forecasts = new ArrayList<>();

                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
                int choice = Integer.parseInt(prefManager.getString("forecastChoice", "1"));

                if (choice == 1) {  //5day 3h forecasts
                    dbHelper.deleteForecastsByCityId(cityId); //start with empty forecast list
                } else {  //load 48 1h forecasts and then append the 3h forecasts
                    forecasts = dbHelper.getForecastsByCityId(cityId);
                    if (forecasts == null || forecasts.size() != 48) { //data from OneCallAPI not available even though it should
                        return;
                    }
                }

                // Continue with inserting new records

                for (int i = 0; i < list.length(); i++) {
                    String currentItem = list.get(i).toString();
                    Forecast forecast = extractor.extractForecast(currentItem);
                    // Data were not well-formed, abort
                    if (forecast == null) {
                        final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                        if (NavigationActivity.isVisible)
                            Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Could retrieve all data, so proceed
                    else {
                        if ((choice == 1) || (forecast.getForecastTime() > forecasts.get(47).getForecastTime())) {  //if 5day/3h mode or if ForecastTime > last time from OneCallAPI
                            if (choice == 2) { //at the position where 1h forecast changes to 3h forecast the precipitation shown in 3h forecast could be duplicate (already included in previous 1h forecasts, and therefore needs substraction)
                                if (forecast.getForecastTime() == (forecasts.get(47).getForecastTime() + 60 * 60 * 1000)) {
                                    if ((forecast.getPrecipitation() - forecasts.get(47).getPrecipitation() - forecasts.get(46).getPrecipitation()) >= 0) {
                                        forecast.setPrecipitation(forecast.getPrecipitation() - forecasts.get(47).getPrecipitation() - forecasts.get(46).getPrecipitation());
                                    } else
                                        forecast.setPrecipitation(0); //only in case of inconsistency of OWM 1h and 3h forecasts
                                }
                                if (forecast.getForecastTime() == (forecasts.get(47).getForecastTime() + 2 * 60 * 60 * 1000)) {
                                    if ((forecast.getPrecipitation() - forecasts.get(47).getPrecipitation()) >= 0) {
                                        forecast.setPrecipitation(forecast.getPrecipitation() - forecasts.get(47).getPrecipitation());
                                    } else
                                        forecast.setPrecipitation(0); //only in case of inconsistency of OWM 1h and 3h forecasts
                                }
                            }
                            forecast.setCity_id(cityId);
                            // add it to the database
                            dbHelper.addForecast(forecast);
                            forecasts.add(forecast);
                        }
                    }
                }

                ViewUpdater.updateForecasts(forecasts);
                //again update Weekforecasts and 5Day Widgets (new forecasts might change some rain weather symbols, see CityWeatherAdapter checkSun() )
                List<WeekForecast> weekforecasts = dbHelper.getWeekForecastsByCityId(cityId);
                ViewUpdater.updateWeekForecasts(weekforecasts);
                possiblyUpdate5DayWidgets(cityId, weekforecasts);

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
                if (NavigationActivity.isVisible) Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void possiblyUpdate5DayWidgets(int cityID, List<WeekForecast> weekforecasts) {
        //search for 5day widgets with same city ID
        int widget5dayCityID= getWidgetCityID(context);
        int[] widget5dayIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget5day.class));

        for (int widgetID : widget5dayIDs) {
            //check if city ID is same
            if (cityID == widget5dayCityID) {
                //perform update for the widget

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_5day);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(cityID);

                WeatherWidget5day.updateView(context, appWidgetManager, views, widgetID, city, weekforecasts);
                appWidgetManager.updateAppWidget(widgetID, views);
            }
        }

    }

}
