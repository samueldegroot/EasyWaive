package com.myapp.easywaiver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;


public class ReleaseFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_form);
        Toolbar myToolbar = findViewById(R.id.form_toolbar);
        setActionBar(myToolbar);

        View myView = findViewById(R.id.release_constraint);

        //set background
        HomeActivity.loadAndSetBackground(this, myView, getString(R.string.preference_file_key));

        String organization = getIntent().getStringExtra("org");
        String org_email = getIntent().getStringExtra("org_email");
        organization = organization.replace("\n", "");

        String waiver_text = "I hereby grant the " + organization + " (the “Licensee”) the right to use photographs or video recordings (“photos”) of me on its websites and in publication without compensation to me.\n\n" +

        "Such photos of me may be placed on the Internet and that I may be identified by name or regarding the photos.  I waive the right to approve the final product and agree that all such portraits, pictures, photographs, video and audio recordings are and shall remain the property of the Licensee.\n\n" +

        "I hereby release and forever discharge the Licensee from any and all claims, demands, rights, promises, damages and liabilities arising out of or in connection with the use or distribution of the photos, including but not limited to any claims for invasion of privacy, appropriation of likeness or defamation.\n\n" +

        "I agree to accept emails or text messages from the Licensee until I opt out of receiving these materials. This release is binding on me and my heirs, assigns and personal representatives.\n";


        TextView tv = findViewById(R.id.waiver_text);
        ScrollView sv = findViewById(R.id.scrollView2);

        tv.setText(waiver_text);

        Button agree_button = findViewById(R.id.agree_button);
        agree_button.setEnabled(false);
        agree_button.setAlpha((float) 0.3);

        sv.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (sv.getChildAt(0).getBottom()
                                == (sv.getHeight() + sv.getScrollY())) {
                            //scroll view is at bottom
                            agree_button.setEnabled(true);
                            agree_button.setAlpha((float) 1);
                        } else {
                            //scroll view is not at bottom
                            agree_button.setEnabled(false);
                            agree_button.setAlpha((float) 0.3);
                        }
                    }
                });

        agree_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ReleaseFormActivity.this, MainActivity.class);
                myIntent.putExtra("waiver", waiver_text);
                myIntent.putExtra("org_email", org_email);
                ReleaseFormActivity.this.startActivity(myIntent);
            }

        });
    }
}