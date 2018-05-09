package com.logpht.wheremygang;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, Response.Listener, Response.ErrorListener {
    private TextView txtSignUp;
    private EditText edTxtPhone, edTxtPassword;
    private Button btnLogin;
    private CheckBox chkBoxRemember;
    private User user;
    private ProgressDialog spinner;
    private AccountService accountService;
    private static final int SIGNUP_REQUEST_CODE = 1;
    private static final String WRONG_PHONE = "Wrong Phone";
    private static final String WRONG_PASSWORD = "Wrong Password";
    private static final String JSON_NAME_PARAM = "name";
    private static final String JSON_IDROOM_PARAM = "idRoom";
    private static final String JSON_ROOM_NAME_PARAM = "nameRoom";
    private static final int MY_REQUEST_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("API >= 23", "request permission");
            requestForPermissions();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.accountService = new AccountService(this);
        this.txtSignUp = findViewById(R.id.txtsignup);
        txtSignUp.setOnClickListener(this);
        this.edTxtPhone = findViewById(R.id.txtPhoneNumber);
        this.edTxtPhone.setOnTouchListener(new InputEditTextErrorInformer(this.edTxtPhone));
        this.edTxtPassword = findViewById(R.id.txtPassword);
        this.edTxtPassword.setOnTouchListener(new InputEditTextErrorInformer(this.edTxtPassword));
        this.btnLogin = findViewById(R.id.btnLogin);
        this.btnLogin.setOnClickListener(this);
        this.chkBoxRemember = findViewById(R.id.chkBoxRemember);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String logout = intent.getStringExtra(MapActivity.LOGOUT);
        Log.e("Login", "App start from log out" + (logout == null));
        if (logout == null && readFileGang()) {
            // app started from launcher and read file success
            // try to login user choose stay signed in last time
            onClick(this.btnLogin);
        } else {
            // user loged out
            this.accountService.deleteSavedAccount();
            this.user = new User();
        }
        this.edTxtPhone.setSelection(this.edTxtPhone.getText().length());
        this.edTxtPassword.setSelection(this.edTxtPassword.getText().length());
    }

    @Override
    public void onClick(View v) {
        // check network available
        if (!checkNetworkEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.enable_network_text)
                    .setMessage(R.string.enable_network_message)
                    .setPositiveButton(R.string.ok_text, null)
                    .show();
            return;
        }

        int viewID = v.getId();
        if (viewID == R.id.txtsignup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, SIGNUP_REQUEST_CODE);
        } else if (viewID == R.id.btnLogin) {
            // check phone format
            if (!checkValidPhone()) {
                Toast.makeText(this, R.string.check_phone, Toast.LENGTH_SHORT).show();
                announceEditTextInputError(this.edTxtPhone);
                return;
            }
            // check empty password
            if (!checkValidPassword()) {
                Toast.makeText(this, R.string.empty_password, Toast.LENGTH_SHORT).show();
                announceEditTextInputError(this.edTxtPassword);
                return;
            }
            // check location available
            if (!checkGPSEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.enable_gps_text)
                        .setMessage(R.string.enable_gps_message)
                        .setPositiveButton(R.string.ok_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent gpsIntent = new Intent();
                                gpsIntent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(gpsIntent);
                            }
                        })
                        .show();
                return;
            }
            // request to server for login
            String id = this.edTxtPhone.getText().toString();
            String password = this.edTxtPassword.getText().toString();
            this.user.setPhone(id);
            this.user.setPassword(password);
            // animate logining
            this.spinner = new ProgressDialog(this);
            spinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            spinner.setIndeterminate(true);
            spinner.setMessage(getResources().getString(R.string.logining));
            spinner.setCanceledOnTouchOutside(false);
            spinner.show();
            // request server
            this.accountService.signIn(id, password, this, this);
        }
    }

    private boolean readFileGang() {
        try {
            this.user = accountService.readSavedAccount();
            Log.e("LoginActivity", "Read file gang successfully");
            this.edTxtPhone.setText(this.user.getPhone());
            this.edTxtPassword.setText(this.user.getPassword());
            return true;
        } catch (FileNotFoundException e) {
            Log.e("LoginActivity", "File gang doesn't exist");
            this.user = new User();
        } catch (Exception e) {
            Log.e("LoginActivity", "Other error when read file gang");
            this.user = new User();
        }
        return false;
    }

    private void writeGangFile() {
        try {
            this.accountService.writeSavedAccount(this.user);
        } catch (IOException e) {
            Log.e("login - writeGangFile", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkValidPhone() {
        String phone = this.edTxtPhone.getText().toString();
        phone = phone.trim();
        if (phone.equals("")) {
            return false;
        }
        for (char digit : phone.toCharArray()) {
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
    }

    private boolean checkValidPassword() {
        String password = this.edTxtPassword.getText().toString();
        return password.equals("") ? false : true;
    }

    private void announceEditTextInputError(EditText editText) {
        editText.setBackgroundColor(getResources().getColor(R.color.colorRedPink));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGNUP_REQUEST_CODE) {
            if (resultCode == SignUpActivity.SIGNUP_SUCCESS) {
                // show dialog to announce
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage(R.string.login_after_signup)
                        .setPositiveButton(R.string.ok_text, null)
                        .show();
                // get created user info from sign up page
                this.user = data.getParcelableExtra(SignUpActivity.EXTRA_NAME);
                // assign that info to fields
                this.edTxtPhone.setText(user.getPhone());
                this.edTxtPassword.setText(user.getPassword());
            }
        }
    }

    // handle server response
    @Override
    public void onResponse(Object response) {
        if (response.equals(WRONG_PHONE)) {
            this.spinner.dismiss();
            Toast.makeText(this, R.string.login_wrong_phone, Toast.LENGTH_LONG).show();
        } else if (response.equals(WRONG_PASSWORD)) {
            this.spinner.dismiss();
            Toast.makeText(this, R.string.login_wrong_password, Toast.LENGTH_LONG).show();
        } else {
            try {
                // parse data
                JSONObject jsonObject = new JSONObject((String) response);
                this.user.setJoiningRoomID(jsonObject.getInt(JSON_IDROOM_PARAM));
                this.user.setName(jsonObject.getString(JSON_NAME_PARAM));
                this.user.setJoiningRoomName(jsonObject.getString(JSON_ROOM_NAME_PARAM));
                // write gang file as user want
                if (this.chkBoxRemember.isChecked()) {
                    writeGangFile();
                    Log.d("write gang", "executed");
                } else {
                    this.accountService.deleteSavedAccount();
                    Log.d("delete gang", "executed");
                }
                // navigate to map page
                Intent mapIntent = new Intent(this, MapActivity.class);
                mapIntent.putExtra("user", this.user);
                startActivity(mapIntent);
                finish();
                //Toast.makeText(this, "Login success", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("login - response", "cant parse json object");
                Toast.makeText(this, "Login fail", Toast.LENGTH_LONG).show();
            } finally {
                this.spinner.dismiss();
            }
        }
    }

    // handle error from server
    @Override
    public void onErrorResponse(VolleyError error) {
        this.spinner.dismiss();
        Toast.makeText(this, R.string.error_request_server, Toast.LENGTH_LONG).show();
        Log.e("login - onError", "-------------");
        error.printStackTrace();
    }

    private void requestForPermissions() {
        // request location permission for api >= 23
        ArrayList<String> permissionList = new ArrayList<>(3);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permissionList.size() > 0) {
            String[] permissions = new String[permissionList.size()];
            for (int i = 0; i < permissionList.size(); i++) {
                permissions[i] = permissionList.get(i);
            }
            ActivityCompat.requestPermissions(this, permissions, MY_REQUEST_PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_REQUEST_PERMISSIONS_CODE) {
            // permission denied
            // exit the app
            if (grantResults.length == 0 ) {
                exitApp();
            } else {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        exitApp();
                    }
                }

            }
        }
    }

    private void exitApp() {
        finish();
    }


    private boolean checkNetworkEnabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkGPSEnabled() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}