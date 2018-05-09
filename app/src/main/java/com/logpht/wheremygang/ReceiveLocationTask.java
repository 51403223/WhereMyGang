package com.logpht.wheremygang;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReceiveLocationTask extends AsyncTask<Integer,Void,Void> implements IObjectObserver {
    private ArrayList<ILocationObserver> observers = new ArrayList<>();
    private boolean continueLoop = true;
    private int interval;
    private Response.Listener responseListener;
    private Response.ErrorListener errorListener;
    protected RequestQueue requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
    private static final String tag = "ReceiveLocationTask";

    public ReceiveLocationTask(int interval, Response.Listener responseListener, Response.ErrorListener errorListener) {
        this.interval = interval;
        this.responseListener = responseListener;
        this.errorListener = errorListener;
    }

    public void stopTask() {
        Log.d(tag, "stoping task");
        this.continueLoop = false;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        Log.e(tag, "doInBackground with num of params = " + integers.length);
        int roomId = integers[0];
        while (continueLoop) {
            try {
                receiveLocations(roomId, responseListener, errorListener);
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(tag, "doInBackground done");
        return null;
    }

    public void receiveLocations(final int roomId, Response.Listener response, Response.ErrorListener errorListener) {
        Log.d(tag, "requesting server for locations of user in room id = " + roomId);
        String url = LocationServices.host + "/getLocationUserInRoom.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(3);
                params.put("IDRoom", String.valueOf(roomId));
                return params;
            }
        };
        requestQueue.start();
        requestQueue.add(request);
    }

    @Override
    public void registerObserver(ILocationObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Notify all observers that data has changed
     * @param data - in this case, it's User
     */
    @Override
    public void notifyObservers(Object data) {
        for (ILocationObserver observer : this.observers) {
            observer.handleDataChange(data);
        }
    }

    @Override
    public void notifyLocationConnectionLost() {
        for (ILocationObserver observer : this.observers) {
            observer.handleLocationConnectionLost();
        }
    }
}
