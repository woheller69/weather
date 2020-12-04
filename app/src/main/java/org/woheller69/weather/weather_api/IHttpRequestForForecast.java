package org.woheller69.weather.weather_api;

/**
 * This generic interface is for making an HTTP request to some weather API, process the data and
 * finally trigger some mechanism to update the UI.
 */
public interface IHttpRequestForForecast {

    /**
     * @param cityId The (OWM) ID of the city to get the data for.
     */
    void perform(int cityId);

}
