package io.transmission_project.huntertalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import io.transmission_project.huntertalk.ui.firstLaunch.RegistrationActivity;

public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView mTitleWindow = (TextView) findViewById(R.id.titleWindow);
        TextView mMessageWindow = (TextView) findViewById(R.id.messageWindow);

        mTitleWindow.setText("Privacy Policy for Hunter Talk");
        mMessageWindow.setText(getString(R.string.privacy_policy_text));

        final Button proceedButton = (Button)findViewById(R.id.proceed_to_registration);
        CheckBox checkbox = (CheckBox)findViewById(R.id.privacy_checkbox);
        proceedButton.setEnabled(false);

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    proceedButton.setEnabled(true);
                }
                else{
                    proceedButton.setEnabled(false);
                }
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivacyPolicy.this, RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
