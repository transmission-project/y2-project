package com.example.huntertalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class group_page extends AppCompatActivity {

    TextView groupid;
    String st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);
        groupid=findViewById(R.id.groupid);

        st=getIntent().getExtras().getString("Value");
        groupid.setText(st);
    }
}
