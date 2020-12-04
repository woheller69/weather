package org.woheller69.weather.ui.updater;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.WeekForecast;

import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */
public interface IUpdateableCityUI {
    void processNewWeatherData(CurrentWeatherData data);

    void updateForecasts(List<Forecast> forecasts);

    void updateWeekForecasts(List<WeekForecast> forecasts);
}
