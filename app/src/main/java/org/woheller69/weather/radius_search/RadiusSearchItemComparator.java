package org.woheller69.weather.radius_search;

import java.util.Comparator;

/**
 * Compares weather data. First, the data are compared by the category where CLEAR_SKY is the best
 * category and THUNDERSTORM the worst (see IApiToDatabaseConversion#WeatherCategories. If the
 * categories are the same, compare the current temperatures.
 */
public class RadiusSearchItemComparator implements Comparator<RadiusSearchItem> {

    /**
     * @see Comparator#compare(Object, Object)
     * Return < 0 if lhs has a nicer weather than rhs.
     */
    @Override
    public int compare(RadiusSearchItem lhs, RadiusSearchItem rhs) {
        int categoryDifference = lhs.getWeatherCategory() - rhs.getWeatherCategory();
        if (categoryDifference == 0) {
            double temperatureDifference = lhs.getTemperature() - rhs.getTemperature();
            if (temperatureDifference == 0) {
                return 0;
            } else if (temperatureDifference < 0) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return categoryDifference;
        }
    }

}
