package org.woheller69.weather.weather_api.open_weather_map;

import android.content.Context;

import org.woheller69.weather.http.HttpRequestType;
import org.woheller69.weather.http.IHttpRequest;
import org.woheller69.weather.http.VolleyHttpRequest;
import org.woheller69.weather.weather_api.IHttpRequestForOneCallAPI;

/**
 * This class provides the functionality for making and processing HTTP requests to the
 * OpenWeatherMap to retrieve the latest weather data for all stored cities.
 */
public class OwmHttpRequestForOneCallAPI extends OwmHttpRequest implements IHttpRequestForOneCallAPI {

    /**
     * Member variables.
     */
    private Context context;

    /**
     * @param context The context to use.
     */
    public OwmHttpRequestForOneCallAPI(Context context) {
        this.context = context;
    }



    @Override
    public void perform(float lat, float lon, int cityId) {
        IHttpRequest httpRequest = new VolleyHttpRequest(context, cityId);
        final String URL = getUrlForQueryingOneCallAPI(context, lat, lon);
        httpRequest.make(URL, HttpRequestType.GET, new ProcessOwmForecastOneCallAPIRequest(context));
    }
}
