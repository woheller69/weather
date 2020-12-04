package org.woheller69.weather.ui.updater;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.WeekForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */

public class ViewUpdater {
    private static List<IUpdateableCityUI> subscribers = new ArrayList<>();

    public static void addSubscriber(IUpdateableCityUI sub) {
        if (!subscribers.contains(sub)) {
            subscribers.add(sub);
        }
    }

    public static void removeSubscriber(IUpdateableCityUI sub) {
        subscribers.remove(sub);
    }

    public static void updateCurrentWeatherData(CurrentWeatherData data) {
        for (IUpdateableCityUI sub : subscribers) {
            sub.processNewWeatherData(data);
        }
    }

    public static void updateWeekForecasts(List<WeekForecast> forecasts) {
        for (IUpdateableCityUI sub : subscribers) {
            sub.updateWeekForecasts(forecasts);
        }
    }

    public static void updateForecasts(List<Forecast> forecasts) {
        for (IUpdateableCityUI sub : subscribers) {
            sub.updateForecasts(forecasts);
        }
    }
}
