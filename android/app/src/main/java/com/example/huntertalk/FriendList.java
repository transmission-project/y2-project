package com.example.huntertalk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class FriendList extends AppCompatActivity {
    private DatabaseReference mDatabase, usersRef;
    private TableLayout tableFriendList;
    private HashMap<String,String> nickNames = new HashMap<String, String>();
    public String friendName,friendId;
    private TextView tv;
    private int k;
    private int f;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        final EditText etSearch = findViewById(R.id.etsearch);
        Button searchButton = findViewById(R.id.searchButton);


        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        usersRef =  FirebaseDatabase.getInstance().getReference().child("users");
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();
        FirebaseUser currentUser= auth.getCurrentUser();
        mDatabase.child(uid).child("friends").push();
        ActionBar actionBar = getSupportActionBar();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableFriendList= (TableLayout) findViewById(R.id.tableFriendList);
                tableFriendList.removeAllViews();
                k=0;
                f=0;
                startFriendList(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



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
                 final    String email = etSearch.getText().toString().trim();


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
                mDatabase.orderByChild("email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot email1 : dataSnapshot.getChildren()){
                           String emailToCheck= email1.child("email").getValue().toString();
                          if (email.equals(emailToCheck)){
                               return;
                           }
                       }
                        etSearch.setError("User does not exist");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            mDatabase.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    final String futureFriend = dataSnapshot.getKey();
                    mDatabase.child(uid).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Boolean alreadyFriends=false;
                            for (DataSnapshot person : dataSnapshot.getChildren()) {
                                String friendName = person.getKey().toString();
                                if (futureFriend.equals(friendName)){
                                    alreadyFriends=true;
                                    break;
                                }
                            }
                            if(alreadyFriends){
                                etSearch.setError("You're already friends!");
                                return;
                            }

                                usersRef.child(futureFriend).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nickname3 = dataSnapshot.getValue().toString();
                                        mDatabase.child(uid).child("friends").child(futureFriend).setValue(nickname3);
                                        friendName=nickname3;
                                        nickNames.put(futureFriend,nickname3);
                                        createARow();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });


//dev0eEDe1GSgSKTXqeYHdF5dAVV2


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
//     abc@se.se
                }
            });

                }

        });
    }

    private void startFriendList(DataSnapshot dataSnapshot) {
        for (DataSnapshot friends : dataSnapshot.getChildren()){
            if (friends.getKey().equals("friends")){
                for (DataSnapshot person : friends.getChildren()){
                    UserInformation uInfo = new UserInformation();
                    friendName = person.getValue().toString();
                    friendId = person.getKey().toString();
                    nickNames.put(friendId,friendName);
                    createARow();
                }
            }
        }
    }

    private void createARow() {
        TableRow row = new TableRow(getBaseContext());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 10, 5, 10);
        row.setLayoutParams(lp);
        tv = new TextView(getBaseContext());
        tv.setText(friendName);
        tv.setId(f + k + 1000);
        row.setId(f + k);
        row.addView(tv, lp);
        tableFriendList.addView(row, k);
        k++;
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

