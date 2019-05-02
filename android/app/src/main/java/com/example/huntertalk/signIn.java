package com.example.huntertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huntertalk.ui.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class signIn extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private Button sendEmail;
    private EditText enterEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reset password");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sendEmail = findViewById(R.id.sendEmail);
        enterEmail = findViewById(R.id.enterEmail);
        mAuth = FirebaseAuth.getInstance();
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = enterEmail.getText().toString().trim();
                if (emailAddress.equals("") || !emailAddress.contains("@") || !emailAddress.contains(".")){
                    enterEmail.setError("Invalid email address");
                    return;
                }
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
                Toast message= Toast.makeText(signIn.this, "Check your email. If you have an account at Hunter Talk you should have received a password reset link.",
                        Toast.LENGTH_LONG);
                        message.setGravity(Gravity.TOP, 0,0);
                        message.show();
                Intent intent = new Intent(signIn.this, LoginActivity.class);
                startActivity(intent);
                signIn.this.finish();
            }
        });
    }
    boolean secondPress =false;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (secondPress){
                    Intent intent = new Intent(signIn.this, LoginActivity.class);
                    startActivity(intent);}
                else{
                    Toast message= Toast.makeText(signIn.this, "Press once again to cancel the reset",
                            Toast.LENGTH_LONG);
                    message.setGravity(Gravity.TOP, 0,0);
                    message.show();
                    secondPress=true;
                }
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
