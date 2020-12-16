package org.woheller69.weather.weather_api.open_weather_map;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

/**
 * This class implements the IApiToDatabaseConversion interface for the OpenWeatherMap API.
 */
public class OwmToDatabaseConversion extends IApiToDatabaseConversion {

    /**
     * @see IApiToDatabaseConversion#convertWeatherCategory(String)
     * https://openweathermap.org/weather-conditions
     */
    @Override
    public int convertWeatherCategory(String category) {
        int value = Integer.parseInt(category);
        if (value >= 200 && value <= 299) {
            return WeatherCategories.THUNDERSTORM.getNumVal();
        } else if (value >= 300 && value <= 399) {
            return WeatherCategories.DRIZZLE_RAIN.getNumVal();
        } else if (value == 500) {
            return WeatherCategories.LIGHT_RAIN.getNumVal();
        } else if (value == 501) {
            return WeatherCategories.MODERATE_RAIN.getNumVal();
        } else if ((value >= 502 && value <= 510 )|| (value >= 512 && value <= 519 ) || (value >=523 && value<=599)) {
            return WeatherCategories.RAIN.getNumVal();
        } else if (value >= 520 && value <= 522 ) {
            return WeatherCategories.SHOWER_RAIN.getNumVal();
        } else if (value == 600) {
            return WeatherCategories.LIGHT_SNOW.getNumVal();
        } else if (value == 601) {
            return WeatherCategories.SNOW.getNumVal();
        } else if ((value >= 602 && value <= 610 )|| (value >= 617 && value <= 619 ) || (value >=623 && value<=699)) {
            return WeatherCategories.HEAVY_SNOW.getNumVal();
        } else if (value >= 620 && value <= 622) {
            return WeatherCategories.SHOWER_SNOW.getNumVal();
        } else if ((value >= 611 && value <= 616)|| (value==511)) {
            return WeatherCategories.RAIN_SNOW.getNumVal();
        } else if (value >= 700 && value <= 799) {
            return WeatherCategories.MIST.getNumVal();
        } else if (value == 800) {
            return WeatherCategories.CLEAR_SKY.getNumVal();
        } else if (value == 801) {
            return WeatherCategories.FEW_CLOUDS.getNumVal();
        } else if (value == 802) {
            return WeatherCategories.SCATTERED_CLOUDS.getNumVal();
        } else if (value == 803) {
            return WeatherCategories.BROKEN_CLOUDS.getNumVal();
        } else if (value == 804) {
            return WeatherCategories.OVERCAST_CLOUDS.getNumVal();
        }
        // Fallback: Clouds
        return WeatherCategories.OVERCAST_CLOUDS.getNumVal();
    }

}
