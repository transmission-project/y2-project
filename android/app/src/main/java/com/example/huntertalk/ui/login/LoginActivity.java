package com.example.huntertalk.ui.login;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huntertalk.CreateGroupPage;
import com.example.huntertalk.Home_page;
import com.example.huntertalk.R;
import com.example.huntertalk.RegistrationActivity;
import com.example.huntertalk.signIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.widget.TextView.*;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button registrationButton;
    private TextView resetpw;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign in");
        loginButton = findViewById(R.id.login);
        loginButton.setEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        resetpw = findViewById(R.id.resetpw);
        resetpw.setPaintFlags(resetpw.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        resetpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToSignIn();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameEditText.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    usernameEditText.setError("Invalid email address");
                    return;
                }
                String password = passwordEditText.getText().toString().trim();
                if (password.length()<6){
                   passwordEditText.setError("The password is at least 6 characters long");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    moveToMainActivity();
                                } else {
                                    // If sign in fails, display a message to the user.
                                       Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        registrationButton = findViewById(R.id.registerButton);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                moveToRegistrationActivity();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //FirebaseUser currentUser = null;
        if (currentUser!=null){
            moveToMainActivity();
        }
    }

   /* private void updateUiWithUser(LoggedInUserView model) {
        //String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }*/

    private void moveToMainActivity(){
        Intent intent = new Intent(LoginActivity.this, Home_page.class);
        startActivity(intent);
    }
    private void moveToRegistrationActivity(){
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
    private void moveToSignIn(){
        Intent intent = new Intent(LoginActivity.this, signIn.class);
        startActivity(intent);
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


}

