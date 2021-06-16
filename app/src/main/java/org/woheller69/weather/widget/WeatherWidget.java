package org.woheller69.weather.widget;


import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static androidx.core.app.JobIntentService.enqueueWork;

import static java.lang.Boolean.TRUE;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

public class WeatherWidget extends AppWidgetProvider {
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;

    public void updateAppWidget(Context context, final int appWidgetId) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        PFASQLiteHelper db = PFASQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);
            if(prefManager.getBoolean("pref_GPS", true)==TRUE) updateLocation(context, cityID);
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

    public static void updateLocation(final Context context, int cityID) {
        PFASQLiteHelper db = PFASQLiteHelper.getInstance(context);
        List<CityToWatch> cities = db.getAllCitiesToWatch();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                CityToWatch city;
                double lat = Math.round(locationGPS.getLatitude()*100.0)/100.0;  //round 2 digits
                double lon = Math.round(locationGPS.getLongitude()*100.0)/100.0; //round 2 digits
                for (int i=0; i<cities.size();i++){
                    if (cities.get(i).getCityId()==cityID) {
                        city = cities.get(i);
                        city.setLatitude((float) lat);
                        city.setLongitude((float) lon);
                        city.setCityName(String.format(Locale.getDefault(),"%.2f° / %.2f°", lat, lon));
                        //Toast.makeText(context.getApplicationContext(), String.format("%.2f / %.2f", lat, lon), Toast.LENGTH_SHORT).show();
                        db.updateCityToWatch(city);

                        break;
                    }
                }
            } else Toast.makeText(context.getApplicationContext(),R.string.error_no_position,Toast.LENGTH_SHORT).show();
        }
    }



    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, CurrentWeatherData weatherData, List<WeekForecast> weekforecasts, List<Forecast> hourlyforecasts) {

        long time = weatherData.getTimestamp();
        int zoneseconds = weatherData.getTimeZoneSeconds();
        int [] forecastIDs = {0,R.id.widget_1h, R.id.widget_2h,R.id.widget_3h,R.id.widget_4h,R.id.widget_5h,R.id.widget_6h,R.id.widget_7h, R.id.widget_8h};

        long updateTime = (time + zoneseconds) * 1000;

        long riseTime = (weatherData.getTimeSunrise() + zoneseconds) * 1000;
        long setTime = (weatherData.getTimeSunset() + zoneseconds) * 1000;

        views.setTextViewText(R.id.widget_updatetime, String.format("(%s)", StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
        views.setTextViewText(R.id.widget_temperature, " "+StringFormatUtils.formatTemperature(context, weatherData.getTemperatureCurrent())+" ");
        views.setViewPadding(R.id.widget_temperature,1,1,1,1);
        views.setTextViewText(R.id.widget_rain60min,"☔  "+weatherData.getRain60min());
        views.setTextViewText(R.id.widget_city_name, city.getCityName());
        views.setTextViewText(R.id.widget_wind, " "+StringFormatUtils.formatWindSpeed(context,weatherData.getWindSpeed())+" ");
        views.setInt(R.id.widget_wind,"setBackgroundResource",StringFormatUtils.widgetColorWindSpeed(context,weatherData.getWindSpeed()));
        views.setViewPadding(R.id.widget_wind,1,1,1,1);
        views.setTextViewText(R.id.widget_sunrise_sunset,"\u2600\u25b2 " + StringFormatUtils.formatTimeWithoutZone(context, riseTime) + " \u25bc " + StringFormatUtils.formatTimeWithoutZone(context, setTime));
        views.setTextViewText(R.id.widget_UVindex,"UV");
        views.setInt(R.id.widget_UVindex,"setBackgroundResource",StringFormatUtils.widgetColorUVindex(context,Math.round(weekforecasts.get(0).getUv_index())));

        boolean isDay = weatherData.getTimestamp()  > weatherData.getTimeSunrise() && weatherData.getTimestamp() < weatherData.getTimeSunset();

        views.setImageViewResource(R.id.widget_image_view, UiResourceProvider.getIconResourceForWeatherCategory(weatherData.getWeatherID(), isDay));

        if (hourlyforecasts!=null&&!hourlyforecasts.isEmpty())
        {
            for (int i=1;i<9;i++){
                Calendar forecastTime = Calendar.getInstance();
                forecastTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                forecastTime.setTimeInMillis(hourlyforecasts.get(i).getLocalForecastTime(context));

                Calendar sunSetTime = Calendar.getInstance();
                sunSetTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                sunSetTime.setTimeInMillis(weatherData.getTimeSunset() * 1000 + weatherData.getTimeZoneSeconds() * 1000);
                sunSetTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));


                Calendar sunRiseTime = Calendar.getInstance();
                sunRiseTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                sunRiseTime.setTimeInMillis(weatherData.getTimeSunrise() * 1000 + weatherData.getTimeZoneSeconds() * 1000);
                sunRiseTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));

                isDay = forecastTime.after(sunRiseTime) && forecastTime.before(sunSetTime);
                views.setImageViewResource(forecastIDs[i],UiResourceProvider.getIconResourceForWeatherCategory(hourlyforecasts.get(i).getWeatherID(), isDay));
                views.setViewVisibility(forecastIDs[i], View.VISIBLE);
            }
        } else {
            for (int i=1;i<9;i++){
                views.setViewVisibility(forecastIDs[i], View.GONE);
            }
        }

        Intent intentUpdate = new Intent(context, WeatherWidget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_update, pendingUpdate);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Log.d("GPS", "Widget onUpdate");
            if(prefManager.getBoolean("pref_GPS", true)==TRUE && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && !powerManager.isPowerSaveMode()) {
                if (locationListenerGPS==null) {
                    Log.d("GPS", "Listener null");
                    locationListenerGPS = new LocationListener() {
                        @Override
                        public void onLocationChanged(android.location.Location location) {
                            // There may be multiple widgets active, so update all of them
                            Log.d("GPS", "Location changed");
                            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class)); //IDs Might have changed since last call of onUpdate
                            for (int appWidgetId : appWidgetIds) {
                                updateAppWidget(context, appWidgetId);
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 3000, locationListenerGPS);  //Update every 10 min, min distance 5km
                }
            }else {
                Log.d("GPS","Remove Updates");
                if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
                locationListenerGPS=null;
            }

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

        CurrentWeatherData currentWeather=dbHelper.getCurrentWeatherByCityId(widgetCityID);
        List<WeekForecast> weekforecasts=dbHelper.getWeekForecastsByCityId(widgetCityID);
        List<Forecast> hourlyforecasts=dbHelper.getForecastsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class));

        for (int widgetID : widgetIDs) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

                WeatherWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather,weekforecasts, hourlyforecasts);
                appWidgetManager.updateAppWidget(widgetID, views);

        }
     }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.d("GPS", "Last widget removed");
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
        locationListenerGPS=null;
    }

}

