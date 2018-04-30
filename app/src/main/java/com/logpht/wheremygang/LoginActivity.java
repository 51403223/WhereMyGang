package com.logpht.wheremygang;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView txtSignUp;
    private EditText edTxtPhone, edTxtPassword;
    private Button btnLogin;
    private static final int SIGNUP_REQUEST_CODE = 1;
    private User user;
    private AccountService accountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.accountService = new AccountService(this);
        try {
            this.user = accountService.readSavedAccount();
            Log.e("LoginActivity", "Read file gang successfully");
        } catch (FileNotFoundException e) {
            Log.e("LoginActivity", "File gang doesn't exist");
            this.user = null;
        }
        setContentView(R.layout.login);
        this.txtSignUp = findViewById(R.id.txtsignup);
        txtSignUp.setOnClickListener(this);
        this.edTxtPhone = findViewById(R.id.txtPhoneNumber);
        this.edTxtPassword = findViewById(R.id.txtPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txtsignup) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, SIGNUP_REQUEST_CODE);
        }
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
                this.user = data.getParcelableExtra("user");
                // assign that info to fields
                this.edTxtPhone.setText(user.getPhone());
                this.edTxtPassword.setText(user.getPassword());
            }
        }
    }
}