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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinAGroupById extends AppCompatActivity {
    private DatabaseReference usersRef, groupRef;
    Boolean changed=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        Button joinButton= findViewById(R.id.btjoin);
     final  EditText groupIDInput = findViewById(R.id.etgroupid);



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
                                 * Gets acurrent user id and nickname and adds to the list of joined
                                 */
                                usersRef.child(uid).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nickname = dataSnapshot.getValue().toString();
                                        groupRef.child(ds1.getKey()).child("joined").child(uid).setValue(nickname);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });

                /**
                * Remove previous "recently Hunted and get all joined people from the group
                * as new recently hunted
                */
                                usersRef.child(uid).child("recentlyHunted").removeValue();
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
}
