package com.example.huntertalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class join_create extends AppCompatActivity {
    private DatabaseReference usersRef, groupRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        Button joinButton= findViewById(R.id.btjoin);
        final EditText groupIDInput = findViewById(R.id.etgroupid);

        joinButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                final int content = Integer.parseInt(groupIDInput.getText().toString());

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                groupRef = database.getReference().child("groups");
                usersRef = database.getReference().child("users");
                final String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                groupRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long dscount=dataSnapshot.getChildrenCount();


                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            Log.d("mytag",Long.toString(dscount));
                            int value = Integer.parseInt(ds.getKey());
                            if(content==value){
                                groupRef.child(ds.getKey()).child("joined").child(uid).setValue(uid);
                                Intent i = new Intent(join_create.this, InsideGroupActivity.class);
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
}
