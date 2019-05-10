package com.example.huntertalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home_page extends AppCompatActivity  {

    private Button bjoin;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    final private String uid = auth.getCurrentUser().getUid();
    private DatabaseReference usersRef;
    private FirebaseDatabase database;
    private TextView welcomeNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //getting instance of database and reference to fetch nickname of current user
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("users");
        welcomeNickname = (TextView) findViewById(R.id.textView);

        //getting a single value from the database
        usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String nickname = dataSnapshot.getValue().toString();
                welcomeNickname.setText("Welcome " + String.valueOf(nickname) + "!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bjoin = (Button) findViewById(R.id.bjoin);
        bjoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this,join_create.class);
                startActivity(i);
            }
        });

        Button createButton = findViewById(R.id.button3);
        createButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this,CreateGroupPage.class);
                startActivity(i);
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
            Intent i=new Intent(Home_page.this,SettingsPage.class);
            startActivity(i);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
