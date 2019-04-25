package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class join_create extends AppCompatActivity {

    Button btjoin;
    EditText etgroupid;
    String st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        btjoin= findViewById(R.id.btjoin);
        etgroupid = findViewById(R.id.etgroupid);

        btjoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(join_create.this,group_page.class);
                st = "Group id: " + etgroupid.getText().toString();
                i.putExtra("Value",st);
                startActivity(i);

            }
        });


    }
}
