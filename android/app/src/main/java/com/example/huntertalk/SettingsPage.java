package com.example.huntertalk;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.huntertalk.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        Button logoutbutton = findViewById(R.id.logoutbutton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        logoutbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout();


            }
        });
    }
    private void logout(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    boolean secondPress =false;
    boolean changed = true;
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if(!changed){
                    Intent intent = new Intent(SettingsPage.this, InsideGroupActivity.class);
                    startActivity(intent);
                }else{
                    if (secondPress){
                        Intent intent = new Intent(SettingsPage.this, InsideGroupActivity.class);
                        startActivity(intent);}
                    else{
                        Toast message= Toast.makeText(SettingsPage.this, "Press once again to cancel the changes",
                                Toast.LENGTH_LONG);
                        message.setGravity(Gravity.TOP, 0,0);
                        message.show();
                        secondPress=true;
                    }}
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
