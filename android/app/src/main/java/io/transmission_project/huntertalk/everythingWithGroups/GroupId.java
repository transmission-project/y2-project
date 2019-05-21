package io.transmission_project.huntertalk.everythingWithGroups;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Random;

public class GroupId {
    int n;
    int id;
    boolean exists;

    public GroupId (){

        Random rand = new Random();
        exists = true;
        while (exists) {
            n = rand.nextInt(9000) + 1000;
            exists = false;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference mRef = database.getReference().child("groups");

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot group : dataSnapshot.getChildren()) {

                        if (Integer.parseInt(group.getKey()) == n) {
                            exists = true;
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        this.id = n;
    }
    public int getId(){
        return this.id;
    }
}
