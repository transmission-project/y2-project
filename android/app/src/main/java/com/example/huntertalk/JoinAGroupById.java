package com.example.huntertalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class JoinAGroupById extends AppCompatActivity {
    private DatabaseReference usersRef, groupRef, mDatabase, sDatabase;
    private TableLayout tableInvitations;
    private TextView tv1;
    private int rowNumber;
    private int i = 150;
    private int k;
    Boolean changed=false;
    private final FirebaseAuth auth =FirebaseAuth.getInstance();
    final String uid = auth.getCurrentUser().getUid();
    String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        Button joinButton= findViewById(R.id.btjoin);
        mDatabase = FirebaseDatabase.getInstance().getReference("groups");

     final  EditText groupIDInput = findViewById(R.id.etgroupid);

        //create invitationlist on start
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableInvitations= (TableLayout) findViewById(R.id.tableInvitations);
                tableInvitations.removeAllViews();
                for (DataSnapshot groups : dataSnapshot.getChildren()) {
                    for (DataSnapshot subgroups : groups.getChildren()) {
                        String subgroup=subgroups.getKey();
                        if(subgroup.equals("invited")){
                            for (DataSnapshot user : subgroups.getChildren()) {
                                String users=user.getKey();
                                if(users.equals(uid)){
                                    groupID=groups.getKey();
                                    String nickname=user.getValue().toString();
                                    createTable(groupID,nickname);
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
                if(s.length() != 0)
                    changed=true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Join Group");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //The user joins the group number he entered
        joinButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final int content = Integer.parseInt(groupIDInput.getText().toString());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                groupRef = database.getReference().child("groups");
                usersRef = database.getReference().child("users");
                final String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();


                groupRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long dscount=dataSnapshot.getChildrenCount();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            int value = Integer.parseInt(ds.getKey());
                            if(content==value){
                                final DataSnapshot ds1=ds;
                    /**
                    * Gets all joined people from the group
                    */
                                usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nickname = dataSnapshot.getValue().toString();
                                        groupRef.child(ds1.getKey()).child("joined").child(uid).setValue(nickname);
                                        groupRef.child(ds1.getKey()).child("invited").child(uid).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                                for (DataSnapshot ch: ds.child("joined").getChildren()) {
                                    String id= ch.getKey();
                                    String rcNickname= ch.getValue().toString();
                                    if (!id.equals(uid)){
                                    usersRef.child(uid).child("recentlyHunted").child(id).setValue(rcNickname);
                                    }
                                }

                                Intent i = new Intent(JoinAGroupById.this, InsideGroupActivity.class);
                                i.putExtra("groupID", groupIDInput.getText().toString());
                                startActivity(i);
                                break;
                            }
                            else if(dscount==1) {
                                Toast.makeText(getApplicationContext(),"Group not found",Toast.LENGTH_SHORT).show();
                            }
                        dscount--;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    boolean secondPress =false;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:

                if(!changed){
                    Intent intent = new Intent(JoinAGroupById.this, Home_page.class);
                    startActivity(intent);
                }else {
                    if (secondPress) {
                        Intent intent = new Intent(JoinAGroupById.this, Home_page.class);
                        startActivity(intent);
                        this.finish();
                    } else {
                        Toast message = Toast.makeText(JoinAGroupById.this, "Press once again to cancel joining a group",
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
    private void createTable(final String ID, final String nickname){
            rowNumber=0;
            final TableRow row = new TableRow(getBaseContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 10, 5, 10);
            row.setLayoutParams(lp);
            Button btn1 = new Button(this);
            btn1.setText("Accept");
            btn1.setId(i+k+100);
            btn1.setVisibility(View.VISIBLE);
            Button btn2 = new Button(this);
            btn2.setText("Decline");
            btn2.setId(i+k);
            btn2.setVisibility(View.VISIBLE);
            Button btn3 = new Button(this);
            btn3.setText("Invisible");
            btn3.setId(i+k+1000);
            btn3.setVisibility(View.GONE);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView text=(TextView) row.getChildAt(1);
                    String id= text.getText().toString();
                    mDatabase.child(id).child("invited").child(uid).removeValue();
                    mDatabase.child(id).child("joined").child(uid).setValue(nickname);
                    Intent intent = new Intent(JoinAGroupById.this, InsideGroupActivity.class);
                    intent.putExtra("groupID", id);
                    startActivity(intent);
                    tableInvitations.removeView(row);
                }
            });

            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView text=(TextView) row.getChildAt(1);
                    String id= text.getText().toString();
                    mDatabase.child(id).child("invited").child(uid).removeValue();
                    tableInvitations.removeView(row);
                }
            });

            tv1 = new TextView(getBaseContext());
            tv1.setText(ID);
            tv1.setTextSize(20);
            tv1.setId(i + k + 10000);
            row.setId(k);
            row.addView(btn3);
            row.addView(tv1, lp);
            row.addView(btn1);
            row.addView(btn2);
            tableInvitations.addView(row, rowNumber);
            rowNumber++;
    }
}
