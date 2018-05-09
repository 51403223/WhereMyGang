package com.logpht.wheremygang;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Long on 28/04/2018.
 */

public class AccountService {
    public static final String RESULT_SUCCESS = "success";
    private Context context;
    private String fullFileName;
    private static final String host = "https://finalassandroid.000webhostapp.com";

    public AccountService(Context context) {
        this.context = context;
        this.fullFileName = context.getFilesDir().getAbsolutePath() + "/gang";
    }

    public void signIn(final String userID, final String password, Response.Listener responseListener, Response.ErrorListener errorListener) {
        String url = host + "/getInfoUser.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(5);
                params.put("id", userID);
                params.put("pass", password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    public void signUp(final String id, final String password, final String name,
                         Response.Listener responseListener, Response.ErrorListener errorListener) {
        String url = host + "/signup.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, responseListener, errorListener) {
            // put params
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>(5);
                params.put("idSignUp", id);
                params.put("nameSignUp", name);
                params.put("passSignUp", password);
                params.put("longitude", "0");
                params.put("latitude", "0");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public boolean writeSavedAccount(User user) throws IOException {
        if (this.fullFileName != null) {
            FileWriter writer = new FileWriter(this.fullFileName);
            String s = user.getPhone() + "\n" +
                    user.getName() + "\n" +
                    user.getPassword() + "\n" +
                    user.getJoiningRoomID() + "\n" +
                    user.getJoiningRoomName();
            writer.write(s);
            writer.close();
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteSavedAccount() {
        if (this.fullFileName != null) {
            File file = new File(this.fullFileName);
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    public User readSavedAccount() throws FileNotFoundException {
        if (this.fullFileName != null) {
            Scanner scanner = new Scanner(new File(this.fullFileName));
            User user = new User();
            try {
                user.setPhone(scanner.nextLine());
                user.setName(scanner.nextLine());
                user.setPassword(scanner.nextLine());
                user.setJoiningRoomID(Integer.parseInt(scanner.nextLine()));
                user.setJoiningRoomName(scanner.nextLine());
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AccountService", "read saved account fail while reading file");
                return null;
            }
        } else {
            return null;
        }
    }
}
