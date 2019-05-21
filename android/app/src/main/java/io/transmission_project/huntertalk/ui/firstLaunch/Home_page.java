package io.transmission_project.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.transmission_project.huntertalk.userRelated.FriendList;
import io.transmission_project.huntertalk.R;
import io.transmission_project.huntertalk.userRelated.SettingsPage;
import io.transmission_project.huntertalk.everythingWithGroups.CreateGroupPage;
import io.transmission_project.huntertalk.everythingWithGroups.JoinAGroupById;

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

        /**
         * Functionality on button press of button join
         */
        LinearLayout bJoin = (LinearLayout) findViewById(R.id.buttonJoin);
        bJoin.setOnClickListener(new View.OnClickListener(){
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
        if (id == R.id.homePageSettings) {
            Intent i=new Intent(Home_page.this, SettingsPage.class);
            i.putExtra("previousPage", "homePage");
            startActivity(i);
            this.finish();
        }
        if (id == R.id.addFriends) {
            Intent i=new Intent(Home_page.this, FriendList.class);
            i.putExtra("previousPage", "homePage");
            startActivity(i);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //Back button is currently disabled from home page
    @Override
    public void onBackPressed() {
    }
}
