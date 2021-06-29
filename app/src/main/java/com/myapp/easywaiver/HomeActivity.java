package com.myapp.easywaiver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HomeActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.home_toolbar);
        setActionBar(myToolbar);

        String organization = read_file(getApplicationContext(), "organization.txt");
        //Toast.makeText(HomeActivity.this, organization, Toast.LENGTH_SHORT).show();

        Button new_form_button = findViewById(R.id.new_form);
        Button org_button = findViewById(R.id.org);
        Button share_button = findViewById(R.id.share);
        Button about_button = findViewById(R.id.about);

        new_form_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(HomeActivity.this, ReleaseFormActivity.class);
                myIntent.putExtra("org", organization);
                HomeActivity.this.startActivity(myIntent);
            }
        });

        org_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(HomeActivity.this, ChangeOrgActivity.class);
                myIntent.putExtra("org", organization);
                HomeActivity.this.startActivity(myIntent);
            }

        });

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File lastPdf = (File) getIntent().getSerializableExtra("pdfFile");
                if (lastPdf != null) {
                    shareFile(lastPdf);
                }
            }
        });

        about_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                // Add the buttons
                builder.setPositiveButton(R.string.back, null);
                // Set other dialog properties
                builder.setMessage(R.string.dialog_message);
                builder.setTitle(R.string.about);

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void shareFile(File myFile){
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        if(myFile.exists()) {
            intentShareFile.setType("application/pdf");
            Uri myUri = FileProvider.getUriForFile(
                    HomeActivity.this,
                    "com.myapp.easywaiver.provider", //(use your app signature + ".provider" )
                    myFile);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, myUri);

            grantUriPermission("com.myapp.easywaiver.provider", myUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            grantUriPermission("com.myapp.easywaiver.provider", myUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission("com.myapp.easywaiver.provider", myUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            //intentShareFile.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            this.startActivity(Intent.createChooser(intentShareFile, "Upload signed form"));
        }
    }

    public String read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return "Organization Name";
        } catch (UnsupportedEncodingException e) {
            return "Organization Name";
        } catch (IOException e) {
            return "Organization Name";
        }
    }

}

