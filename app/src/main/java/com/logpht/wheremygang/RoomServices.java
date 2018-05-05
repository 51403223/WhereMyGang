package com.logpht.wheremygang;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RoomServices {
    private String host = "";

    public void createRoom(final String ownerID, final String nameRoom, final String passRoom,
                          Response.Listener responseListener, Response.ErrorListener errorListener) {
        String url = host + "/CreateRoom.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            // put params
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(5);
                params.put("ownerID", ownerID);
                params.put("nameRoom", nameRoom);
                params.put("passRoom", passRoom);
                return params;
            }
        };
        RequestQueue requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
        requestQueue.add(stringRequest);
    }


}
