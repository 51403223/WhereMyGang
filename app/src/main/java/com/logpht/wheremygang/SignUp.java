package com.logpht.wheremygang;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity implements View.OnClickListener {
    EditText phoneNumber,nickName;
    Button btnSignUp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        phoneNumber = findViewById(R.id.phoneSignUp);
        nickName = findViewById(R.id.nickname);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        String url = "https://finalassandroid.000webhostapp.com/server.php";
        StringRequest sq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("web", "onResponse: -------------");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("web", error.getMessage());
            }
        }){
            protected Map<String,String> getParams(){
                Map<String,String> parr = new HashMap<String, String>();
//                parr.put("id",phoneNumber.getText().toString());
//                parr.put("name",nickName.getText().toString());
                parr.put("id","123");
                parr.put("name","123");
                return parr;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(sq);
        Toast.makeText(this, "Sucess", Toast.LENGTH_SHORT).show();
    }
}
