package com.example.huntertalk.everythingWithGroups;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.huntertalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.makeText;

public class InviteToGroupFragment extends Fragment {

    View myView;
    private DatabaseReference userDb;
    private DatabaseReference groupDb;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.invite_to_group_layout, container, false);
        final EditText etSearch = myView.findViewById(R.id.etsearchmember);

        super.onCreate(savedInstanceState);

        //hide keyboard when pressing outside
        myView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE){
                    hideKeyboardFrom(getContext(), v);
                }
                return true;
            }
        });

        Button searchButton = myView.findViewById(R.id.btnsearch);

        userDb = FirebaseDatabase.getInstance().getReference("users");
        groupDb = FirebaseDatabase.getInstance().getReference("groups");

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        etSearch.setHint("Enter email");
        etSearch.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String enteredemail = etSearch.getText().toString().trim();

                Log.d("btnTag", enteredemail);
                if (!enteredemail.contains("@") || !enteredemail.contains(".")) {
                    etSearch.setError("Invalid email addresss");
                    return;
                }
                else if (TextUtils.isEmpty(enteredemail)) {
                    etSearch.setError("Enter Email");
                    return;
                }
                else {
                    userDb.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    String nickname = user.child("nickname").getValue().toString();
                                    groupDb.child(groupID).child("invited").child(inviteduser).setValue(nickname);
                                    etSearch.setHint("Enter email");
                                    etSearch.setText("");

                                    hideKeyboardFrom(getContext(), getView());

                                    Toast toast = makeText(getContext(), "Invitation sent", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.BOTTOM,0,600);
                                    toast.show();
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
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
