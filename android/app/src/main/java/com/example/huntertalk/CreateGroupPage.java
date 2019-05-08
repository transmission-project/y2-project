package com.example.huntertalk;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class CreateGroupPage extends AppCompatActivity implements View.OnClickListener {

    private Button btnCreate;
    private DatabaseReference usersRef, groupRef;
    private TextView friend, tv, tv1;
    private String friendName, friendId;
    private TableLayout tableRecHunted,tableFriends;
    private int k = 0;
    private int f = 0;
    private String[] selected;
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
        String uid = auth.getCurrentUser().getUid();

        groupRef.child(groupId).child("joined").child(uid).setValue(uid);

        btnCreate = (Button) findViewById(R.id.createButton);
        btnCreate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                for(int i = 0; i < k; i++) {
                    if(selected[i] != null){
                        groupRef.child(groupId).child("invited").child(selected[i]).setValue(selected[i]);
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

                tableRecHunted = (TableLayout) findViewById(R.id.tableGroupMembers);
                tableRecHunted.removeAllViews();
                k = 0;

                tableFriends = (TableLayout) findViewById(R.id.tableFriends);
                tableFriends.removeAllViews();
                f=0;

                for (DataSnapshot info : dataSnapshot.getChildren()) {
                    if (info.getKey().equals("recentlyHunted") ) {
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            Log.d("hashTag", friendName + " and " + friendId);
                            recentlyHunted.put(friendId, friendName);
                            usersRef.child(friendId).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nickname = dataSnapshot.getValue().toString();
                                    friends.put(friendId, nickname);

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                        createTableFriends(recentlyHunted, "rc");

                    }
                    else if ( info.getKey().equals("friends")){
                        for (DataSnapshot person : info.getChildren()) {
                            friendName = person.getValue().toString();
                            friendId = person.getKey();
                            friends.put(friendId, friendName);
                        }
                        createTableFriends(friends, "fr");
                    }
                }

                selected = new String[f + k];
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
    });
}
    private void createTableFriends(HashMap<String, String> people, String command){
        for (String key: people.keySet()){
            String nickname= people.get(key);
            final TableRow row = new TableRow(getBaseContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 5, 10);
            row.setLayoutParams(lp);
            tv1 = new TextView(getBaseContext());
            tv1.setText(nickname);
            tv1.setId(f +k + 1000);
            row.setId(f+k);
            row.addView(tv1, lp);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int clicked_id = v.getId();
                    friend = (TextView) findViewById(clicked_id + 1000);
                    String nameRH = friend.getText().toString();

                    if (selected[clicked_id ]== null) {
                        friend.setTextColor(Color.GREEN);
                        selected[clicked_id] = nameRH;
                    }
                    else {
                        friend.setTextColor(Color.BLACK);
                        selected[clicked_id] = null;
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

    @Override
    public void onClick(View v) {
        int clicked_id = v.getId();
        friend = (TextView) findViewById(clicked_id + 1000);
        String nameRH = friend.getText().toString();

        if (selected[clicked_id]== null) {
            friend.setTextColor(Color.GREEN);
            selected[clicked_id] = nameRH;
        }
        else {
            friend.setTextColor(Color.BLACK);
            selected[clicked_id] = null;
        }
    }
}
