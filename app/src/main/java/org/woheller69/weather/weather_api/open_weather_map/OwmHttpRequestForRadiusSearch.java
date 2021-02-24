package org.woheller69.weather.weather_api.open_weather_map;

import android.content.Context;

import org.woheller69.weather.http.HttpRequestType;
import org.woheller69.weather.http.IHttpRequest;
import org.woheller69.weather.http.VolleyHttpRequest;
import org.woheller69.weather.weather_api.IHttpRequestForRadiusSearch;

/**
 * Implementation for the OpenWeatherMap API.
 */
public class OwmHttpRequestForRadiusSearch extends OwmHttpRequest implements IHttpRequestForRadiusSearch {

    /**
     * Member variables
     */
    private Context context;

    /**
     * Constructor.
     *
     * @param context The context to use.
     */
    public OwmHttpRequestForRadiusSearch(Context context) {
        this.context = context;
    }

    @Override
    public void perform(float lat, float lon, int edgeLength, int resultCount) {
        IHttpRequest httpRequest = new VolleyHttpRequest(context);
        final String URL = getUrlForQueryingSingleCity(context, lat, lon, false);
        httpRequest.make(URL, HttpRequestType.GET, new ProcessRadiusSearchRequest(context, edgeLength, resultCount));
    }

    public class OwmHttpRequestForResults extends OwmHttpRequest implements IHttpRequestForRadiusSearchResults {

        /**
         * Member variables
         */
        private Context context;
        private int resultCount;
        private double[] boundingBox;
        private int mapZoom;

        /**
         * Constructor.
         *
         * @param boundingBox The bounding box. Determines the search square. The first value in the
         *                    array is the left longitude, second bottom latitude, third right
         *                    longitude, fourth top latitude.
         * @param mapZoom     Defines the map zoom to use. For further details see the comment in
         *                    ProcessRadiusSearchRequest#processSuccessScenario
         */
        public OwmHttpRequestForResults(Context context, int resultCount, double[] boundingBox, int mapZoom) {
            this.context = context;
            this.resultCount = resultCount;
            this.boundingBox = boundingBox;
            this.mapZoom = mapZoom;
        }

        /**
         * @see IHttpRequestForRadiusSearchResults#perform()
         */
        @Override
        public void perform() {
            // In the second step the actual weather data are fetched and processed
            IHttpRequest httpRequest = new VolleyHttpRequest(context);
            final String URL = getUrlForQueryingRadiusSearch(context, boundingBox, mapZoom);
            httpRequest.make(
                    URL,
                    HttpRequestType.GET,
                    new ProcessRadiusSearchRequest(context, 0, 0).new ProcessRadiusSearchResultRequest(context, resultCount)
            );
        }

    }

}
