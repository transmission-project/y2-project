package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class join_create extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        Button joinButton= findViewById(R.id.btjoin);
        final EditText groupIDInput = findViewById(R.id.etgroupid);

        joinButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i =  new Intent(join_create.this, InsideGroupActivity.class);
                String groupID= "Group id: " + groupIDInput.getText().toString();
                i.putExtra("groupID",groupID);
                startActivity(i);
            }
        });


    }
}
