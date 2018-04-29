package com.logpht.wheremygang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountService accountService = new AccountService(LoginActivity.this.getApplicationContext());
                try {
                    //accountService.writeSavedAccount(new User("09009", "ehehehe", "asd", 1, null));

//                    User u = accountService.readSavedAccount();
//                    Log.d("------------", u.toString());

                    //Log.d("-----------", accountService.deleteSavedAccount() + "");

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("LoginActivity", "1");
                }
            }
        });
    }

}
