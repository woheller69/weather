package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by yonjuni on 02.01.17.
 */

public class WeekWeatherAdapter extends RecyclerView.Adapter<WeekWeatherAdapter.WeekForecastViewHolder> {

    private Context context;
    private float[][] forecastData;

    WeekWeatherAdapter(float[][] forecastData, Context context) {
        this.context = context;
        this.forecastData = forecastData;
    }

    @Override
    public WeekForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_week_forecast, parent, false);
        return new WeekForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekForecastViewHolder holder, int position) {
        float[] dayValues = forecastData[position];
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        DecimalFormat decimalFormat = new DecimalFormat("0.0");


        setIcon((int) dayValues[9], holder.weather);
        holder.humidity.setText(StringFormatUtils.formatInt(dayValues[2],"%rh"));
        holder.precipitation.setText(StringFormatUtils.formatDecimal(dayValues[4],"mm"));
        holder.uv_index.setText(String.format("UV %s",StringFormatUtils.formatInt((int)(Math.round(dayValues[7])))));
        holder.wind_speed.setText(StringFormatUtils.formatWindSpeed(context, dayValues[5]));

        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        c.setTimeInMillis((long) dayValues[8]);
        int day = c.get(Calendar.DAY_OF_WEEK);

        holder.day.setText(StringFormatUtils.getDay(day));
        holder.temperature_max.setText(String.format("%s\u200a%s", decimalFormat.format(prefManager.convertTemperatureFromCelsius(dayValues[0])), prefManager.getWeatherUnit()));
        holder.temperature_min.setText(String.format("%s\u200a%s", decimalFormat.format(prefManager.convertTemperatureFromCelsius(dayValues[1])), prefManager.getWeatherUnit()));
    }

    @Override
    public int getItemCount() {
        return forecastData.length - 1;
    }

    class WeekForecastViewHolder extends RecyclerView.ViewHolder {

        TextView day;
        ImageView weather;
        TextView temperature_max;
        TextView temperature_min;
        TextView humidity;
        TextView wind_speed;
        TextView precipitation;
        TextView uv_index;

        WeekForecastViewHolder(View itemView) {
            super(itemView);

            day = itemView.findViewById(R.id.week_forecast_day);
            weather = itemView.findViewById(R.id.week_forecast_weather);
            temperature_max = itemView.findViewById(R.id.week_forecast_temperature_max);
            temperature_min = itemView.findViewById(R.id.week_forecast_temperature_min);
            humidity = itemView.findViewById(R.id.week_forecast_humidity);
            wind_speed = itemView.findViewById(R.id.week_forecast_wind_speed);
            precipitation = itemView.findViewById(R.id.week_forecast_precipitation);
            uv_index = itemView.findViewById(R.id.week_forecast_uv_index);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setIcon(int value, ImageView imageView) {
        imageView.setImageResource(UiResourceProvider.getIconResourceForWeatherCategory(value, true));
    }

}
