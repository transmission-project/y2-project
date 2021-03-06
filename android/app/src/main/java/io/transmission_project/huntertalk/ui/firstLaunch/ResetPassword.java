package io.transmission_project.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.transmission_project.huntertalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private Button sendEmail;
    private EditText enterEmail;
    private Boolean changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changed=false;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login help");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        /**
         * Functionality on the text field for entering the email to reset password for
         */
        enterEmail = findViewById(R.id.enterEmail);
        enterEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    changed=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**
         * Functionality of send email button
         */
        mAuth = FirebaseAuth.getInstance();
        sendEmail = findViewById(R.id.sendEmail);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = enterEmail.getText().toString().trim();
                if (emailAddress.equals("") || !emailAddress.contains("@") || !emailAddress.contains(".")){
                    enterEmail.setError("Invalid email address");
                    return;
                }

                //reset password method (defined by the Firebase)
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
                Toast message= Toast.makeText(ResetPassword.this, "Check your email. If you have an account at Hunter Talk you should have received a password reset link.",
                        Toast.LENGTH_LONG);
                        message.setGravity(Gravity.TOP, 0,0);
                        message.show();
                Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                startActivity(intent);
                ResetPassword.this.finish();
            }
        });
    }

    /**
     * Functionality of the back button with confirmation asking if any data was input by the user.
     */
    boolean secondPress =false;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if(!changed){
                    Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                if (secondPress){
                    Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                    startActivity(intent);}
                else{
                    Toast message= Toast.makeText(ResetPassword.this, "Press once again to cancel the reset",
                            Toast.LENGTH_LONG);
                    message.setGravity(Gravity.TOP, 0,0);
                    message.show();
                    secondPress=true;
                }}
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
