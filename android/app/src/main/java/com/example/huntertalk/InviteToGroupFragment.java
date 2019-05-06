package com.example.huntertalk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InviteToGroupFragment extends Fragment {

    View myView;
    private DatabaseReference userDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.invite_to_group_layout, container, false);

        super.onCreate(savedInstanceState);
        getActivity().setContentView(R.layout.invite_to_group_layout);

        final EditText etSearch = getActivity().findViewById(R.id.etsearchmember);
        Button searchButton = getActivity().findViewById(R.id.btnsearch);

        userDb = FirebaseDatabase.getInstance().getReference("users");

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final String uid = auth.getCurrentUser().getUid();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etSearch.getText().toString().trim();

                if (!email.contains("@") || !email.contains(".")) {
                    etSearch.setError("Invalid email addresss");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    etSearch.setError("Enter Email");
                    return;
                }
                System.out.println("  Email checks passed");
            }
            });


        return myView;
    }
}
