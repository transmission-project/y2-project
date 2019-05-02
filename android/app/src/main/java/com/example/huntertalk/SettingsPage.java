package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huntertalk.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends AppCompatActivity {
    private Boolean nicknameChange= false;
    private Boolean passwordChange= false;
    private Boolean password2Change= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Button logoutbutton = findViewById(R.id.logoutbutton);
        Button applyChanges= findViewById(R.id.apply);
        final EditText nickname = findViewById(R.id.etchangenickname);
        final EditText password = findViewById(R.id.etchangepw);
        final EditText confirmPassword = findViewById(R.id.etchangepw2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        applyChanges.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password1= password.getText().toString().trim();
                String nickname1= nickname.getText().toString().trim();
                String confirmPassword1=confirmPassword.getText().toString().trim();

                if(passwordChange==true||password2Change==true){
                    if (TextUtils.isEmpty(password1)) {
                        password.setError("Enter Password");
                        password2Change= false;
                        passwordChange= false;
                        return;
                    }
                    if (!TextUtils.equals(password1, confirmPassword1)) {
                        confirmPassword.setError("Passwords have to match");
                        password2Change= false;
                        passwordChange= false;
                        return;
                    }
                    if (password1.length() < 6) {
                        password.setError("Enter minimum 6 characters");
                        password2Change= false;
                        passwordChange= false;
                        return;
                    }
                    /**
                     * Add connection to DB
                     */
                }
                if (nicknameChange){
                    if (nickname1.length() == 0) {
                        nickname.setError("Nickname can't be empty");
                        nicknameChange=false;
                        return;
                    }
                    if (TextUtils.isEmpty(nickname1)) {
                        Toast.makeText(getApplicationContext(), "You must have a nickname.", Toast.LENGTH_SHORT).show();
                        nicknameChange=false;
                        return;
                    }

                    /**
                     * Add connection to DB
                     */
                }
                if(passwordChange==nicknameChange==password2Change==false){
                    Toast.makeText(getApplicationContext(), "No changes done to your data", Toast.LENGTH_LONG).show();
                }
                SettingsPage.this.finish();
            }

        }));
        nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    nicknameChange=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    passwordChange=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    password2Change=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



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
