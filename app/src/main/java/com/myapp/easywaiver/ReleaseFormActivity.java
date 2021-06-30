package com.myapp.easywaiver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;


public class ReleaseFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_form);
        Toolbar myToolbar = findViewById(R.id.form_toolbar);
        setActionBar(myToolbar);

        String organization = getIntent().getStringExtra("org");
        organization = organization.replace("\n", "");

        //String organization = "Monica De La Cruz-Hernandez Campaign";
        String waiver_text = "I hereby grant " + organization + " the irrevocable right and permission to use photographs or video recordings (“photos”) of me on their websites and in publications, promotional flyers, educational materials, derivative works, or for any other similar purpose without compensation to me.\n\n" +
                "I understand and agree that such photos of me may be placed on the Internet.  I also understand and agree that I may be identified by name or title in printed, Internet or broadcast information that might accompany the photos of me.  I waive the right to approve the final product.  I agree that all such portraits, pictures, photographs, video and audio recordings, and any reproductions thereof, and all plates, negatives, recording tape and digital files are and shall remain their property.\n\n" +
                "I hereby release, acquit and forever discharge " + organization + " from any and all claims, demands, rights, promises, damages and liabilities arising out of or in connection with the use or distribution of said photos, including but not limited to any claims for invasion of privacy, appropriation of likeness or defamation.\n\n" +
                "I hereby warrant that I am over 17 years of age and competent to contract in my own name.  This release is binding on me and my heirs, assigns and personal representatives.";

        TextView tv = findViewById(R.id.waiver_text);

        tv.setText(waiver_text);

        Button agree_button = findViewById(R.id.agree_button);

        agree_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ReleaseFormActivity.this, MainActivity.class);
                myIntent.putExtra("waiver", waiver_text);
                ReleaseFormActivity.this.startActivity(myIntent);
            }

        });
    }
}