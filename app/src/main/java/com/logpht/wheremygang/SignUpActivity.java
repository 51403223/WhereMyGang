package com.logpht.wheremygang;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class SignUpActivity extends AppCompatActivity
        implements View.OnClickListener, Response.Listener, Response.ErrorListener {
    private EditText edTxtPhone, edTxtName, edTxtPass, edTxtConfirm;
    private Button btnSignUp;
    public static final int SIGNUP_SUCCESS = 0;
    public static final int SIGNUP_FAIL = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        this.edTxtPhone = findViewById(R.id.phoneSignUp);
        this.edTxtName = findViewById(R.id.nickname);
        this.edTxtPass = findViewById(R.id.pass);
        this.edTxtConfirm = findViewById(R.id.confirmPass);
        this.btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
    }

    /**
     * check if the password is not empty or password is the same with confirm password
     * @return
     * 0 - all fine
     * 1 - password is empty
     * 2 - password and confirm is not match
     */
    private int checkValidPassword() {
        // check empty password
        String password = this.edTxtPass.getText().toString();
        if (password.equals("")) {
            return 1;
        }
        // check matching
        String confirmPass = this.edTxtConfirm.getText().toString();
        if (!password.equals(confirmPass)) {
            return 2;
        }
        return 0;
    }

    /**
     * check if phone is in right number format
     * @return true if correct format
     * otherwise - false
     */
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

    /**
     * name must contain only alphabet characters and space
     * @return true if satisfied
     * otherwise - false
     */
    private boolean checkValidName() {
        String name = this.edTxtName.getText().toString();
        name = name.trim();
        if (name.equals("")) {
            return false;
        }
        for (char c : name.toCharArray()) {
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == ' ')) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // check valid phone number format
        if (!checkValidPhone()) {
            Toast.makeText(this, R.string.check_phone, Toast.LENGTH_LONG).show();
            return;
        }
        // check name format
        if (!checkValidName()) {
            Toast.makeText(this, R.string.check_name, Toast.LENGTH_LONG).show();
            return;
        }
        // check password
        switch (checkValidPassword()) {
            case 1:
                Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
                return;
            case 2:
                Toast.makeText(this, R.string.confirm_password_not_match, Toast.LENGTH_LONG).show();
                return;
        }

        AccountService accountService = new AccountService(this.getApplicationContext());
        accountService.signUp(this.edTxtPhone.getText().toString(), this.edTxtPass.getText().toString(),
                this.edTxtName.getText().toString(), this, this);
    }

    // handle error occurs while requesting server
    @Override
    public void onErrorResponse(VolleyError error) {
        // handle when error from server
        Toast.makeText(this, R.string.error_request_server, Toast.LENGTH_SHORT).show();
        Log.e("onErrorResponse", error.getMessage());
    }

    // handle server response
    @Override
    public void onResponse(Object response) {
        // handle server response
        if (response.equals(AccountService.RESULT_SUCCESS)) {
            // pass created user info to login page
            User user = new User();
            user.setName(this.edTxtName.getText().toString());
            user.setPhone(this.edTxtPhone.getText().toString());
            user.setPassword(this.edTxtPass.getText().toString());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("user", user);
            setResult(SIGNUP_SUCCESS, resultIntent);
            finish();
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setTitle(R.string.error_text)
                        .setMessage(R.string.signup_id_existed)
                        .setPositiveButton(R.string.ok_text, null)
                        .show();
            Log.d("signUp - onResponse", "sign up fail");
        }
    }
}
