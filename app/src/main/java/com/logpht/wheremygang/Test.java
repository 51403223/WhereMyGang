package com.logpht.wheremygang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Test extends AppCompatActivity {
    private Button btnLogin;
    private TextView txtSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Login();
        SignUp();
    }
    public void SignUp(){
        txtSignUp = findViewById(R.id.txtsignup);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Test.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
    public void Login(){
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Test.this,MapActivity.class);
                startActivity(intent);
            }
        });
    }

}
