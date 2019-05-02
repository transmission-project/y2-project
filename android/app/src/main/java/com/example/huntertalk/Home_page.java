package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Home_page extends AppCompatActivity  {

    Button bjoin;
    String st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        bjoin = (Button) findViewById(R.id.bjoin);
        bjoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this,join_create.class);
                startActivity(i);
            }
        });

        Button createButton = findViewById(R.id.button3);
        createButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this,CreateGroupPage.class);
                startActivity(i);
            }
        });
    }

}
