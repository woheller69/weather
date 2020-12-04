package org.woheller69.weather.weather_api;

/**
 * This interface class defines a set of methods that guarantee that even the use of multiple APIs
 * result in the same data bases.
 */
public abstract class IApiToDatabaseConversion {

    /**
     * This enum provides a list of all available weather categories and assigns them a numerical
     * value. Please note that is ordered from best weather (CLEAR_SKY) to worst weather
     * (THUNDERSTORM).
     */
    public enum WeatherCategories {
        CLEAR_SKY(10),
        FEW_CLOUDS(20),
        SCATTERED_CLOUDS(30),
        BROKEN_CLOUDS(40),
        OVERCAST_CLOUDS(45),
        MIST(50),
        DRIZZLE_RAIN(60),
        LIGHT_RAIN(70),
        MODERATE_RAIN(71),
        RAIN(72),
        SHOWER_RAIN(75),
        LIGHT_SNOW(80),
        SNOW(81),
        HEAVY_SNOW(82),
        RAIN_SNOW(83),
        SHOWER_SNOW(85),
        THUNDERSTORM(90);

        private int numVal;

        WeatherCategories(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }

    }

    /**
     * @param value The value to get the enum label for.
     * @return Returns the label that belongs to the given value. Fallback value is CLOUDS.
     */
    public static WeatherCategories getLabelForValue(int value) {
        switch (value) {
            case 10:
                return WeatherCategories.CLEAR_SKY;
            case 20:
                return WeatherCategories.FEW_CLOUDS;
            case 30:
                return WeatherCategories.SCATTERED_CLOUDS;
            case 40:
                return WeatherCategories.BROKEN_CLOUDS;
            case 45:
                return WeatherCategories.OVERCAST_CLOUDS;
            case 50:
                return WeatherCategories.MIST;
            case 60:
                return WeatherCategories.DRIZZLE_RAIN;
            case 70:
                return WeatherCategories.LIGHT_RAIN;
            case 71:
                return WeatherCategories.MODERATE_RAIN;
            case 72:
                return WeatherCategories.RAIN;
            case 75:
                return WeatherCategories.SHOWER_RAIN;
            case 80:
                return WeatherCategories.LIGHT_SNOW;
            case 81:
                return WeatherCategories.SNOW;
            case 82:
                return WeatherCategories.HEAVY_SNOW;
            case 85:
                return WeatherCategories.SHOWER_SNOW;
            case 90:
                return WeatherCategories.THUNDERSTORM;
            default:
                return WeatherCategories.FEW_CLOUDS;
        }
    }

    /**
     * Different APIs will use different representation for weather conditions / categories.
     * Internally, they will stored uniformly. Implementation of this weather provide this
     * functionality.Category(jsonWeather.getString("main"));
     * forecast.setWeather
     *
     * @param category The category to convert into the internal representation.
     * @return Returns 10 for clear sky, 20 for (few) clouds, 30 for scattered cloud, 40 for broken
     * clouds, 50 for shower rain, 60 for rain, 70 for thunderstorm, 80 for snow, 90 for mist.
     */
    public abstract int convertWeatherCategory(String category);

}
