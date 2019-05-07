package com.example.huntertalk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InviteToGroupFragment extends Fragment {

    View myView;
    private DatabaseReference userDb;
    private DatabaseReference groupDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.invite_to_group_layout, container, false);

        super.onCreate(savedInstanceState);
        //getActivity().setContentView(R.layout.invite_to_group_layout);

        final EditText etSearch = myView.findViewById(R.id.etsearchmember);
        Button searchButton = myView.findViewById(R.id.btnsearch);

        userDb = FirebaseDatabase.getInstance().getReference("users");
        groupDb = FirebaseDatabase.getInstance().getReference("groups");

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String enteredemail = etSearch.getText().toString().trim();

                if (!enteredemail.contains("@") || !enteredemail.contains(".")) {
                    etSearch.setError("Invalid email addresss");
                    return;
                }
                else if (TextUtils.isEmpty(enteredemail)) {
                    etSearch.setError("Enter Email");
                    return;
                }
                else {

                    //usersRef.child(uid).addValueEventListener(new ValueEventListener() {

                    userDb.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot user: dataSnapshot.getChildren()){
                                String dbemail=user.child("email").getValue().toString();

                                String groupID;
                                try {
                                    groupID = getActivity().getIntent().getExtras().getString("groupID");
                                }
                                catch (NullPointerException e) {
                                    groupID = "ERROR";
                                }

                                if(dbemail.equals(enteredemail)){
                                    String inviteduser=user.getKey();
                                    groupDb.child(groupID).child("invited").child(inviteduser).setValue(inviteduser);
                                    //Log.d("grouptag", "groupID "+groupID);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            });



        return myView;
    }
}
