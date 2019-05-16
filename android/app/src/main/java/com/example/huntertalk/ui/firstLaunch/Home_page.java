package com.example.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.huntertalk.LeaveGroupPopUp;
import com.example.huntertalk.userRelated.FriendList;
import com.example.huntertalk.R;
import com.example.huntertalk.userRelated.SettingsPage;
import com.example.huntertalk.everythingWithGroups.CreateGroupPage;
import com.example.huntertalk.everythingWithGroups.JoinAGroupById;

public class Home_page extends AppCompatActivity  {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    final private String uid = auth.getCurrentUser().getUid();
    private DatabaseReference usersRef, groupsRef;
    private FirebaseDatabase database;
    private TextView welcomeNickname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //getting instance of database and reference to fetch nickname of current user
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        groupsRef = database.getReference().child("groups");
        welcomeNickname = (TextView) findViewById(R.id.textView);

        //getting a single value from the database to set welcome nickname
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String nickname = dataSnapshot.child("nickname").getValue().toString();
                welcomeNickname.setText("Welcome " + String.valueOf(nickname) + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Functionality on button press of button join
         */
        LinearLayout bjoin = (LinearLayout) findViewById(R.id.buttonJoin);
        bjoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this, JoinAGroupById.class);
                startActivity(i);
                Home_page.this.finish();
            }
        });

        /**
         * Functionality on button press of create button
        */
        LinearLayout createButton = findViewById(R.id.buttonCreate);
        createButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this, CreateGroupPage.class);
                startActivity(i);
                Home_page.this.finish();
            }
        });
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mybutton) {
            Intent i=new Intent(Home_page.this, SettingsPage.class);
            startActivity(i);
            this.finish();
        }
        if (id == R.id.addFriends) {
            Intent i=new Intent(Home_page.this, FriendList.class);
            startActivity(i);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
