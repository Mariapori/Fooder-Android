package fi.jesunmaailma.fooder.android.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class FooderDataService {
    Context context;

    public FooderDataService(Context context) {
        this.context = context;
    }

    public interface OnRestaurantDataResponse {
        void onResponse(JSONArray response);
        void onError(String error);
    }

    public interface OnRestaurantByIdDataResponse {
        void onResponse(JSONObject response);
        void onError(String error);
    }

    public interface OnFavouriteAddedRestaurantDataResponse {
        void onResponse(JSONObject response);
        void onError(String error);
    }

    public interface OnFavouriteDataResponse {
        void onResponse(JSONArray response);
        void onError(String error);
    }

    public void getRestaurants(String url, OnRestaurantDataResponse onDataResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                onDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onDataResponse.onError(error.getMessage());
            }
        });

        queue.add(arrayRequest);
    }

    public void getRestaurantById(String url, OnRestaurantByIdDataResponse onDataResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onDataResponse.onError(error.getMessage());
            }
        });

        queue.add(objectRequest);
    }

    public void getUserFavourites(String url, OnFavouriteDataResponse favouriteDataResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                favouriteDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                favouriteDataResponse.onError(error.getMessage());
            }
        });

        queue.add(arrayRequest);
    }

    // POST-pyyntö endpointtiin
    public void addRestaurantToFavourites(String url, OnFavouriteAddedRestaurantDataResponse addedRestaurantDataResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                addedRestaurantDataResponse.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                addedRestaurantDataResponse.onError(error.getMessage());
            }
        });

        queue.add(objectRequest);
    }
}
