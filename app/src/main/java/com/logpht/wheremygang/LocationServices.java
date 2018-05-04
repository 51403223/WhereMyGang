package com.logpht.wheremygang;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;

import java.util.ArrayList;

/**
 * Created by Long on 28/04/2018.
 */

public class LocationServices extends Service implements LocationListener, IObjectObserver {
    private ArrayList<ILocationObserver> observers = new ArrayList<>(1);
    private String userID;
    private IBinder binder = new LocationServiceBinder();
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

    public void setUserID(String userID) {
        this.userID = userID;
    }

    //    private MyASyncTask myASyncTask;
//    public void stop() {
//        myASyncTask.setContinueLoop(false);
//    }
    public void sendUserLocation() {
        Log.d("locationservice", "sendUserLocation");
//        myASyncTask = new MyASyncTask(this.user);
//        myASyncTask.execute();

//        String url = "https://finalassandroid.000webhostapp.com/UpdateLocation.php";
//        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("locationservice", "response: " + response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("locationservice", "error");
//                error.printStackTrace();
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                HashMap<String, String> params = new HashMap<>(3);
//                params.put("IDUser", user.getPhone());
//                params.put("lati", String.valueOf(user.getLatitude()));
//                params.put("long", String.valueOf(user.getLongitude()));
//                return params;
//            }
//        };
//        requestQueue.start();
//        requestQueue.add(request);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("locationservice", "location change");
        this.notifyObservers(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void registerObserver(ILocationObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Notify all observers that data has changed
     * @param data - in this case, it's a Location object which defined by onLocationChange
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

//class MyASyncTask extends AsyncTask<Void, Void, Void> {
//    private User user;
//    private boolean continueLoop = true;
//
//    public MyASyncTask(User user) {
//        this.user = user;
//    }
//
//    public void setContinueLoop(boolean continueLoop) {
//        this.continueLoop = continueLoop;
//    }
//
//    @Override
//    protected Void doInBackground(Void... voids) {
//        while (continueLoop) {
//            user.setLatitude(user.getLatitude() + 1);
//            user.setLongitude(user.getLongitude() + 1);
//            Log.d("locationservice", String.format("%s - %s", user.getLatitude(), user.getLongitude()));
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    @Override
//    protected void onProgressUpdate(Void... values) {
//
//    }
//
//    @Override
//    protected void onPreExecute() {
//
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//
//    }
//
//
//}

