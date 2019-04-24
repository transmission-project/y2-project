package com.example.huntertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.huntertalk.ui.login.LoginActivity;


public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

     final Button registerButton = findViewById(R.id.register2);
     registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveToLoginActivity();
            }
        });
    }

    private void moveToLoginActivity(){
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}
