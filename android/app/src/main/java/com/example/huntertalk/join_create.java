package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huntertalk.ui.login.LoginActivity;

public class join_create extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        Button joinButton= findViewById(R.id.btjoin);
        final EditText groupIDInput = findViewById(R.id.etgroupid);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Join Group");
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        joinButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i =  new Intent(join_create.this, InsideGroupActivity.class);
                i.putExtra("groupID", groupIDInput.getText().toString());
                startActivity(i);
            }
        });


    }
    boolean secondPress =false;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (secondPress){
                    Intent intent = new Intent(join_create.this, Home_page.class);
                    startActivity(intent);}
                else{
                    Toast message= Toast.makeText(join_create.this, "Press once again to cancel joining a group",
                            Toast.LENGTH_LONG);
                    message.setGravity(Gravity.TOP, 0,0);
                    message.show();
                    secondPress=true;
                }
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
