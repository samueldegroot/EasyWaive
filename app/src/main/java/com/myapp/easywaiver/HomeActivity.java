package com.myapp.easywaiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.myapp.easywaiver.billing.BillingDataSource;
import com.myapp.easywaiver.EasyWaiveRepository;
import static com.myapp.easywaiver.EasyWaiveRepository.SKU_EASY_WAIVE_APP_SUBSCRIPTION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import com.myapp.easywaiver.HomeActivityViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private HomeActivityViewModel homeActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.home_toolbar);
        setActionBar(myToolbar);
        //getActionBar().setIcon(R.mipmap.ic_launcher_round); //icon too big to use :(


        String organization = read_file(getApplicationContext(), "organization.txt");
        //Toast.makeText(HomeActivity.this, organization, Toast.LENGTH_SHORT).show();

        Button new_form_button = findViewById(R.id.new_form);
        Button org_button = findViewById(R.id.org);
        Button share_button = findViewById(R.id.share);
        Button about_button = findViewById(R.id.about);

        //get last signed pdf, if exists enable share button
        File lastPdf = (File) getIntent().getSerializableExtra("pdfFile");
        if (lastPdf != null) {
            share_button.setEnabled(true);
            share_button.setAlpha(1);
        }

        // Create our Activity ViewModel, which exists to handle global Snackbar messages
        HomeActivityViewModel.HomeActivityViewModelFactory homeActivityViewModelFactory = new
                HomeActivityViewModel.HomeActivityViewModelFactory(
                ((EasyWaiveApplication) getApplication()).appContainer.
                        easyWaiveRepository);
        homeActivityViewModel = new ViewModelProvider(this, homeActivityViewModelFactory)
                .get(HomeActivityViewModel.class);
        // Allows billing to refresh purchases during onResume
        getLifecycle().addObserver(homeActivityViewModel.getBillingLifecycleObserver());

        EasyWaiveRepository ewr = ((EasyWaiveApplication) HomeActivity.this.getApplication()).appContainer.easyWaiveRepository;

        //getLifecycle().addObserver(ewr.getBillingLifecycleObserver());

        //Log.v("billing flow", ewr.getBillingFlowInProcess().getValue().toString());
        //Log.v("skutitle", ewr.getSkuTitle(SKU_EASY_WAIVE_APP_SUBSCRIPTION).getValue());
        //ewr.buySku(HomeActivity.this, SKU_EASY_WAIVE_APP_SUBSCRIPTION);
        //Log.v("ispurchased", Objects.requireNonNull(((EasyWaiveApplication) HomeActivity.this.getApplication()).appContainer.easyWaiveRepository.isPurchased(SKU_EASY_WAIVE_APP_SUBSCRIPTION).getValue()).toString());

        new_form_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ewr.billingDataSource.isActiveSubcription(SKU_EASY_WAIVE_APP_SUBSCRIPTION)) {
                    Intent myIntent = new Intent(HomeActivity.this, ReleaseFormActivity.class);
                    myIntent.putExtra("org", organization);
                    HomeActivity.this.startActivity(myIntent);
                }
                else {
                    ewr.billingDataSource.launchBillingFlow(HomeActivity.this, SKU_EASY_WAIVE_APP_SUBSCRIPTION);
                }

                /*
                if (ewr.isPurchased(SKU_EASY_WAIVE_APP_SUBSCRIPTION).getValue()) {//if active subscription
                    Intent myIntent = new Intent(HomeActivity.this, ReleaseFormActivity.class);
                    myIntent.putExtra("org", organization);
                    HomeActivity.this.startActivity(myIntent);
                }
                else{ //ask to get subscription
                    ewr.buySku(HomeActivity.this, SKU_EASY_WAIVE_APP_SUBSCRIPTION);
                }

                 */
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
                shareFile(lastPdf);
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

    /*
    private void noSKUMessage() {
        //do nothing?
    }

    private void setUpBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        startConnection();
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };



    private void handlePurchase(Purchase purchase) {
        Log.v("TAG_INAPP","handlePurchase : ${purchase}");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                        //do nothing
                    }
                };
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.v("TAG_INAPP","Billing Setup Done");
                    //queryAvailableProducts();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                //try to restart haha
            }
        });
    }

    private void queryAvailableProducts() {
        List<String> skuList = new ArrayList<> ();
        skuList.add("easy_waive_app_subscription");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        Log.v("TAG_INAPP","skuDetailsList : ${skuDetailsList}");
                    }
                });
    }
    */


}

