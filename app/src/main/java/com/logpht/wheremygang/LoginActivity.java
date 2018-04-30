package com.logpht.wheremygang;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, Response.Listener, Response.ErrorListener {
    private TextView txtSignUp;
    private EditText edTxtPhone, edTxtPassword;
    private Button btnLogin;
    private CheckBox chkBoxRemember;
    private User user;
    private AccountService accountService;
    private static final int SIGNUP_REQUEST_CODE = 1;
    private static final String WRONG_PHONE = "Wrong Phone";
    private static final String WRONG_PASSWORD = "Wrong Password";
    private static final String JSON_NAME_PARAM = "name";
    private static final String JSON_IDROOM_PARAM = "idRoom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.txtSignUp = findViewById(R.id.txtsignup);
        txtSignUp.setOnClickListener(this);
        this.edTxtPhone = findViewById(R.id.txtPhoneNumber);
        this.edTxtPhone.setOnTouchListener(new InputEditTextErrorInformer(this.edTxtPhone));
        this.edTxtPassword = findViewById(R.id.txtPassword);
        this.edTxtPassword.setOnTouchListener(new InputEditTextErrorInformer(this.edTxtPassword));
        this.btnLogin = findViewById(R.id.btnLogin);
        this.btnLogin.setOnClickListener(this);
        this.chkBoxRemember = findViewById(R.id.chkBoxRemember);
        // try to read saved account
        readFileGang();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.edTxtPhone.setSelection(this.edTxtPhone.getText().length());
        this.edTxtPassword.setSelection(this.edTxtPassword.getText().length());
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if (viewID == R.id.txtsignup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, SIGNUP_REQUEST_CODE);
        } else if (viewID == R.id.btnLogin) {
            // check phone format
            if (!checkValidPhone()) {
                Toast.makeText(this, R.string.check_phone, Toast.LENGTH_LONG).show();
                announceEditTextInputError(this.edTxtPhone);
                return;
            }
            // check empty password
            if (!checkValidPassword()) {
                Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
                announceEditTextInputError(this.edTxtPassword);
                return;
            }
            // request to server for login
            String id = this.edTxtPhone.getText().toString();
            String password = this.edTxtPassword.getText().toString();
            this.user.setPhone(id);
            this.user.setPassword(password);
            this.accountService.signIn(id, password, this, this);
        }
    }

    private void readFileGang() {
        this.accountService = new AccountService(this);
        try {
            this.user = accountService.readSavedAccount();
            Log.e("LoginActivity", "Read file gang successfully");
            this.edTxtPhone.setText(this.user.getPhone());
            this.edTxtPassword.setText(this.user.getPassword());
        } catch (FileNotFoundException e) {
            Log.e("LoginActivity", "File gang doesn't exist");
            this.user = new User();
        }
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
            Toast.makeText(this, R.string.login_wrong_phone, Toast.LENGTH_LONG).show();
        } else if (response.equals(WRONG_PASSWORD)) {
            Toast.makeText(this, R.string.login_wrong_password, Toast.LENGTH_LONG).show();
        } else {
            try {
                // parse data
                JSONObject jsonObject = new JSONObject((String) response);
                this.user.setJoiningRoomID(jsonObject.getInt(JSON_IDROOM_PARAM));
                this.user.setName(jsonObject.getString(JSON_NAME_PARAM));
                // write gang file as user want
                if (this.chkBoxRemember.isChecked()) {
                    writeGangFile();
                    Log.d("write gang", "executed");
                } else {
                    this.accountService.deleteSavedAccount();
                    Log.d("delete gang", "executed");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("login - response", "cant parse json object");
            }
        }
    }

    // handle error from server
    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, R.string.error_request_server, Toast.LENGTH_SHORT).show();
        Log.e("login - onError", "");
        error.printStackTrace();
    }
}