package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.huntertalk.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Button logoutbutton = findViewById(R.id.logoutbutton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        logoutbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();


            }
        });
    }
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
}
