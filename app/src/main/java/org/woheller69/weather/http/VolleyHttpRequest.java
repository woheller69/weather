package org.woheller69.weather.http;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.woheller69.weather.weather_api.IProcessHttpRequest;


/**
 * This class implements the IHttpRequest interface. It provides HTTP requests by using Volley.
 * See: https://developer.android.com/training/volley/simple.html
 */
public class VolleyHttpRequest implements IHttpRequest {

    private Context context;
    private int cityId;

    /**
     * Constructor.
     *
     * @param context Volley needs a context "for creating the cache dir".
     * @see Volley#newRequestQueue(Context)
     */
    public VolleyHttpRequest(Context context, int cityId) {

        this.context = context;
        this.cityId = cityId;
    }

    /**
     * @see IHttpRequest#make(String, HttpRequestType, IProcessHttpRequest)
     */
    @Override
    public void make(String URL, HttpRequestType method, final IProcessHttpRequest requestProcessor) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // Set the request method
        int requestMethod;
        switch (method) {
            case POST:
                requestMethod = Request.Method.POST;
                break;
            case GET:
                requestMethod = Request.Method.GET;
                break;
            case PUT:
                requestMethod = Request.Method.PUT;
                break;
            case DELETE:
                requestMethod = Request.Method.DELETE;
                break;
            default:
                requestMethod = Request.Method.GET;
        }

        // Execute the request and handle the response
        StringRequest stringRequest = new StringRequest(requestMethod, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        requestProcessor.processSuccessScenario(response,cityId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestProcessor.processFailScenario(error);
                    }
                }
        );

        queue.add(stringRequest);
    }

}
