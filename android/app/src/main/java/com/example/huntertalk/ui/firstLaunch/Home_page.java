package com.example.huntertalk.ui.firstLaunch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.huntertalk.userRelated.FriendList;
import com.example.huntertalk.R;
import com.example.huntertalk.userRelated.SettingsPage;
import com.example.huntertalk.everythingWithGroups.CreateGroupPage;
import com.example.huntertalk.everythingWithGroups.JoinAGroupById;

public class Home_page extends AppCompatActivity  {

    Button bjoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        bjoin = (Button) findViewById(R.id.bjoin);
        bjoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this, JoinAGroupById.class);
                startActivity(i);
            }
        });

        Button createButton = findViewById(R.id.button3);
        createButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent i=new Intent(Home_page.this, CreateGroupPage.class);
                startActivity(i);
            }
        });


    }
    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mybutton) {
            Intent i=new Intent(Home_page.this, SettingsPage.class);
            startActivity(i);
            this.finish();
        }
        if (id == R.id.addFriends) {
            Intent i=new Intent(Home_page.this, FriendList.class);
            startActivity(i);
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
