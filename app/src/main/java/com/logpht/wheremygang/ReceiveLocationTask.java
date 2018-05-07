package com.logpht.wheremygang;

import android.os.AsyncTask;

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
    protected RequestQueue requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));

    public ReceiveLocationTask(int interval) {
        this.interval = interval;
    }

    public void stopTask() {
        this.continueLoop = false;
    }

    @Override
    protected Void doInBackground(Integer... integers) {
        int roomId = integers[0];
        while (continueLoop) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void receiveLocations(final int roomId, Response.Listener response, Response.ErrorListener errorListener) {
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

    @Override
    public void notifyObservers(Object data) {
        for (ILocationObserver observer : this.observers) {
            observer.handleDataChange(data);
        }
    }
}
