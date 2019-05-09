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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class GroupOverviewFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference usersRef, groupRef;
    private FirebaseAuth mAuth;
    private TextView tv;
    private TableLayout tb;
    private HashMap <String, String> membersInTheGroup=new HashMap<String, String>();
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
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser= mAuth.getCurrentUser();
       final String uid= currentUser.getUid();

        groupRef.child(groupID).child("joined").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupMember : dataSnapshot.getChildren()) {
                    final String member = groupMember.getKey();
                    final String name= groupMember.getValue().toString();
                    System.out.println(member+ "   "+ name);
                    membersInTheGroup.put(member,name);

                    //get nickname from user ID and add to recently hunted of existing group members
                    usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String nickname = dataSnapshot.getValue().toString();
                            if (!member.equals(uid)) {
                                usersRef.child(member).child("recentlyHunted").child(uid).setValue(nickname);
                            }
                            membersInTheGroup.put(uid,nickname);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                createTable();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        groupRef.child(groupID).child("joined").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            String removedKey= dataSnapshot.getKey();
            membersInTheGroup.remove(removedKey);
            createTable();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return myView;
    }

    private void createTable(){
        tb = (TableLayout) myView.findViewById(R.id.tableGroupMembers);
        tb.removeAllViews();
        for (String key: membersInTheGroup.keySet()){
            String nickname= membersInTheGroup.get(key);
            addRow(nickname);
        }
    }

    private void addRow(String nickname){
        TableRow row = new TableRow(myView.getContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 5, 10);
        row.setLayoutParams(lp);
        tv = new TextView(myView.getContext());
        tv.setText(nickname);
        row.addView(tv, lp);
        tb.addView(row);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
