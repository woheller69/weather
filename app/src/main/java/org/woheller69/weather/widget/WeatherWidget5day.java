package org.woheller69.weather.widget;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

public class WeatherWidget5day extends AppWidgetProvider {

    public void updateAppWidget(Context context, final int appWidgetId) {

        PFASQLiteHelper db = PFASQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);

            Intent intent = new Intent(context, UpdateDataService.class);
            //Log.d("debugtag", "widget calls single update: " + cityID + " with widgetID " + appWidgetId);

            intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
            intent.putExtra("cityId", cityID);
            intent.putExtra(SKIP_UPDATE_INTERVAL, true);
            enqueueWork(context, UpdateDataService.class, 0, intent);
        }
    }

    public static int getWidgetCityID(Context context) {
        PFASQLiteHelper db = PFASQLiteHelper.getInstance(context);
        int cityID=0;
        List<CityToWatch> cities = db.getAllCitiesToWatch();
        int rank=cities.get(0).getRank();
        for (int i = 0; i < cities.size(); i++) {   //find cityID for first city to watch = lowest Rank
            CityToWatch city = cities.get(i);
            //Log.d("debugtag",Integer.toString(city.getRank()));
            if (city.getRank() <= rank ){
                rank=city.getRank();
                cityID = city.getCityId();
            }
         }
        return cityID;
}

    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, List<Forecast> forecasts, List<WeekForecast> weekforecasts) {

        int cityId=getWidgetCityID(context);
        PFASQLiteHelper database = PFASQLiteHelper.getInstance(context.getApplicationContext());
        int zoneseconds = database.getCurrentWeatherByCityId(cityId).getTimeZoneSeconds();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));


        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        int zonemilliseconds = zoneseconds*1000;

        int []forecastData = new int[5];
        String []weekday = new String[5];
        for (int i=0;i<5;i++){
            c.setTimeInMillis(weekforecasts.get(i).getForecastTime()+zonemilliseconds);
            int day = c.get(Calendar.DAY_OF_WEEK);
            weekday[i]=context.getResources().getString(StringFormatUtils.getDayShort(day));

            forecastData[i]=weekforecasts.get(i).getWeatherID();

                if ((forecastData[i] >= IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (forecastData[i] <= IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())) {
                    if (checkSun(weekforecasts.get(i).getForecastTime(),forecasts)) {
                        forecastData[i] = IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal(); //if at least one interval with sun +/-5 from noon, use shower rain instead of rain
                        if (getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts) < forecastData[i])
                            forecastData[i] = getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts); //if always sun use worst sun category
                    }
                }
                if ((forecastData[i] >= IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (forecastData[i] <= IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())) {
                    if (checkSun(weekforecasts.get(i).getForecastTime(),forecasts)) {
                        forecastData[i] = IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
                        if (getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts) < forecastData[i])
                            forecastData[i] = getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts);
                    }
                }
                if (forecastData[i] == IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()) {
                    if (checkSun(weekforecasts.get(i).getForecastTime(),forecasts)) {
                        forecastData[i] = IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
                        if (getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts) < forecastData[i])
                            forecastData[i] = getCorrectedWeatherID(weekforecasts.get(i).getForecastTime(),forecasts);
                    }
                }

        }

        views.setImageViewResource(R.id.widget_5day_image1, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[0], true));
        views.setImageViewResource(R.id.widget_5day_image2, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[1], true));
        views.setImageViewResource(R.id.widget_5day_image3, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[2], true));
        views.setImageViewResource(R.id.widget_5day_image4, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[3], true));
        views.setImageViewResource(R.id.widget_5day_image5, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[4], true));

        views.setTextViewText(R.id.widget_5day_day1,weekday[0]);
        views.setTextViewText(R.id.widget_5day_day2,weekday[1]);
        views.setTextViewText(R.id.widget_5day_day3,weekday[2]);
        views.setTextViewText(R.id.widget_5day_day4,weekday[3]);
        views.setTextViewText(R.id.widget_5day_day5,weekday[4]);

        views.setTextViewText(R.id.widget_5day_temp_max1, StringFormatUtils.formatTemperature(context,weekforecasts.get(0).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max2, StringFormatUtils.formatTemperature(context,weekforecasts.get(1).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max3, StringFormatUtils.formatTemperature(context,weekforecasts.get(2).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max4, StringFormatUtils.formatTemperature(context,weekforecasts.get(3).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max5, StringFormatUtils.formatTemperature(context,weekforecasts.get(4).getMaxTemperature()));

        views.setTextViewText(R.id.widget_5day_temp_min1, StringFormatUtils.formatTemperature(context,weekforecasts.get(0).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min2, StringFormatUtils.formatTemperature(context,weekforecasts.get(1).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min3, StringFormatUtils.formatTemperature(context,weekforecasts.get(2).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min4, StringFormatUtils.formatTemperature(context,weekforecasts.get(3).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min5, StringFormatUtils.formatTemperature(context,weekforecasts.get(4).getMinTemperature()));

        views.setImageViewResource(R.id.widget_5day_wind1,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(0).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind2,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(1).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind3,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(2).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind4,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(3).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind5,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(4).getWind_speed()));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        PFASQLiteHelper dbHelper = PFASQLiteHelper.getInstance(context);

        int widgetCityID=WeatherWidget.getWidgetCityID(context);

        List<Forecast> forecasts=dbHelper.getForecastsByCityId(widgetCityID);
        List<WeekForecast> weekforecasts=dbHelper.getWeekForecastsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget5day.class));

        for (int widgetID : widgetIDs) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_5day);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

            WeatherWidget5day.updateView(context, appWidgetManager, views, widgetID, city, forecasts,weekforecasts);
            appWidgetManager.updateAppWidget(widgetID, views);

        }
     }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    //this method fixes the problem that OpenWeatherMap will show a rain symbol for the whole day even if weather during day is great and there are just a few drops of rain during night
    private static boolean checkSun(long forecastTimeNoon, List<Forecast> forecastList ) {
        boolean sun=false;
        //iterate over FCs 5h before and 5h past forecast time of the weekforecast (which should usually be noon)
        for (Forecast fc : forecastList) {
            if ((fc.getForecastTime() >= forecastTimeNoon-18000000) && (fc.getForecastTime() <= forecastTimeNoon+18000000)) {
//                Log.d("ID",Integer.toString(fc.getWeatherID()));
                if (fc.getWeatherID() <= IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal()) sun = true;  //if weather better or equal broken clouds in one interval there is at least some sun during day.
            }
        }
        //       Log.d("ID",Boolean.toString(sun));
        return sun;
    }
    //this method fixes the problem that OpenWeatherMap will show a rain symbol for the whole day even if weather during day is great and there are just a few drops of rain during night
    private static Integer getCorrectedWeatherID(long forecastTimeNoon, List<Forecast> forecastList ) {

        int category=0;
        //iterate over FCs 5h before and 5h past forecast time of the weekforecast (which should usually be noon)
        for (Forecast fc : forecastList) {
            if ((fc.getForecastTime() >= forecastTimeNoon - 18000000) && (fc.getForecastTime() <= forecastTimeNoon + 18000000)) {
                //Log.d("Category",Integer.toString(fc.getWeatherID()));
                if (fc.getWeatherID() > category) {
                    category = fc.getWeatherID();  //find worst weather
                }
            }
        }
        //if worst is overcast clouds set category to broken clouds because fix is only used if checkSun=true, i.e. at least one interval with sun
        if (category==IApiToDatabaseConversion.WeatherCategories.OVERCAST_CLOUDS.getNumVal()) category=IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal();
        if (category>IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal()) category=1000;
        //Log.d("Category",Integer.toString(category));
        return category;
    }

}

