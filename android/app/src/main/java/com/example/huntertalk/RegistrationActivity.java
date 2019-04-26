package com.example.huntertalk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPasswordConfirm;
    private Button btnSignIn, registerButton, btnResetPassword;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Registration");

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        registerButton = (Button) findViewById(R.id.register2);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputPasswordConfirm = (EditText) findViewById(R.id.confirmPassword);
        final EditText inputNickname = (EditText) findViewById(R.id.displayName);

        inputEmail.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                inputEmail.setText("");
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String passwordConfirm = inputPasswordConfirm.getText().toString().trim();
                String nickname = inputNickname.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Invalid email addresss");
                    return;
                }
                if (email.equals("") || !email.contains("@") || !email.contains(".")){
                    inputEmail.setError("Invalid email addresss");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Enter Password");
                    return;
                }
                if (!TextUtils.equals(password, passwordConfirm)) {
                    inputPasswordConfirm.setError("Passwords have to match");
                    return;
                }
                if (password.length() < 6) {
                    inputPassword.setError("Enter minimum 6 characters");
          
                    return;
                }if (nickname.length() == 0) {
                    inputPassword.setError("Enter minimum 6 characters");

                    return;
                }

                //create user and finish with our registerFollowup listener
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this,
                                new registerFollowup(RegistrationActivity.this,
                                        email, password, nickname));
            }
        });
    }

}

class registerFollowup implements OnCompleteListener<AuthResult> {

    private RegistrationActivity registrationActivity;
    private String email, password, nickname;

    public registerFollowup(RegistrationActivity registrationActivity, String email, String password, String nickname) {
        this.registrationActivity = registrationActivity;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        Toast.makeText(registrationActivity, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
        // If sign in fails, display a message to the user. If sign in succeeds
        // the auth state listener will be notified and logic to handle the
        // signed in user can be handled in the listener.
        if (!task.isSuccessful()) {
            Toast.makeText(registrationActivity, "Authentication failed." + task.getException(),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            FirebaseAuth auth = FirebaseAuth.getInstance();
            //store user info in realtime database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersTable = database.getReference().child("users");
            String uid = auth.getCurrentUser().getUid();

            usersTable.child(uid).child("nickname").setValue(nickname);
            usersTable.child(uid).child("email").setValue(email);
            Toast message= Toast.makeText(registrationActivity, "You have successfully registered. Redirecting to the main page.",
                    Toast.LENGTH_LONG);
            message.setGravity(Gravity.TOP, 0,0);
            message.show();
            registrationActivity.finish();
        }

    }
}
