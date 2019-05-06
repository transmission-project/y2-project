package com.example.huntertalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FriendList extends AppCompatActivity {
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        final EditText etSearch = findViewById(R.id.etsearch);
        Button searchButton = findViewById(R.id.searchButton);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();
        mDatabase.child(uid).child("friends").push();
        ActionBar actionBar = getSupportActionBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etSearch.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                etSearch.setHint("Enter email");
                return false;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String email = etSearch.getText().toString().trim();


                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Invalid email addresss");
                        return;
                    }
                    if (email.equals("") || !email.contains("@") || !email.contains(".")) {
                        etSearch.setError("Invalid email addresss");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Enter Email");
                        return;
                    }
                System.out.println("  Email checks passed");
            mDatabase.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    final String futureFriend = dataSnapshot.getKey();


                    // retrieve nickname fix

                   String nickname2= mDatabase.child(futureFriend).child("nickname").getKey();
                   System.out.println(nickname2);






                    System.out.println(" Future friend key is  "+ futureFriend);
                    mDatabase.child(uid).child("friends").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            System.out.println("  Before for loop");
                            for (DataSnapshot person : dataSnapshot.getChildren()) {
                               String friendName = person.getValue().toString();
                                 if (futureFriend == friendName){
                                     etSearch.setError("You're already friends!");
                                     return;
                                 }

                            }
                            System.out.println(" After for loop ");
//dev0eEDe1GSgSKTXqeYHdF5dAVV2

                            mDatabase.child(uid).child("friends").child(futureFriend).setValue( futureFriend,"ID");
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });






                }

        });
    }

        @Override
        public boolean onOptionsItemSelected (MenuItem menuItem){
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    FriendList.this.finish();
                    return true;
            }
            return (super.onOptionsItemSelected(menuItem));
        }

        // create an action bar button
  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.friend_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

}

