package io.transmission_project.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.transmission_project.huntertalk.PrivacyPolicy;
import io.transmission_project.huntertalk.R;
import io.transmission_project.huntertalk.everythingWithGroups.InsideGroupActivity;
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
    private TextView resetPw;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupsRef;
    private FirebaseDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login);

        loginButton.setEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        resetPw = findViewById(R.id.resetpw);
        resetPw.setPaintFlags(resetPw.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        resetPw.setOnClickListener(new View.OnClickListener() {
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
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    moveToMainActivity();
                                } else {
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
                moveToPrivacyPolicy();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        groupsRef = database.getReference().child("groups");

        // Check if user is signed in (non-null).
        // If so, enter the home activity or group that the user was in already.
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){

            checkCurrentGroupAndRejoin(currentUser.getUid());

            moveToMainActivity();
        }


    }

    private void checkCurrentGroupAndRejoin(final String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("currentGroup")){

                    final String currentGroup = dataSnapshot.child("currentGroup").getValue().toString();

                    groupsRef.child(currentGroup).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //If somehow the group exists but is empty (due to some bug) delete the group.
                            if(!dataSnapshot.hasChild("joined")){
                                groupsRef.child(currentGroup).removeValue();
                                usersRef.child(uid).child("currentGroup").removeValue();
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
    }

    private void moveToMainActivity(){
        Intent intent = new Intent(LoginActivity.this, Home_page.class);
        startActivity(intent);
    }
    private void moveToPrivacyPolicy(){
        Intent intent = new Intent(LoginActivity.this, PrivacyPolicy.class);
        startActivity(intent);
    }
    private void moveToSignIn(){
        Intent intent = new Intent(LoginActivity.this, ResetPassword.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
