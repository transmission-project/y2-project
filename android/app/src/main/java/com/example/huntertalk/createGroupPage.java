package com.example.huntertalk;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class createGroupPage extends Activity implements View.OnClickListener {

    private Button btnCreate;
    private EditText nickname;
    private DatabaseReference mDatabase, mRef;
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

        mRef = database.getReference().child("groups");


        mDatabase = database.getReference().child("users");

        mRef.child("groupId").child("members").child("curUserId").setValue("curUserId");



        btnCreate = (Button) findViewById(R.id.createButton);

        btnCreate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Write what happens when create button is clicked
                for(int i = 0; i < k; i++) {

                    if(selected[i]){
                        mRef.child("groupId").child("members").child(Integer.toString(i + 1000)).setValue(Integer.toString(i + 1000));

                    }
                }

            }
        });



        nickname = (EditText) findViewById(R.id.nicknameCGP);



        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tableRecHunted = (TableLayout) findViewById(R.id.tableRecHunted);
                tableRecHunted.removeAllViews();
                k = 0;


                    for (DataSnapshot accounts : dataSnapshot.getChildren()) {
                        for (DataSnapshot recentlyHunted : accounts.getChildren()) {
                            for (DataSnapshot person : recentlyHunted.getChildren()) {
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

                                row.setOnClickListener(createGroupPage.this);
                                tableRecHunted.addView(row, k);
                                k++;

                            }
                        }

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
}
