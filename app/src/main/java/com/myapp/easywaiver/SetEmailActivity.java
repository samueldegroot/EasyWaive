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

public class SetEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_email);
        String email = getIntent().getStringExtra("org_email");
        EditText editText = findViewById(R.id.email_tv);
        editText.setHint(email);
        Button email_button = findViewById(R.id.email_button);
        Button back_button = findViewById(R.id.back_button);
        Toolbar myToolbar = findViewById(R.id.email_toolbar);
        setActionBar(myToolbar);

        //enable update button after user types something
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                email_button.setEnabled(true);
                email_button.setAlpha(1);
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
                Intent myIntent = new Intent(SetEmailActivity.this, HomeActivity.class);
                SetEmailActivity.this.startActivity(myIntent);
            }
        });

        //store input into org_email.txt in internal storage
        email_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = editText.getText().toString();
                String FILENAME = "org_email.txt";

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

                Intent myIntent = new Intent(SetEmailActivity.this, HomeActivity.class);
                myIntent.putExtra("email", string);
                SetEmailActivity.this.startActivity(myIntent);
            }
        });

    }
}