package com.myapp.easywaiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toolbar;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChangeOrgActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_org);
        String org = HomeActivity.loadString(this, "organization", "Organization Name", getString(R.string.preference_file_key));
        EditText editText = findViewById(R.id.org_tv);
        editText.setHint(org);
        Button org_button = findViewById(R.id.org_button);
        Button back_button = findViewById(R.id.back_button);
        Toolbar myToolbar = findViewById(R.id.org_toolbar);
        setActionBar(myToolbar);

        View myView = findViewById(R.id.org_constraint);

        //set background
        HomeActivity.loadAndSetBackground(this, myView, getString(R.string.preference_file_key));

        //enable update button after user types something
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                org_button.setEnabled(true);
                org_button.setAlpha(1);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //do nothing
            }
        });

        //back to home without updating
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ChangeOrgActivity.this, HomeActivity.class);
                ChangeOrgActivity.this.startActivity(myIntent);
            }
        });

        //store input into organization.txt in internal storage
        org_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = editText.getText().toString();
                //String FILENAME = "organization.txt";

                HomeActivity.saveString(ChangeOrgActivity.this, "organization", string, getString(R.string.preference_file_key));

                /*
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 */

                Intent myIntent = new Intent(ChangeOrgActivity.this, HomeActivity.class);
                //myIntent.putExtra("org", string);
                ChangeOrgActivity.this.startActivity(myIntent);
            }
        });

    }
}