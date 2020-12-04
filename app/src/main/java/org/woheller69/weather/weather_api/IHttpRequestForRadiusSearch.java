package org.woheller69.weather.weather_api;

/**
 * This generic interface is for making an HTTP request to some weather API for the radius search,
 * process the data and finally trigger some mechanism to update the UI.
 */
public interface IHttpRequestForRadiusSearch {

    /**
     * @param cityId      The ID of the cityId to get the best weather locations around.
     * @param edgeLength  Determines the edge length of the square. The given cityId will be exactly
     *                    the center of the square.
     * @param resultCount Determines how many records shall be finally displayed in the UI.
     */
    void perform(int cityId, int edgeLength, int resultCount);

}
