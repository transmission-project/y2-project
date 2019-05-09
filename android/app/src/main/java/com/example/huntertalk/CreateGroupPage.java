package com.example.huntertalk;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class CreateGroupPage extends AppCompatActivity {

    private Button btnCreate;
    private DatabaseReference usersRef, groupRef;
    private TextView friend, tv, tv1;
    private String friendName, friendId;
    private TableLayout tableRecHunted,tableFriends;
    private int k = 0;
    private int f = 0;
    private String[][] selected;
    private HashMap<String,String> friends = new HashMap<String, String>();
    private HashMap<String,String> recentlyHunted = new HashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_page);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create New Group");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groupRef = database.getReference().child("groups");
        usersRef = database.getReference().child("users");
        GroupId groupIdObject = new GroupId();
        final String groupId = Integer.toString(groupIdObject.getId());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        btnCreate = (Button) findViewById(R.id.createButton);
        btnCreate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //adds current user as joined to the group and
                usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String nickname = dataSnapshot.getValue().toString();
                        groupRef.child(groupId).child("joined").child(uid).setValue(nickname);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                /**
                 * Adding people to invite list
                 * Nicknames are stored at 0 ids are at 1
                 */

                for(int i = 0; i < k+f; i++) {
                    if(selected[i][0] != null){
                        groupRef.child(groupId).child("invited").child(selected[i][1]).setValue(selected[i][0]);
                    }
                }
                Intent intent =new Intent(CreateGroupPage.this,InsideGroupActivity.class);
                intent.putExtra("groupID", groupId);
                startActivity(intent);
            }
        });

        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tableRecHunted = (TableLayout) findViewById(R.id.tableGroupMembers1);
                tableRecHunted.removeAllViews();
                k = 0;

                tableFriends = (TableLayout) findViewById(R.id.tableFriends);
                tableFriends.removeAllViews();
                f=0;
            /**
            * Method to output all the recently hunted and friends
            */
                for (DataSnapshot info : dataSnapshot.getChildren()) {
                    if (info.getKey().equals("recentlyHunted") ) {
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            recentlyHunted.put(friendId, friendName);
                            usersRef.child(friendId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nickname = dataSnapshot.getValue().toString();
                                    recentlyHunted.put(friendId, nickname);

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        createTable(recentlyHunted, "rc");

                    }
                    else if ( info.getKey().equals("friends")){
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            friends.put(friendId, friendName);
                        }
                        createTable(friends, "fr");
                    }
                }
                //nicknames are stored at 0 ids are at 1
                selected = new String[f + k][2];
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
    });
}
    /**
     *  Create a table based on Hash Map
     */
    private void createTable(HashMap<String, String> people, String command){
        for (String key: people.keySet()){
            String nickname= people.get(key);
            final String keyForStoringId =key;
            final TableRow row = new TableRow(getBaseContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 5, 10);
            row.setLayoutParams(lp);
            tv1 = new TextView(getBaseContext());
            tv1.setText(nickname);
            tv1.setId(f+k + 1000);
            row.setId(f+k);
            row.addView(tv1, lp);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int clicked_id = v.getId();
                    friend = (TextView) findViewById(clicked_id + 1000);
                    String nameRH = friend.getText().toString();
                    if (selected[clicked_id][0]== null) {
                        friend.setTextColor(Color.GREEN);
                        selected[clicked_id][0] = nameRH;
                        selected[clicked_id][1] = keyForStoringId;
                    }
                    else {
                        friend.setTextColor(Color.BLACK);
                        selected[clicked_id][0] = null;
                    }
                }
            }
         );
        if (command.equals("rc")) {
            tableRecHunted.addView(row, k);
            k++;
        }
        else{
            tableFriends.addView(row, f);
            f++;
        }
        }
    }
}
