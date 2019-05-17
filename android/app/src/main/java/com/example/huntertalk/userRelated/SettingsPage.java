package com.example.huntertalk.userRelated;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.view.Gravity;
import android.view.MenuItem;

import com.example.huntertalk.R;
import com.example.huntertalk.everythingWithGroups.InsideGroupActivity;
import com.example.huntertalk.ui.firstLaunch.Home_page;
import com.example.huntertalk.ui.firstLaunch.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Potential ddos vulnarability updating current nickname to the same one. due to database access
 */
public class SettingsPage extends AppCompatActivity {
    private Boolean nicknameChange= false;
    private Boolean passwordChange= false;
    private Boolean password2Change= false;
    private DatabaseReference mDatabase;
    private String previousPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Button logoutbutton = findViewById(R.id.logoutbutton);
        Button applyChanges= findViewById(R.id.apply);
        Button addFriends = findViewById(R.id.addFriends);
        final EditText nickname = findViewById(R.id.etchangenickname);
        final EditText password = findViewById(R.id.etchangepw);
        final EditText confirmPassword = findViewById(R.id.etchangepw2);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        previousPage = getIntent().getExtras().getString("previousPage");

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    addFriends.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), FriendList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    });

        /**
         *  Conduct the required changes and update the values in the data base
         */
        applyChanges.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password1= password.getText().toString().trim();
               final String nickname1= nickname.getText().toString().trim();
                String confirmPassword1=confirmPassword.getText().toString().trim();

                /**
                 * Password related checks
                 */
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
                    auth.getCurrentUser().updatePassword(password1);
                }
                /**
                 *  Nickname related changes
                 */
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
                    if (TextUtils.isEmpty(nickname1)) {
                        Toast.makeText(getApplicationContext(), "You must have a nickname.", Toast.LENGTH_SHORT).show();
                        nicknameChange=false;
                        return;
                    }
                    mDatabase.child("users").child(uid).child("nickname").setValue(nickname1);
                    try {
                       String groupID = getIntent().getExtras().getString("groupNumber");
                       System.out.println("The group id is "+ groupID);
                       System.out.println("uid is "+ uid);
                       mDatabase.child("groups").child(groupID).child("joined").child(uid).setValue(nickname1);

                    }
                    catch (NullPointerException e) {
                        String groupID = "None";
                    }

                    /**
                    * Update nickname for all users that are friends with current user.
                     */
                    mDatabase.child("users").child(uid).child("friendOf").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot friendsOfThisPerson: dataSnapshot.getChildren()){
                                String friendId= friendsOfThisPerson.getKey();
                                mDatabase.child("users").child(friendId).child("friends").child(uid).setValue(nickname1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    /**
                     * Update nickname for all recently hunted lists containing this user.
                     */

                    mDatabase.child("users").child(uid).child("recentlyHuntedOf").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot friendsOfThisPerson: dataSnapshot.getChildren()){
                                String friendId= friendsOfThisPerson.getKey();
                                mDatabase.child("users").child(friendId).child("recentlyHunted").child(uid).setValue(nickname1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                if(passwordChange==false &&password2Change==false && nicknameChange==false){
                    Toast.makeText(getApplicationContext(), "No changes done to your data", Toast.LENGTH_LONG).show();
                }
                SettingsPage.this.finish();
            }

        }));

        /**
         *  Checks for user changes in the fields
         */
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

        /**
         * Logout button
         */
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();
            }
        });
    }
    /**
     * Logout functionality
     */
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    /**
     *  Functionality of the back button with leave confirmation if anything was changed
     */
    boolean secondPress =false;

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if(!nicknameChange&&!password2Change&&!passwordChange){
                   this.finish();
                }else{
                    if (secondPress){
                        this.finish();}
                    else{
                        Toast message= Toast.makeText(SettingsPage.this, "Press again to cancel the changes",
                                Toast.LENGTH_LONG);
                        message.setGravity(Gravity.TOP, 0,0);
                        message.show();
                        secondPress=true;
                    }}
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    //check which page we came from and go to that activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (previousPage.equals("insideGroupPage")){
            this.finish();
        }
        else {
            Intent intent = new Intent(SettingsPage.this, Home_page.class);
            startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
    }
}
