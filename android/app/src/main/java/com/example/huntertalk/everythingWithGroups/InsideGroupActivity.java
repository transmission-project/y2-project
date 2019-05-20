package com.example.huntertalk.everythingWithGroups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
import com.google.firebase.database.annotations.NotNull;


public class InsideGroupActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, VoipFragment.OnFragmentInteractionListener {

    private DatabaseReference groupsRef, usersRef;
    private String groupID;
    private String uid;
    private Intent service;
    static final int POP_UP_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new GroupExceptionHandler(this));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new GroupOverviewFragment()).commit();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        groupsRef = database.getReference().child("groups");
        usersRef= database.getReference().child("users");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        //Clear the Recently hunted list
        usersRef.child(uid).child("recentlyHunted").removeValue();

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
            finish();
        }

        //Voip
        startVoipFragment(savedInstanceState);

        //Map
        startTrackerService();
    }
    // start the TrackerService//

    //Start the TrackerService//
    private void startTrackerService() {
        service= new Intent(this, TrackingService.class);
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
            Intent intent = new Intent(InsideGroupActivity.this, LeaveGroupPopUp.class);
            intent.putExtra("groupID", groupID);
            intent.putExtra("uid",uid);
            startActivityForResult(intent, POP_UP_REQUEST);

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
        //put extra to inform of which the previous activity was
        if (id == R.id.action_settings) {
            Intent i =  new Intent(InsideGroupActivity.this, SettingsPage.class);
            i.putExtra("previousPage", "insideGroupPage");
            i.putExtra("groupNumber", groupID);
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


        } else if (id == R.id.nav_chat) {

        } else if (id == R.id.nav_invite) {
            fragmentManager.beginTransaction().replace(R.id.content_frame, new InviteToGroupFragment()).commit();

        } else if (id == R.id.nav_settings) {
            Intent i =  new Intent(InsideGroupActivity.this, SettingsPage.class);
            i.putExtra("previousPage", "insideGroupPage");
            i.putExtra("groupNumber", groupID);
            startActivity(i);

            /*
             * Remove current user from the group (on pressing "Leave Group")
             */
        } else if (id == R.id.nav_leave) {
            System.out.println("The group ID of the group being left to "+ groupID);
            this.stopService(service);
            groupsRef.child(groupID).child("joined").child(uid).removeValue();
            groupsRef.child(groupID).child("locations").child(uid).removeValue();
            usersRef.child(uid).child("currentGroup").removeValue();
            groupsRef.child(groupID).addListenerForSingleValueEvent((new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    System.out.println("The data snapshot is "+ dataSnapshot.toString());
                    if(!dataSnapshot.hasChild("joined")){
                        System.out.println("Inside the total removal of invited and locations");
                        groupsRef.child(groupID).removeValue();
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

    private void startVoipFragment(Bundle activitySavedInstanceState) throws IllegalStateException{
        FragmentManager fm = getSupportFragmentManager();
        final VoipFragment voipFrag;
        String fragTag = groupID + "_VOIP_fragment";

        if(activitySavedInstanceState != null) {
            //if we're restarting the activity, retrieve the old voip fragment instead of making a new one
            voipFrag = (VoipFragment) fm.findFragmentByTag(fragTag);
            if(voipFrag == null)
                throw new IllegalStateException("Could not find VOIP fragment '" +
                        fragTag + "', Activity recreation failed.");
        }
        else {
            //create a voip fragment
            voipFrag = VoipFragment.newInstance(uid, Integer.parseInt(groupID));
            fm.beginTransaction().add(R.id.voip_frame, voipFrag, fragTag).commit();
        }

        //Connect PTT button
        findViewById(R.id.ptt_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    voipFrag.startTalking();
                    //groupsRef.child("muted").child(uid).setValue(false);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    voipFrag.stopTalking();
                    //groupsRef.child("muted").child(uid).setValue(true);
                }
                return true;
            }
        });
    }

    @Override
    public void onFragmentInteraction(@NotNull Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                String popUp = data.getStringExtra("popUp");

                if (popUp.equals("yes")){
                    this.stopService(service);
                    finish();
                }
            }
            if (resultCode == RESULT_CANCELED){

            }
        }
    }
}

class GroupExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {

    private final InsideGroupActivity myContext;

    public GroupExceptionHandler(InsideGroupActivity context) {
        myContext = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Error occurred inside the group.");
        e.printStackTrace(System.out);

        // attempt to relaunch the activity
        try {
            Intent intent = myContext.getIntent();

            boolean restartedAlready;
            try { restartedAlready = intent.getExtras().getBoolean("restartedAfterException"); }
            catch(NullPointerException ex) {restartedAlready = false;}
            if(restartedAlready) {
                System.out.println("Refusing to restart InsideGroupActivity twice.");
            }

            intent.putExtra("restartedAfterException", true);


            System.out.println("Restarting InsideGroupActivity.");
            myContext.finish();
            myContext.startActivity(intent);
        }

        catch (Exception ex) {
            //cannot recover, END IT ALL
            System.out.println("Unable to recover Group, aborting.");
            myContext.finish();
            return;
        }
    }
}
