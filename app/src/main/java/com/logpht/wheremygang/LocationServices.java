package com.logpht.wheremygang;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Long on 28/04/2018.
 */

public class LocationServices extends Service {
    private IBinder binder = new LocationBinder();
    private RequestQueue requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));

    @Override
    public void onCreate() {
        Log.d("locationservice", "oncreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("locationservice", "ondestroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("locationservice", "onbind");
        return this.binder;
    }

    public void sendUserLocation(final User user) {
        Log.d("locationservice", "sendUserLocation");
        String url = "https://finalassandroid.000webhostapp.com/UpdateLocation.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("locationservice", "response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("locationservice", "error");
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(3);
                params.put("IDUser", user.getPhone());
                params.put("lati", String.valueOf(user.getLatitude()));
                params.put("long", String.valueOf(user.getLongitude()));
                return params;
            }
        };
        requestQueue.start();
        requestQueue.add(request);
    }

    public class LocationBinder extends Binder {
        public LocationServices getLocationService() {
            return LocationServices.this;
        }
    }

}
