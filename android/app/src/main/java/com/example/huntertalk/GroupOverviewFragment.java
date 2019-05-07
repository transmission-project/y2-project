package com.example.huntertalk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupOverviewFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference usersRef, groupRef;
    private TextView tv;
    private TableLayout tb;
    View myView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.group_overview_layout, container, false);

        String groupID;
        try {
            groupID = getActivity().getIntent().getExtras().getString("groupID");
        }
        catch (NullPointerException e) {
            groupID = "ERROR";
        }

        String activityTitle = getResources().getString(R.string.title_with_group_name, groupID);
        getActivity().setTitle(activityTitle);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        groupRef = database.getReference().child("groups");
        usersRef = database.getReference().child("users");

        groupRef.child(groupID).child("joined").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String member;
                tb = (TableLayout) getActivity().findViewById(R.id.tableGroupMembers);
                tb.removeAllViews();

                for (DataSnapshot groupMember : dataSnapshot.getChildren()) {
                    member = groupMember.getValue().toString();

                    //get nicknames from user ID
                    usersRef.child(member).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String nickname = dataSnapshot.getValue().toString();

                            TableRow row = new TableRow(getActivity().getBaseContext());
                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(10, 10, 5, 10);
                            row.setLayoutParams(lp);
                            tv = new TextView(getActivity().getBaseContext());
                            tv.setText(nickname);
                            row.addView(tv, lp);
                            tb.addView(row);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return myView;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
