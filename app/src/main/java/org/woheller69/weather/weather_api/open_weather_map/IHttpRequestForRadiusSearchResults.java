package org.woheller69.weather.weather_api.open_weather_map;

/**
 * This generic interface is for making an HTTP request to some weather API for the radius search to
 * retrieve the results process the data and finally trigger some mechanism to update the UI.
 */
public interface IHttpRequestForRadiusSearchResults {

    /**
     * Makes an HTTP request to the weather API in order to retrieve the weather data for the
     * specified search criteria.
     */
    void perform();

}
