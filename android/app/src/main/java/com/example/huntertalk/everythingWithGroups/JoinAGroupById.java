package com.example.huntertalk.everythingWithGroups;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.example.huntertalk.LeaveGroupPopUp;
import com.example.huntertalk.ui.firstLaunch.Home_page;
import com.example.huntertalk.R;

import com.example.huntertalk.userRelated.FriendList;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.makeText;
import static com.example.huntertalk.userRelated.FriendList.hideKeyboard;

public class JoinAGroupById extends AppCompatActivity {

    private DatabaseReference usersRef, groupRef, mDatabase;
    private TableLayout tableInvitations;
    private TextView tv1;
    private int rowNumber;
    private int i = 150;
    private int k;
    Boolean changed = false;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    final String uid = auth.getCurrentUser().getUid();
    String groupID;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng lastKnownLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lastKnownLocation = new LatLng(-33.8523341, 151.2106085);
                        if (location != null) {
                            lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                });


        setContentView(R.layout.activity_join_create);
        Button joinButton = findViewById(R.id.btjoin);
        mDatabase = FirebaseDatabase.getInstance().getReference("groups");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        final EditText groupIDInput = findViewById(R.id.etgroupid);

        //create invitation list on start
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableInvitations = (TableLayout) findViewById(R.id.tableInvitations);
                tableInvitations.removeAllViews();
                for (DataSnapshot groups : dataSnapshot.getChildren()) {
                    for (DataSnapshot subgroups : groups.getChildren()) {
                        String subgroup = subgroups.getKey();
                        if (subgroup.equals("invited")) {
                            for (DataSnapshot user : subgroups.getChildren()) {
                                String users = user.getKey();
                                if (users.equals(uid)) {
                                    groupID = groups.getKey();
                                    String nickname = user.getValue().toString();
                                    createTable(groupID, nickname);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupIDInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    changed = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Join Group");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //The user joins the group number he entered
        joinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!groupIDInput.getText().toString().equals("")) {
                   try {
                       final int content = Integer.parseInt(groupIDInput.getText().toString());
                       FirebaseDatabase database = FirebaseDatabase.getInstance();
                       groupRef = database.getReference().child("groups");
                       usersRef = database.getReference().child("users");
                       final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                       groupRef.addListenerForSingleValueEvent(new ValueEventListener() {

                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               long dscount = dataSnapshot.getChildrenCount();
                               for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                                   int value = Integer.parseInt(ds.getKey());
                                   if (content == value) {
                                       /**
                                        * Gets current user id and nickname and adds to the list of joined
                                        * Gets all joined people from the group
                                        */
                                       usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                               String nickname = dataSnapshot.child("nickname").getValue().toString();
                                               groupRef.child(ds.getKey()).child("joined").child(uid).setValue(nickname);
                                               groupRef.child(ds.getKey()).child("invited").child(uid).removeValue();
                                               usersRef.child(uid).child("currentGroup").setValue(ds.getKey());
                                               groupRef.child(ds.getKey()).child("locations").child(uid).setValue(lastKnownLocation);
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError databaseError) {
                                           }
                                       });



                                       Intent i = new Intent(JoinAGroupById.this, InsideGroupActivity.class);
                                       i.putExtra("groupID", groupIDInput.getText().toString());
                                       JoinAGroupById.this.finish();
                                       startActivity(i);
                                       break;
                                   } else if (dscount == 1) {
                                       Toast toast = makeText(getApplicationContext(), "Group not found", Toast.LENGTH_SHORT);
                                       toast.setGravity(Gravity.BOTTOM, 0, 700);
                                       toast.show();
                                   }
                                   dscount--;
                               }
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });

                   }
                catch(NumberFormatException numEx){
                    groupIDInput.setError("Group ID is a number");
                   }
                } else {
                    groupIDInput.setError("Please enter group ID");
                }

            }
        });
    }

    boolean secondPress = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:

                if (!changed) {
                    Intent intent = new Intent(JoinAGroupById.this, Home_page.class);
                    this.finish();
                    startActivity(intent);
                } else {
                    if (secondPress) {
                        Intent intent = new Intent(JoinAGroupById.this, Home_page.class);
                        this.finish();
                        startActivity(intent);
                    } else {
                        Toast message = makeText(JoinAGroupById.this, "Press once again to cancel joining a group",
                                Toast.LENGTH_LONG);
                        message.setGravity(Gravity.TOP, 0, 0);
                        message.show();
                        secondPress = true;
                    }
                }
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    //Create invitations table
    private void createTable(final String ID, final String nickname) {

        rowNumber = 0;
        final TableRow row = new TableRow(getBaseContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 5, 10);
        row.setLayoutParams(lp);
        Button acceptButton = new Button(this);
        acceptButton.setText("Accept");
        acceptButton.setId(i + k + 100);
        acceptButton.setVisibility(View.VISIBLE);
        Button declineButton = new Button(this);
        declineButton.setText("Decline");
        declineButton.setId(i + k);
        declineButton.setVisibility(View.VISIBLE);
        Button invisibleButton = new Button(this);
        invisibleButton.setText("Invisible");
        invisibleButton.setId(i + k + 1000);
        invisibleButton.setVisibility(View.GONE);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) row.getChildAt(1);
                String id = text.getText().toString();
                mDatabase.child(groupID).child("invited").child(uid).removeValue();
                mDatabase.child(groupID).child("joined").child(uid).setValue(nickname);

                usersRef.child(uid).child("currentGroup").setValue(id);

                Intent intent = new Intent(JoinAGroupById.this, InsideGroupActivity.class);
                intent.putExtra("groupID", id);
                JoinAGroupById.this.finish();
                startActivity(intent);
                tableInvitations.removeView(row);
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) row.getChildAt(1);
                String id = text.getText().toString();
                mDatabase.child(id).child("invited").child(uid).removeValue();
                tableInvitations.removeView(row);
            }
        });

        tv1 = new TextView(getBaseContext());
        tv1.setText(ID);
        tv1.setTextSize(20);
        tv1.setId(i + k + 10000);
        row.setId(k);
        row.addView(invisibleButton);
        row.addView(tv1, lp);
        row.addView(acceptButton);
        row.addView(declineButton);
        tableInvitations.addView(row, rowNumber);
        rowNumber++;
    }
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard(JoinAGroupById.this);
        return super.onTouchEvent(event);
    }
}
