package com.logpht.wheremygang;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Long on 28/04/2018.
 */

public class LocationServices extends Service implements LocationListener, IObjectObserver {
    protected static final String host = "https://finalassandroid.000webhostapp.com";
    protected ArrayList<ILocationObserver> observers = new ArrayList<>(1);
    protected String userID; // id of current user
    private IBinder binder = new LocationServiceBinder();
    protected RequestQueue requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
    private static String tag = "Location Service";

    @Override
    public void onCreate() {
        Log.d(tag, "On create");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(tag, "On Destroy");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.d("location service", "onbind");
        return this.binder;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    private void sendUserLocation(final Location location) {
        Log.d(tag, "Sending user location to server");

        String url = host + "/UpdateLocation.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(tag, "Error send location");
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(3);
                params.put("IDUser", userID);
                params.put("lati", String.valueOf(location.getLatitude()));
                params.put("long", String.valueOf(location.getLongitude()));
                return params;
            }
        };
        requestQueue.start();
        requestQueue.add(request);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(tag, "Location changed. Continue to send them to server then update user location in MapActivity");
        sendUserLocation(location);
        this.notifyObservers(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d(tag, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d(tag, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.d(tag, "onProviderDisabled");
    }

    @Override
    public void registerObserver(ILocationObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Notify all observers that data has changed
     * @param data - in this case, it's a Location object which defined by {@link #onLocationChanged(Location)}
     */
    @Override
    public void notifyObservers(Object data) {
        for (ILocationObserver observer : this.observers) {
            observer.handleDataChange(data);
        }
    }

    public class LocationServiceBinder extends Binder {
        public LocationServices getLocationService() {
            return LocationServices.this;
        }
    }

}
