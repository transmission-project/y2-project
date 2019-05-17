package com.example.huntertalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

import com.example.huntertalk.everythingWithGroups.InsideGroupActivity;
import com.example.huntertalk.ui.firstLaunch.Home_page;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LeaveGroupPopUp extends Activity {

    private String groupID;
    private String uid;
    private String finish;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.leave_group_pop_up);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        //set window size relative to screen size
        getWindow().setLayout((int)(width * .8), (int)(height * .25));

        //If yes button is pressed user is removed from group in database
        ImageButton yes = (ImageButton) findViewById(R.id.buttonCheck);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference groupsRef = database.getReference().child("groups");
                final DatabaseReference usersRef = database.getReference().child("users");


                //get group ID and user ID from Inside Group Page
                try {
                    groupID = getIntent().getExtras().getString("groupID");
                    uid = getIntent().getExtras().getString("uid");

                }
                catch (NullPointerException e) {
                    groupID = "ERROR";
                    uid = "ERROR";
                }

                usersRef.child(uid).child("currentGroup").removeValue();

                groupsRef.child(groupID).child("joined").child(uid).removeValue();
                groupsRef.child(groupID).child("locations").child(uid).removeValue();
                groupsRef.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.hasChild("joined")) {
                            groupsRef.child(groupID).removeValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //send yes to inside group and close
                Intent popUpIntent = new Intent();
                popUpIntent.putExtra("popUp", "yes");

                setResult(RESULT_OK, popUpIntent);
                finish();
            }
        });

        //If no button is pressed send no and finish pop up
        ImageButton no = (ImageButton) findViewById(R.id.buttonCross);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popUpIntent = new Intent();
                popUpIntent.putExtra("popUp", "no");

                setResult(RESULT_OK, popUpIntent);
                finish();
            }
        });
    }
}
