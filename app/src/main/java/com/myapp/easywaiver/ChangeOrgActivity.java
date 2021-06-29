package com.myapp.easywaiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
        String org = getIntent().getStringExtra("org");
        EditText editText = findViewById(R.id.org_tv);
        editText.setText(org);
        Button org_button = findViewById(R.id.org_button);
        Toolbar myToolbar = findViewById(R.id.org_toolbar);
        setActionBar(myToolbar);

        //onclick
        org_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = editText.getText().toString();
                String FILENAME = "organization.txt";

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

                Intent myIntent = new Intent(ChangeOrgActivity.this, HomeActivity.class);
                myIntent.putExtra("org", string);
                ChangeOrgActivity.this.startActivity(myIntent);
            }
        });

    }
}