package com.example.huntertalk.userRelated;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huntertalk.R;
import com.example.huntertalk.ui.firstLaunch.Home_page;
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
    private int counterForRH,counterForFR,counterForRowElements, friendRowCounter,rHRowCounter;
    private final FirebaseAuth auth =FirebaseAuth.getInstance();
    final String uid = auth.getCurrentUser().getUid();
    private HashMap<String,String> nickNames = new HashMap<String, String>();
    private HashMap<String,String> recentlyHunted = new HashMap<String, String>();
    private String previousPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        try {
            previousPage = getIntent().getExtras().getString("previousPage");
        }
        catch (NullPointerException e){

        }

        final EditText etSearch = findViewById(R.id.etsearch);
        Button searchButton = findViewById(R.id.searchButton);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        /**
         * Enabling the back button
         */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /**
         *  Create a friend list on launch
         */
       mDatabase.child(uid).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               tableFriends = (TableLayout) findViewById(R.id.tableFriendList);
               tableFriends.removeAllViews();
                counterForFR=0;
                friendRowCounter=0;
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

        /**
         *  Create a Recently Hunted list on launch
         */
        mDatabase.child(uid).child("recentlyHunted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableRecHunted = (TableLayout) findViewById(R.id.tableGroupMembers2);
                tableRecHunted.removeAllViews();
                counterForRH=0;
                rHRowCounter=0;
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

                hideKeyboard(FriendList.this);
                /**
                 * Check if email provided for potential friend exists.
                 */
                mDatabase.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChildren()){
                        etSearch.setError("User does not exist");
                        }
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
                    mDatabase.child(uid).child("friends").orderByKey().equalTo(futureFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(dataSnapshot.child(futureFriend).exists()){
                                etSearch.setError("You're already friends!");
                                return;
                            }
                            /**
                             * Add friends to the list with the nickname.
                             * Also add current user to the list "friend of" of the future friend
                             */
                            mDatabase.child(futureFriend).child("nickname").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String nickname3 = dataSnapshot.getValue().toString();
                                    mDatabase.child(uid).child("friends").child(futureFriend).setValue(nickname3);
                                    friendName=nickname3;
                                    nickNames.put(futureFriend,nickname3);
                                    addRow(nickname3,futureFriend, "fr");
                                    etSearch.setText("");
                                    etSearch.setHint("Enter email");
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
     * Get all friends and Recently hunted from the database and show on the appropriate lists
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
        counterForRowElements=1;
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        final TableRow row = new TableRow(getBaseContext());
        lp.setMargins(3, 10, 3, 10);
        row.setLayoutParams(lp);
        lp = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(3, 10, 3, 10);
        LinearLayout layout = new LinearLayout(getBaseContext());
        lp.weight = 1;
        layout.setLayoutParams(lp);
        layout.setWeightSum(1);
        LinearLayout.LayoutParams chiledParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        chiledParams.weight = (float) 1;

        tv1 = new TextView(getBaseContext());
        tv1.setText(nickname);
        tv1.setTextSize(18);
        tv1.setTextColor(Color.BLACK);
        tv1.setId(counterForFR + counterForRH + counterForRowElements);
        tv1.setLayoutParams(chiledParams);

        layout.addView(tv1);

        counterForRowElements++;

        tv = new TextView(getBaseContext());
        tv.setText(id);
        tv.setId(counterForFR + counterForRH + counterForRowElements);
        counterForRowElements++;
        tv.setVisibility(View.GONE);
        layout.addView(tv);


        /**
         * Creates appropriate buttons with correct functionality for each table
         * and adds them to the row. Then adds the row to the appropriate TableLayout
         */
        if (command.equals("rc")) {

            LinearLayout.LayoutParams chiledParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chiledParams1.gravity = Gravity.RIGHT;


        ImageView addBtn = new ImageView(this);
        addBtn.setId(counterForRowElements+counterForRH+counterForFR);
        counterForRowElements++;
        addBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addBtn.setLayoutParams(chiledParams1);
        if (command.equals("rc")) {
            addBtn.setBackgroundResource(R.drawable.ic_person_add_green_24dp);
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout lay = (LinearLayout) row.getChildAt(0);
                    TextView text=(TextView) lay.getChildAt(1);
                    String id= text.getText().toString();
                    TextView textNickname=(TextView) lay.getChildAt(0);
                    String nickname= textNickname.getText().toString();
                    mDatabase.child(uid).child("friends").child(id).setValue(nickname);
                    mDatabase.child(id).child("friendOf").child(uid).setValue("fr");
                    if(!nickNames.containsKey(id)){
                        nickNames.put(id,nickname);
                        addRow(nickname,id,"fr");
                    }else{
                        Toast.makeText(getApplicationContext(), "Already friends", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            layout.addView(addBtn);
            row.addView(layout);
            tableRecHunted.addView(row, rHRowCounter);
            counterForRH+=5;
            rHRowCounter++;
        }}

        if (command.equals("fr")){

            LinearLayout.LayoutParams chiledParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chiledParams1.gravity = Gravity.RIGHT;

            ImageView declinebtn = new ImageView(this);
            declinebtn.setBackgroundResource(R.drawable.ic_close_black_24dp);
            declinebtn.setId(counterForFR + counterForRH + counterForRowElements);
            declinebtn.setScaleType(ImageView.ScaleType.FIT_XY);
            declinebtn.setLayoutParams(chiledParams1);
            counterForRowElements++;

            declinebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout lay = (LinearLayout) row.getChildAt(0);
                    TextView text=(TextView) lay.getChildAt(1);
                    String id= text.getText().toString();
                    System.out.println(id);
                    mDatabase.child(uid).child("friends").child(id).removeValue();
                    mDatabase.child(id).child("friendOf").child(uid).removeValue();
                    nickNames.remove(id);
                    tableFriends.removeView(row);
                }
            });
            layout.addView(declinebtn);
            row.addView(layout);
            tableFriends.addView(row, friendRowCounter);
            counterForFR+=5;
            friendRowCounter++;
        }
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard(FriendList.this);
        return super.onTouchEvent(event);
    }

    //check which page we came from and go to that activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if ((previousPage != null) && previousPage.equals("insideGroupPage")){
            this.finish();
        }
        else if ((previousPage != null) && previousPage.equals("homePage")){
            Intent intent = new Intent(FriendList.this, Home_page.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    }
}
