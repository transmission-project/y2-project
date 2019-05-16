package com.example.huntertalk.ui.firstLaunch;

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

import com.example.huntertalk.R;
import com.example.huntertalk.everythingWithGroups.InsideGroupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private TextView registrationButton;
    private TextView resetpw;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private DatabaseReference usersRef, groupsRef;
    private FirebaseDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Sign in");
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

            /**
             *  Method that does the authentication (done by Firebase)
             */
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

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        groupsRef = database.getReference().child("groups");

        // Check if user is signed in (non-null) and update UI accordingly.
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){

            usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("currentGroup")){

                        final String currentGroup = dataSnapshot.child("currentGroup").getValue().toString();

                          groupsRef.child(currentGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                  if(!dataSnapshot.hasChild("joined")){
                                      groupsRef.child(currentGroup).removeValue();
                                  }else{
                                      Intent intent = new Intent(LoginActivity.this, InsideGroupActivity.class);
                                      intent.putExtra("groupID", currentGroup);
                                      startActivity(intent);
                                      LoginActivity.this.finish();
                                  }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {

                              }
                          });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

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
        Intent intent = new Intent(LoginActivity.this, ResetPassword.class);
        startActivity(intent);
    }
}

