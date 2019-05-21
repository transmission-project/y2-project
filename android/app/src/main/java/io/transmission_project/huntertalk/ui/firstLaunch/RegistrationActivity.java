package io.transmission_project.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.transmission_project.huntertalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegistrationActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputPasswordConfirm, inputNickname;
    private boolean changed;
    private boolean secondPress;
    private TextView login;
    private Button  registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registation);
        changed=false;
        secondPress=false;

        registerButton = (Button) findViewById(R.id.register2);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputPasswordConfirm = (EditText) findViewById(R.id.confirmPassword);
        inputNickname = (EditText) findViewById(R.id.displayName);
        login = (TextView) findViewById(R.id.login2);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

       addWatcherForChanges(inputEmail);
       addWatcherForChanges(inputPassword);
       addWatcherForChanges(inputPasswordConfirm);
       addWatcherForChanges(inputNickname);

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
                    inputEmail.setError("Invalid email address");
                    return;
                }
                if (email.equals("") || !email.contains("@") || !email.contains(".")){
                    inputEmail.setError("Invalid email address");
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
                }
                if (TextUtils.isEmpty(nickname)) {
                    inputPassword.setError("Nickname can't be empty");
                    return;
                }

                FirebaseAuth auth = FirebaseAuth.getInstance();
                //create user and finish with our registerFollowup listener
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistrationActivity.this,
                                new registerFollowup(RegistrationActivity.this,
                                        email, password, nickname));
            }
        });
    }

    /**
     * Makes user press twice to exit if anything was typed
     */
    @Override
    public void onBackPressed() {
                if(!changed){
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                if (secondPress){
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);}
                else{
                    Toast message= Toast.makeText(RegistrationActivity.this, "Press once again to cancel the registration",
                            Toast.LENGTH_LONG);
                    message.setGravity(Gravity.TOP, 0,0);
                    message.show();
                    secondPress=true;
                }}
        }

    //Adding text watcher to edit text fields
    private void addWatcherForChanges(EditText textField){
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    changed=true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

        /** If sign in fails, display a message to the user. If sign in succeeds
         *  the auth state listener will be notified and logic to handle the
         *  signed in user can be handled in the listener.
         */
        if (!task.isSuccessful()) {
            Toast.makeText(registrationActivity, "Authentication failed." + task.getException(),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference usersTable = database.getReference().child("users");
            final String uid = auth.getCurrentUser().getUid();
            usersTable.child(uid).child("nickname").setValue(nickname);
            usersTable.child(uid).child("email").setValue(email);
            // generate token for users when they register
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("fail", "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            usersTable.child(uid).child("fcmToken").setValue(token);
                        }
                    });

            Toast message = Toast.makeText(registrationActivity, "You have successfully registered. Redirecting to the main page.",
                    Toast.LENGTH_LONG);
            message.setGravity(Gravity.TOP, 0,0);
            message.show();
            registrationActivity.finish();
        }
    }
}
