package com.example.huntertalk.everythingWithGroups;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.example.huntertalk.LeaveGroupPopUp;
import com.example.huntertalk.R;
import com.example.huntertalk.userRelated.SettingsPage;
import com.example.huntertalk.ui.firstLaunch.Home_page;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InsideGroupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference groupsRef;
    private String groupID;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new GroupOverviewFragment()).commit();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        groupsRef = database.getReference().child("groups");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();

        setContentView(R.layout.activity_inside_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        try {
            groupID = getIntent().getExtras().getString("groupID");
        }
        catch (NullPointerException e) {
            groupID = "ERROR";
        }
        startTrackerService();
    }
    // start the TrackerService//

    //Start the TrackerService//
    private void startTrackerService() {
        Intent service= new Intent(this, TrackingService.class);
        service.putExtra("groupID", groupID);
        service.putExtra("uid", uid);
        this.startService(service);
        System.out.println("The tracking has been enabled");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent i = new Intent(InsideGroupActivity.this, LeaveGroupPopUp.class);
            i.putExtra("groupID", groupID);
            i.putExtra("uid",uid);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inside_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i =  new Intent(InsideGroupActivity.this, SettingsPage.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_overview) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new GroupOverviewFragment()).commit();

        } else if (id == R.id.nav_map) {
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);
            MapFragment map= new MapFragment();
            map.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, map).commit();
           // i.putExtra("groupID", groupID);


        } else if (id == R.id.nav_chat) {

        } else if (id == R.id.nav_invite) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new InviteToGroupFragment()).commit();

        } else if (id == R.id.nav_settings) {
            Intent i =  new Intent(InsideGroupActivity.this, SettingsPage.class);
            startActivity(i);

            /**
             * Remove current user from the group (on pressing "Leave Group")
             */
        } else if (id == R.id.nav_leave) {
            groupsRef.child(groupID).child("joined").child(uid).removeValue();
            groupsRef.child(groupID).addListenerForSingleValueEvent((new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild("joined")){
                        groupsRef.child(groupID).child("invited").removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            }));
            Intent i =  new Intent(InsideGroupActivity.this, Home_page.class);
            this.finish();
            startActivity(i);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
