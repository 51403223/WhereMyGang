package com.logpht.wheremygang;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Long on 28/04/2018.
 */

public class LocationReceivingService extends LocationServices {
    private int roomID;
    private ReceiveLocationTask task;

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public void startReceiveLoop(int interval) {
        task = new ReceiveLocationTask(interval);
        task.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public void receiveLocations(Response.Listener response, Response.ErrorListener errorListener) {
        String url = host + "/getLocationUserInRoom.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(3);
                params.put("IDRoom", String.valueOf(roomID));
                return params;
            }
        };
        requestQueue.start();
        requestQueue.add(request);
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