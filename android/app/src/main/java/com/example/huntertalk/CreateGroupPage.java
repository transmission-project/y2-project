package com.example.huntertalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;

public class CreateGroupPage extends AppCompatActivity implements View.OnClickListener {

    private Button btnCreate;
    private EditText nickname;
    private DatabaseReference usersRef, groupRef;
    private TextView friend, tv;
    private String friendName;
    private TableLayout tableRecHunted;
    private int k = 0;
    private boolean[] selected;

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
                Intent intent =new Intent(CreateGroupPage.this,InsideGroupActivity.class);
                intent.putExtra("groupID", groupId);
                startActivity(intent);

                for(int i = 0; i < k; i++) {

                    if(selected[i]){
                        groupRef.child(groupId).child("invited").child(Integer.toString(i + 1000)).setValue(Integer.toString(i + 1000));

                    }
                }

            }
        });



        nickname = (EditText) findViewById(R.id.nicknameCGP);



        usersRef.child(uid).child("recentlyHunted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tableRecHunted = (TableLayout) findViewById(R.id.tableRecHunted);
                tableRecHunted.removeAllViews();
                k = 0;

                for (DataSnapshot person : dataSnapshot.getChildren()) {
                            UserInformation uInfo = new UserInformation();
                            friendName = person.getValue().toString();

                            TableRow row = new TableRow(getBaseContext());
                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(10, 10, 5, 10);
                            row.setLayoutParams(lp);
                            tv = new TextView(getBaseContext());
                            tv.setText(friendName);
                            tv.setId(1000 + k);
                            row.setId(k);


                            row.addView(tv, lp);

                            row.setOnClickListener(CreateGroupPage.this);
                            tableRecHunted.addView(row, k);
                            k++;

                        }
                selected = new boolean[k];
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int clicked_id = v.getId();
        friend = (TextView) findViewById(clicked_id + 1000);

        if (selected[clicked_id]) {
            friend.setTextColor(Color.BLACK);
            selected[clicked_id] = false;
        }
        else {
            friend.setTextColor(Color.GREEN);
            selected[clicked_id] = true;
        }

    }

    boolean secondPress =false;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (secondPress){
                    Intent intent = new Intent(CreateGroupPage.this, Home_page.class);
                    startActivity(intent);}
                else{
                    Toast message= Toast.makeText(CreateGroupPage.this, "Press once again to cancel group creation",
                            Toast.LENGTH_LONG);
                    message.setGravity(Gravity.TOP, 0,0);
                    message.show();
                    secondPress=true;
                }
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}



