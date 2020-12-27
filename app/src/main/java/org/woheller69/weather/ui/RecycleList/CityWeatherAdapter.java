package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.LineChartView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {
    private static final String TAG = "Forecast_Adapter";

    private int[] dataSetTypes;
    private List<Forecast> courseDayList;
    private float[][] forecastData;

    private Context context;

    private CurrentWeatherData currentWeatherDataList;

    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int WEEK = 2;
    public static final int DAY = 3;
    public static final int CHART = 4;
    public static final int ERROR = 5;

    public CityWeatherAdapter(CurrentWeatherData currentWeatherDataList, int[] dataSetTypes, Context context) {
        this.currentWeatherDataList = currentWeatherDataList;
        this.dataSetTypes = dataSetTypes;
        this.context = context;

        PFASQLiteHelper database = PFASQLiteHelper.getInstance(context.getApplicationContext());

        List<Forecast> forecasts = database.getForecastsByCityId(currentWeatherDataList.getCity_id());
        List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());

        updateForecastData(forecasts);
        updateWeekForecastData(weekforecasts);

    }

    // function update 3-hour or 1-hour forecast list
    public void updateForecastData(List<Forecast> forecasts) {

        courseDayList = new ArrayList<Forecast>();

        long threehoursago = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        long onehourago = System.currentTimeMillis() - (1 * 60 * 60 * 1000);

        if (forecasts.size() >= 48) {  //2day 1-hour forecast
                for (Forecast f : forecasts) {
                    if (f.getForecastTime() >= onehourago) {
                        courseDayList.add(f);
                    }
                }
        } else if (forecasts.size() == 40) {  //5day 3-hour forecast
                for (Forecast f : forecasts) {
                    if (f.getForecastTime() >= threehoursago) {
                        courseDayList.add(f);
                    }
                }
            }
            notifyDataSetChanged();
    }

    // function for week forecast list
    public void updateWeekForecastData(List<WeekForecast> forecasts) {
        if (forecasts.isEmpty()) {
            Log.d("devtag", "######## forecastlist empty");
            forecastData = new float[][]{new float[]{0}};
            return;
        }

        int cityId = forecasts.get(0).getCity_id();

        PFASQLiteHelper dbHelper = PFASQLiteHelper.getInstance(context.getApplicationContext());
        int zonemilliseconds = dbHelper.getCurrentWeatherByCityId(cityId).getTimeZoneSeconds() * 1000;

        //temp max 0, temp min 1, humidity 2, pressure 3, precipitation 4, wind 5, wind direction 6, uv_index 7, time 8, weather ID 9, number of FCs for day 10
        float[] today = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> todayIDs = new LinkedList<>();
        float[] tomorrow = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> tomorrowIDs = new LinkedList<>();
        float[] in2days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in2daysIDs = new LinkedList<>();
        float[] in3days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in3daysIDs = new LinkedList<>();
        float[] in4days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in4daysIDs = new LinkedList<>();
        float[] in5days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in5daysIDs = new LinkedList<>();
        float[] in6days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in6daysIDs = new LinkedList<>();
        float[] in7days = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LinkedList<Integer> in7daysIDs = new LinkedList<>();
        float[] empty = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  //last field is not displayed otherwise
        LinkedList<Integer> emptyIDs = new LinkedList<>();

        forecastData = new float[][]{today, tomorrow, in2days, in3days, in4days, in5days, in6days,in7days,empty};

        today[0]=forecasts.get(0).getMaxTemperature();
        today[1]=forecasts.get(0).getMinTemperature();
        today[2]=forecasts.get(0).getHumidity();
        today[3]=forecasts.get(0).getPressure();
        today[4]=forecasts.get(0).getPrecipitation();
        today[5]=forecasts.get(0).getWind_speed();
        today[6]=forecasts.get(0).getWind_direction();
        today[7]=forecasts.get(0).getUv_index();
        today[8]=forecasts.get(0).getForecastTime()+zonemilliseconds;
        today[9]=forecasts.get(0).getWeatherID();
        if ((today[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (today[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(0).getForecastTime())) {
                today[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((today[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (today[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(0).getForecastTime())) {
                today[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (today[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(0).getForecastTime())) {
                today[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        today[10]=1;

        tomorrow[0]=forecasts.get(1).getMaxTemperature();
        tomorrow[1]=forecasts.get(1).getMinTemperature();
        tomorrow[2]=forecasts.get(1).getHumidity();
        tomorrow[3]=forecasts.get(1).getPressure();
        tomorrow[4]=forecasts.get(1).getPrecipitation();
        tomorrow[5]=forecasts.get(1).getWind_speed();
        tomorrow[6]=forecasts.get(1).getWind_direction();
        tomorrow[7]=forecasts.get(1).getUv_index();
        tomorrow[8]=forecasts.get(1).getForecastTime()+zonemilliseconds;
        tomorrow[9]=forecasts.get(1).getWeatherID();
        if ((tomorrow[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (tomorrow[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(1).getForecastTime())) {
                tomorrow[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((tomorrow[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (tomorrow[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(1).getForecastTime())) {
                tomorrow[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (tomorrow[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(1).getForecastTime())) {
                tomorrow[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        tomorrow[10]=1;

        in2days[0]=forecasts.get(2).getMaxTemperature();
        in2days[1]=forecasts.get(2).getMinTemperature();
        in2days[2]=forecasts.get(2).getHumidity();
        in2days[3]=forecasts.get(2).getPressure();
        in2days[4]=forecasts.get(2).getPrecipitation();
        in2days[5]=forecasts.get(2).getWind_speed();
        in2days[6]=forecasts.get(2).getWind_direction();
        in2days[7]=forecasts.get(2).getUv_index();
        in2days[8]=forecasts.get(2).getForecastTime()+zonemilliseconds;
        in2days[9]=forecasts.get(2).getWeatherID();
        if ((in2days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (in2days[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(2).getForecastTime())) {
                in2days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((in2days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (in2days[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(2).getForecastTime())) {
                in2days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (in2days[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(2).getForecastTime())) {
                in2days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        in2days[10]=1;

        in3days[0]=forecasts.get(3).getMaxTemperature();
        in3days[1]=forecasts.get(3).getMinTemperature();
        in3days[2]=forecasts.get(3).getHumidity();
        in3days[3]=forecasts.get(3).getPressure();
        in3days[4]=forecasts.get(3).getPrecipitation();
        in3days[5]=forecasts.get(3).getWind_speed();
        in3days[6]=forecasts.get(3).getWind_direction();
        in3days[7]=forecasts.get(3).getUv_index();
        in3days[8]=forecasts.get(3).getForecastTime()+zonemilliseconds;
        in3days[9]=forecasts.get(3).getWeatherID();
        if ((in3days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (in3days[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(3).getForecastTime())) {
                in3days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((in3days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (in3days[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(3).getForecastTime())) {
                in3days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (in3days[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(3).getForecastTime())) {
                in3days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        in3days[10]=1;

        in4days[0]=forecasts.get(4).getMaxTemperature();
        in4days[1]=forecasts.get(4).getMinTemperature();
        in4days[2]=forecasts.get(4).getHumidity();
        in4days[3]=forecasts.get(4).getPressure();
        in4days[4]=forecasts.get(4).getPrecipitation();
        in4days[5]=forecasts.get(4).getWind_speed();
        in4days[6]=forecasts.get(4).getWind_direction();
        in4days[7]=forecasts.get(4).getUv_index();
        in4days[8]=forecasts.get(4).getForecastTime()+zonemilliseconds;
        in4days[9]=forecasts.get(4).getWeatherID();
        if ((in4days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (in4days[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(4).getForecastTime())) {
                in4days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((in4days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (in4days[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(4).getForecastTime())) {
                in4days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (in4days[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(4).getForecastTime())) {
                in4days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        in4days[10]=1;

        in5days[0]=forecasts.get(5).getMaxTemperature();
        in5days[1]=forecasts.get(5).getMinTemperature();
        in5days[2]=forecasts.get(5).getHumidity();
        in5days[3]=forecasts.get(5).getPressure();
        in5days[4]=forecasts.get(5).getPrecipitation();
        in5days[5]=forecasts.get(5).getWind_speed();
        in5days[6]=forecasts.get(5).getWind_direction();
        in5days[7]=forecasts.get(5).getUv_index();
        in5days[8]=forecasts.get(5).getForecastTime()+zonemilliseconds;
        in5days[9]=forecasts.get(5).getWeatherID();
        if ((in5days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_RAIN.getNumVal()) && (in5days[9]<=IApiToDatabaseConversion.WeatherCategories.RAIN.getNumVal())){
            if (checkSun(cityId,forecasts.get(5).getForecastTime())) {
                in5days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN.getNumVal();
            }
        }
        if ((in5days[9]>=IApiToDatabaseConversion.WeatherCategories.LIGHT_SNOW.getNumVal()) && (in5days[9]<=IApiToDatabaseConversion.WeatherCategories.HEAVY_SNOW.getNumVal())){
            if (checkSun(cityId,forecasts.get(5).getForecastTime())) {
                in5days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_SNOW.getNumVal();
            }
        }
        if (in5days[9]==IApiToDatabaseConversion.WeatherCategories.RAIN_SNOW.getNumVal()){
            if (checkSun(cityId,forecasts.get(5).getForecastTime())) {
                in5days[9]=IApiToDatabaseConversion.WeatherCategories.SHOWER_RAIN_SNOW.getNumVal();
            }
        }
        in5days[10]=1;

        in6days[0]=forecasts.get(6).getMaxTemperature();
        in6days[1]=forecasts.get(6).getMinTemperature();
        in6days[2]=forecasts.get(6).getHumidity();
        in6days[3]=forecasts.get(6).getPressure();
        in6days[4]=forecasts.get(6).getPrecipitation();
        in6days[5]=forecasts.get(6).getWind_speed();
        in6days[6]=forecasts.get(6).getWind_direction();
        in6days[7]=forecasts.get(6).getUv_index();
        in6days[8]=forecasts.get(6).getForecastTime()+zonemilliseconds;
        in6days[9]=forecasts.get(6).getWeatherID();
        in6days[10]=1;

        in7days[0]=forecasts.get(7).getMaxTemperature();
        in7days[1]=forecasts.get(7).getMinTemperature();
        in7days[2]=forecasts.get(7).getHumidity();
        in7days[3]=forecasts.get(7).getPressure();
        in7days[4]=forecasts.get(7).getPrecipitation();
        in7days[7]=forecasts.get(7).getWind_speed();
        in7days[6]=forecasts.get(7).getWind_direction();
        in7days[7]=forecasts.get(7).getUv_index();
        in7days[8]=forecasts.get(7).getForecastTime()+zonemilliseconds;
        in7days[9]=forecasts.get(7).getWeatherID();
        in7days[10]=1;

        notifyDataSetChanged();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    public class OverViewHolder extends ViewHolder {
        TextView temperature;
        ImageView weather;
        TextView sun;

        OverViewHolder(View v) {
            super(v);
            this.temperature = v.findViewById(R.id.activity_city_weather_temperature);
            this.weather = v.findViewById(R.id.activity_city_weather_image_view);
            this.sun=v.findViewById(R.id.activity_city_weather_sun);
        }
    }

    public class DetailViewHolder extends ViewHolder {
        TextView humidity;
        TextView pressure;
        TextView windspeed;
        TextView rain60min;
        TextView time;

        DetailViewHolder(View v) {
            super(v);
            this.humidity = v.findViewById(R.id.activity_city_weather_tv_humidity_value);
            this.pressure = v.findViewById(R.id.activity_city_weather_tv_pressure_value);
            this.windspeed = v.findViewById(R.id.activity_city_weather_tv_wind_speed_value);
            this.rain60min = v.findViewById(R.id.activity_city_weather_tv_rain60min_value);
            this.time=v.findViewById(R.id.activity_city_weather_title);
        }
    }

    public class WeekViewHolder extends ViewHolder {
        RecyclerView recyclerView;

        WeekViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_week);
            recyclerView.setHasFixedSize(true);
        }
    }

    public class DayViewHolder extends ViewHolder {
        RecyclerView recyclerView;
        TextView recyclerViewHeader;

        DayViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_course_day);
            recyclerView.setHasFixedSize(true);
            recyclerViewHeader=v.findViewById(R.id.recycler_view_header);
        }
    }

    public class ChartViewHolder extends ViewHolder {
        TextView sunrise;
        TextView sunset;
        LineChartView lineChartView;
        BarChartView barChartView;

        ChartViewHolder(View v) {
            super(v);
            this.lineChartView = v.findViewById(R.id.graph_temperature);
            this.barChartView = v.findViewById(R.id.graph_precipitation);
        }
    }

    public class ErrorViewHolder extends ViewHolder {
        ErrorViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == OVERVIEW) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_overview, viewGroup, false);

            return new OverViewHolder(v);

        } else if (viewType == DETAILS) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_details, viewGroup, false);
            return new DetailViewHolder(v);

        } else if (viewType == WEEK) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_week, viewGroup, false);
            return new WeekViewHolder(v);

        } else if (viewType == DAY) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_day, viewGroup, false);
            return new DayViewHolder(v);

        } else if (viewType == CHART) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_chart, viewGroup, false);
            return new ChartViewHolder(v);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_error, viewGroup, false);
            return new ErrorViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        boolean isDay = currentWeatherDataList.getTimestamp() >currentWeatherDataList.getTimeSunrise() && currentWeatherDataList.getTimestamp() < currentWeatherDataList.getTimeSunset();

        if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            //correct for timezone differences
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            Date riseTime = new Date((currentWeatherDataList.getTimeSunrise() + zoneseconds) * 1000L);
            Date setTime = new Date((currentWeatherDataList.getTimeSunset() + zoneseconds) * 1000L);
            holder.sun.setText("\u2600\u25b2 " + timeFormat.format(riseTime) + " \u25bc " + timeFormat.format(setTime));

            setImage(currentWeatherDataList.getWeatherID(), holder.weather, isDay);

            holder.temperature.setText(StringFormatUtils.formatTemperature(context, currentWeatherDataList.getTemperatureCurrent()));

        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;

            long time = currentWeatherDataList.getTimestamp();
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date updateTime = new Date((time + zoneseconds) * 1000L);

            holder.time.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_details_heading), dateFormat.format(updateTime)));
            holder.humidity.setText(StringFormatUtils.formatInt(currentWeatherDataList.getHumidity(), "%rh"));
            holder.pressure.setText(StringFormatUtils.formatDecimal(currentWeatherDataList.getPressure(), " hPa"));
            holder.windspeed.setText(StringFormatUtils.formatWindSpeed(context, currentWeatherDataList.getWindSpeed()) + " " + StringFormatUtils.formatWindDir(context, currentWeatherDataList.getWindDirection()));
            holder.rain60min.setText(currentWeatherDataList.getRain60min());

        } else if (viewHolder.getItemViewType() == WEEK) {

            WeekViewHolder holder = (WeekViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            WeekWeatherAdapter adapter = new WeekWeatherAdapter(forecastData, context);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

        } else if (viewHolder.getItemViewType() == DAY) {

            DayViewHolder holder = (DayViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            CourseOfDayAdapter adapter = new CourseOfDayAdapter(courseDayList, context,holder.recyclerViewHeader,holder.recyclerView);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

        } else if (viewHolder.getItemViewType() == CHART) {
            ChartViewHolder holder = (ChartViewHolder) viewHolder;

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            PFASQLiteHelper database = PFASQLiteHelper.getInstance(context.getApplicationContext());
            AppPreferencesManager prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this.context));
            List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());

            if (weekforecasts.isEmpty()) {
                Log.d("devtag", "######## forecastlist empty");
                return;
            }

            float tmin=1000;
            float tmax=-1000;

            float pmax=0;

            LineSet datasetmax = new LineSet();
            LineSet datasetmin = new LineSet();
            LineSet xaxis = new LineSet(); //create own x-axis as the x-axis of the chart crosses the y-axis numbers. Does not look good

            BarSet precipitationDataset = new BarSet();

            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT"));
            int zonemilliseconds = currentWeatherDataList.getTimeZoneSeconds()*1000;

            for (int i=0 ; i< weekforecasts.size();i++) {
                c.setTimeInMillis(weekforecasts.get(i).getForecastTime()+zonemilliseconds);
                int day = c.get(Calendar.DAY_OF_WEEK);
                float temp_max=weekforecasts.get(i).getMaxTemperature();
                float temp_min=weekforecasts.get(i).getMinTemperature();
                float precip=weekforecasts.get(i).getPrecipitation();

                if ((i == 0) || (i == (weekforecasts.size()-1 ))) {  // 1 bar at begin and end for alignment with temperature line chart (first day starts at noon, last ends at noon)
                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);
                    //x-labels for precipitation dataset must be there and cannot be empty even though they are made invisible below. Otherwise alignment gets destroyed!
                    datasetmax.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_max));
                    datasetmin.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_min));


                } else { // 2 bars in the middle for alignment with temperature line chart

                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);
                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);

                    datasetmax.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_max));
                    datasetmin.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_min));
                }

                if (prefManager.convertTemperatureFromCelsius(temp_max)>tmax) tmax=prefManager.convertTemperatureFromCelsius(temp_max);
                if (prefManager.convertTemperatureFromCelsius(temp_min)<tmin) tmin=prefManager.convertTemperatureFromCelsius(temp_min);
                if (precip>pmax) pmax=precip;
            }

            tmax++;  //add some space above and below
            tmin--;
            int mid = Math.round((tmin + tmax) / 2);
            int step = Math.max(1, (int) Math.ceil(Math.abs(tmax - tmin) / 4));  //step size for y-axis

            for (int i=0 ; i< weekforecasts.size();i++) {
                xaxis.addPoint("",mid-2*step);   //create x-axis at position of min y-axis value
            }

            ArrayList<ChartSet> temperature = new ArrayList<>();
            temperature.add(datasetmax);
            temperature.add(datasetmin);
            temperature.add(xaxis);

            datasetmax.setColor(context.getResources().getColor(R.color.red));
            datasetmax.setThickness(6);
            datasetmax.setSmooth(true);
            datasetmax.setFill(context.getResources().getColor(R.color.middlegrey));

            datasetmin.setColor(context.getResources().getColor(R.color.lightblue));
            datasetmin.setThickness(6);
            datasetmin.setSmooth(true);
            datasetmin.setFill(context.getResources().getColor(R.color.backgroundBlue)); //fill with background, so only range between curves is visible

            xaxis.setThickness(3);
            xaxis.setColor(context.getResources().getColor(R.color.colorPrimaryDark));

            ArrayList<ChartSet> precipitation = new ArrayList<>();
            precipitation.add((precipitationDataset));

            precipitationDataset.setColor(context.getResources().getColor(R.color.blue));
            precipitationDataset.setAlpha(0.8f);  // make precipitation bars transparent

            holder.lineChartView.addData(temperature);
            holder.lineChartView.setAxisBorderValues( mid-2*step, mid+2*step);
            holder.lineChartView.setStep(step);
            holder.lineChartView.setXAxis(false);
            holder.lineChartView.setYAxis(false);
            holder.lineChartView.setYLabels(AxisController.LabelPosition.INSIDE);  //must be INSIDE! OUTSIDE will destroy alignment with precipitation bar chart
            holder.lineChartView.setLabelsColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.lineChartView.setAxisColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.lineChartView.setFontSize((int) Tools.fromDpToPx(17));
            holder.lineChartView.setBorderSpacing(Tools.fromDpToPx(30));

            holder.lineChartView.show();

            holder.barChartView.addData(precipitation);
            holder.barChartView.setBarSpacing(0);
            holder.barChartView.setAxisBorderValues(0,(int) Math.max(10,pmax*2));  //scale down in case of high precipitation, limit to lower half of chart
            holder.barChartView.setXAxis(false);
            holder.barChartView.setYAxis(false);
            holder.barChartView.setYLabels(AxisController.LabelPosition.NONE); //no labels for precipitation
            holder.barChartView.setLabelsColor(0);  //transparent color, make labels invisible
            holder.barChartView.setAxisColor(context.getResources().getColor(R.color.colorPrimaryDark));
            holder.barChartView.setFontSize((int) Tools.fromDpToPx(17));
            holder.barChartView.setBorderSpacing(Tools.fromDpToPx(30));

            holder.barChartView.show();
        }
        //No update for error needed
    }

    public void setImage(int value, ImageView imageView, boolean isDay) {
        imageView.setImageResource(UiResourceProvider.getImageResourceForWeatherCategory(value, isDay));
    }

    //this method fixes the problem that OpenWeatherMap will show a rain symbol for the whole day even if weather during day is great and there are just a few drops of rain during night
    private boolean checkSun(int cityId, long forecastTimeNoon ) {
        PFASQLiteHelper dbHelper = PFASQLiteHelper.getInstance(context);
        List<Forecast> forecastList = dbHelper.getForecastsByCityId(cityId);

        boolean sun=false;
        //iterate over FCs 4h before and 4h past forecast time of the weekforecast (which should usually be noon)
        for (Forecast fc : forecastList) {
            if ((fc.getForecastTime() >= forecastTimeNoon-14400000) && (fc.getForecastTime() <= forecastTimeNoon+14400000)) {
//                Log.d("ID",Integer.toString(fc.getWeatherID()));
                if (fc.getWeatherID() <= IApiToDatabaseConversion.WeatherCategories.BROKEN_CLOUDS.getNumVal()) sun = true;  //if weather better or equal broken clouds in one interval there is at least some sun during day.
            }
        }
 //       Log.d("ID",Boolean.toString(sun));
        return sun;
    }


    @Override
    public int getItemCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }
}