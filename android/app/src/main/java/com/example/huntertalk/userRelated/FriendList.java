package com.example.huntertalk.userRelated;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.example.huntertalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FriendList extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private TableLayout tableRecHunted,tableFriends;
    public String friendName,friendId;
    private TextView tv, tv1;
    private int k,f,i;
    private final FirebaseAuth auth =FirebaseAuth.getInstance();
    final String uid = auth.getCurrentUser().getUid();
    private HashMap<String,String> nickNames = new HashMap<String, String>();
    private HashMap<String,String> recentlyHunted = new HashMap<String, String>();
    private TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        final EditText etSearch = findViewById(R.id.etsearch);
        Button searchButton = findViewById(R.id.searchButton);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        lp.setMargins(10, 10, 5, 10);
        /**
         * Enabling the back button
         */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        /**
         *  Create a friend list when launch
         */
       mDatabase.child(uid).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               tableFriends = (TableLayout) findViewById(R.id.tableFriendList);
               tableFriends.removeAllViews();
                f=0;
                /**
                 * Method to output all the recently hunted and friends
                 */
                startLists(dataSnapshot, "friends");
                createTable(nickNames, "fr");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        mDatabase.child(uid).child("recentlyHunted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableRecHunted = (TableLayout) findViewById(R.id.tableGroupMembers2);
                tableRecHunted.removeAllViews();
                /**
                 * Method to output all the recently hunted and friends
                 */
                startLists(dataSnapshot, "recentlyHunted");
                createTable(recentlyHunted, "rc");

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        /**
         * Changes hint on touch
         */
        etSearch.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        /**
         * Does all the checks and adds the user to the check list
         */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 final    String email = etSearch.getText().toString().trim();
                /**
                 * Check if email provided is of a valid format.
                 */
                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Invalid email address");
                        return;
                    }
                    if (email.equals("") || !email.contains("@") || !email.contains(".")) {
                        etSearch.setError("Invalid email address");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        etSearch.setError("Enter Email");
                        return;
                    }

                /**
                 * Check if email provided for potential friend exists.
                 */
                mDatabase.orderByChild("email").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       for (DataSnapshot email1 : dataSnapshot.getChildren()){
                           if (email1.child("email").exists()){
                               String emailToCheck= email1.child("email").getValue().toString();
                           if (email.equals(emailToCheck)){
                               return;
                           }
                           }
                       }
                        etSearch.setError("User does not exist");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                /**
                 *  Check if the user and potential friends are already friends
                 */
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

                            /**
                             * Add friends to the list with the nickname.
                             */
                                mDatabase.child(futureFriend).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String nickname3 = dataSnapshot.getValue().toString();
                                        mDatabase.child(uid).child("friends").child(futureFriend).setValue(nickname3);
                                        friendName=nickname3;
                                        nickNames.put(futureFriend,nickname3);
                                        addRow(nickname3,futureFriend, "fr");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
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


    /**
     * Get all friends and Recently hunteed from the database and show on the appropriate lists
     */
    private void startLists(DataSnapshot dataSnapshot, String command) {
        for (DataSnapshot friends1 : dataSnapshot.getChildren()){
            friendName = friends1.getValue().toString();
            friendId = friends1.getKey();
            if (command.equals("friends")){
                    nickNames.put(friendId,friendName);
            }
            if (command.equals("recentlyHunted")){
                    recentlyHunted.put(friendId,friendName);
            }
        }
    }

    /**
     *  Create a table based on Hash Map
     */
    private void createTable(HashMap<String, String> people, String command){
        for (String key: people.keySet()){
            String nickname= people.get(key);
            if (command.equals("rc")) {
                addRow(nickname,key, "rc");
            }
            else{
                addRow(nickname,key, "fr");
            }
        }
    }

    /**
     * Method to add a row to a table view. Command "rc" for Recently Hunted,
     * command "fr" for Friends
     * @param nickname
     * @param id
     * @param command
     */
    private void addRow (String nickname, String id, String command){

        // Creates a row with two TextView fields

        final TableRow row = createRow(nickname, id);
        /**
         * Creates appropriate buttons with correct functionality for each table
         * and adds them to the row. Then adds the row to the appropriate TableLayout
         */
        if (command.equals("rc")) {
           final Button btn = new Button(this);
            btn.setText("Add Friend");
            btn.setId(i+k+f+1000);
            i++;
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView text=(TextView) row.getChildAt(1);
                    String id= text.getText().toString();
                    TextView textNickname=(TextView) row.getChildAt(0);
                    String nickname= textNickname.getText().toString();
                    mDatabase.child(uid).child("friends").child(id).setValue(nickname);
                    if(!nickNames.containsKey(id)){
                        nickNames.put(id,nickname);
                        addRow(nickname,id,"fr");
                    }else{
                        Toast.makeText(getApplicationContext(), "Already friends", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            row.addView(btn);
            tableRecHunted.addView(row, k);
            k++;
        }
        if (command.equals("fr")){
            Button btn = new Button(this);
            btn.setText("X");
            btn.setId(i+k+f+1000);
            i++;
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView text=(TextView) row.getChildAt(1);
                    String id= text.getText().toString();
                    mDatabase.child(uid).child("friends").child(id).removeValue();
                    nickNames.remove(id);
                    tableFriends.removeView(row);
                }
            });
            row.addView(btn);
            tableFriends.addView(row, f);
            f++;
        }
    }

    /**
     * Creates a row with two TextView fields
     */

    private TableRow createRow(String nickname, String id) {
        TableRow row=new TableRow(getBaseContext());
        row.setLayoutParams(lp);
        tv1 = new TextView(getBaseContext());
        tv1.setText(nickname);
        tv1.setId(f+k +i + 1000);
        i++;
        tv = new TextView(getBaseContext());
        tv.setText(id);
        tv.setId(f+k +i+ 1000);
        i++;
        tv.setVisibility(View.GONE);
        row.setId(f+k);
        row.addView(tv1, lp);
        row.addView(tv, lp);
     return row;
    }

    /**
     *  Back button functionality
     * @param menuItem
     * @return
     */
    @Override
        public boolean onOptionsItemSelected (MenuItem menuItem){
            switch (menuItem.getItemId()) {
                case android.R.id.home:
                    FriendList.this.finish();
                    return true;
            }
            return (super.onOptionsItemSelected(menuItem));
        }
}

