package com.example.huntertalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import com.example.huntertalk.ui.firstLaunch.Home_page;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LeaveGroupPopUp extends Activity {

    private String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.leave_group_pop_up);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        //getWindow().setLayout((int)(width * .8), (int)(height * .25));

        ImageButton yes = (ImageButton) findViewById(R.id.buttonCheck);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference groupsRef = database.getReference().child("groups");

                FirebaseAuth auth = FirebaseAuth.getInstance();
                String uid = auth.getCurrentUser().getUid();

                try {
                    groupID = getIntent().getExtras().getString("groupID");
                }
                catch (NullPointerException e) {
                    groupID = "ERROR";
                }

                groupsRef.child(groupID).child("joined").child(uid).removeValue();
                groupsRef.child(groupID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.hasChild("joined")) {
                            groupsRef.child(groupID).child("invited").removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                LeaveGroupPopUp.this.finish();
                Intent i =  new Intent(LeaveGroupPopUp.this, Home_page.class);
                startActivity(i);
            }
        });

        ImageButton no = (ImageButton) findViewById(R.id.buttonCross);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaveGroupPopUp.this.finish();
            }
        });

    }
}
