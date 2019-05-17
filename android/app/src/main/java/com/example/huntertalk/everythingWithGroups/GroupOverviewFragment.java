package com.example.huntertalk.everythingWithGroups;

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

import com.example.huntertalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class GroupOverviewFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference usersRef, groupRef;
    private FirebaseAuth mAuth;
    private TextView tv;
    private TableLayout tb;
    private HashMap <String, String> membersInTheGroup;
    View myView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.group_overview_layout, container, false);
        membersInTheGroup=new HashMap<String, String>();
        /**
         *   Sets the group id as the nameof the group for current user
         */
        String groupID;
        try {
            groupID = getActivity().getIntent().getExtras().getString("groupID");
        }
        catch (NullPointerException e) {
            groupID = "ERROR";
        }

        String activityTitle = getResources().getString(R.string.title_with_group_name, groupID);
        getActivity().setTitle(activityTitle);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        groupRef = database.getReference().child("groups");
        usersRef = database.getReference().child("users");
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        final String uid= currentUser.getUid();

            groupRef.child(groupID).child("joined").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println(dataSnapshot);
               String member= dataSnapshot.getKey();
               System.out.println(member);
               String name= dataSnapshot.child("nickname").getValue().toString();
               System.out.println("The uid and key are "+ member+"   "+ name);
               membersInTheGroup.put(member,name);
               createTable();
                if (!member.equals(uid)) {
                    usersRef.child(uid).child("recentlyHunted").child(member).setValue(name);
                    usersRef.child(member).child("recentlyHuntedOf").child(uid).setValue("rh");
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            /**
             *  Updates the table of group members whenever someone leaves
             */
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

    /**
     *  Creates the table and adds a row (method below) for each member of the group
     *  Group members are stored in the hashmap membersInTheGroup
     */
    private void createTable(){
        tb = (TableLayout) myView.findViewById(R.id.tableGroupMembers);
        tb.removeAllViews();
        for (String key: membersInTheGroup.keySet()){
            String nickname= membersInTheGroup.get(key);
            addRow(nickname);
        }
    }

    /**
     * Adding row to the table of users connected to the group.
     * @param nickname
     */
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
