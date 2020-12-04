package org.woheller69.weather.weather_api;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.radius_search.RadiusSearchItem;

/**
 * This interface defines the frame of the functionality to extractCurrentWeatherData weather information from which
 * is returned by some API.
 */
public interface IDataExtractor {

    /**
     * Takes the response from the (web) server and checks whether the requested city was found.
     *
     * @param data The textual response from the server.
     * @return Returns true if the city was found or false otherwise (i. e. in case of 404).
     */
    boolean wasCityFound(String data);

    /**
     * @param data The data that contains the information to instantiate a CurrentWeatherData
     *             object. In the easiest case this is the (HTTP) response of the One Call API.
     * @return Returns the extracted information as a CurrentWeatherData instance.
     */
    CurrentWeatherData extractCurrentWeatherDataOneCall(String data);

    /**
     * Note that data shall contain information for instantiating <b>one</b> RadiusSearchItem
     * instance.
     *
     * @param data The data that provides the information that are necessary to create
     *             RadiusSearchItem instances.
     * @return Returns an RadiusSearchItem object that was created using the provided information.
     */
    RadiusSearchItem extractRadiusSearchItemData(String data);

    /**
     * @param data The data that contains the information to instantiate a Forecast object.
     * @return Returns the extracted weather forecast information. In case some error occurs, null
     * will be returned.
     */
    WeekForecast extractWeekForecast(String data);

    /**
     * @param data The data that contains the information to instantiate a Forecast object.
     * @return Returns the extracted weather forecast information. In case some error occurs, null
     * will be returned.
     */

    Forecast extractHourlyForecast(String data);

    /**
     * @param data The data that contains the information to instantiate a WeekForecast object.
     * @return Returns the extracted weather forecast information. In case some error occurs, null
     * will be returned.
     */
    Forecast extractForecast(String data);

    /**
     * @param data0, data1, data2, data3, data4 contain the information to retrieve the rain for a minute within the next 60min.
     * @return Returns a string with a rain drop in case of rain or a - in case of no rain
     */
    String extractRain60min(String data0,String data1, String data2, String data3, String data4);

    /**
     * @param data The data that contains the longitude and latitude to extract.
     * @return Returns an array where the first element is the latitude and the second the
     * longitude. In case an error occurs, an empty array will be returned.
     */
    double[] extractLatitudeLongitude(String data);

}
