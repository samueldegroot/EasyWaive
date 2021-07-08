package com.myapp.easywaiver;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


import static com.myapp.easywaiver.EasyWaiveRepository.SKU_EASY_WAIVE_APP_SUBSCRIPTION;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int FILE_REQUEST_CODE = 8777;
    private HomeActivityViewModel homeActivityViewModel;
    static public Boolean isActive;
    private static final int UPLOAD_REQUEST_CODE = 8778;
    String organization;
    String banner;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.home_toolbar);
        setActionBar(myToolbar);
        myToolbar.inflateMenu(R.menu.menu);
        verifyStoragePermissions(this);
        //getActionBar().setIcon(R.mipmap.ic_launcher_round); //icon too big to use :(


        organization = read_file(getApplicationContext(), "organization.txt");
        banner = read_file(getApplicationContext(), "banner.txt");

        Button new_form_button = findViewById(R.id.new_form);
        Button org_button = findViewById(R.id.org);
        Button share_button = findViewById(R.id.share);
        org_button.setText("View Forms");
        TextView banner_tv = findViewById(R.id.banner_tv);
        if (banner == "Organization Name") {
            banner = "Welcome";
        }
        else {
            banner_tv.setText(banner);
        }

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

        isActive = false;

        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                ewr.billingDataSource.isActiveSubcription(SKU_EASY_WAIVE_APP_SUBSCRIPTION);
                //Log.v("isActive", isActive.toString());
            }
        };
        handler.postDelayed(r, 500);

        new_form_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActive) {
                    Intent myIntent = new Intent(HomeActivity.this, ReleaseFormActivity.class);
                    myIntent.putExtra("org", organization);
                    HomeActivity.this.startActivity(myIntent);
                } else {
                    ewr.billingDataSource.launchBillingFlow(HomeActivity.this, SKU_EASY_WAIVE_APP_SUBSCRIPTION);
                }
            }
        });

        org_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(false)
                        .setShowVideos(false)
                        .setShowFiles(true)
                        .setSingleChoiceMode(false)
                        .setSuffixes("pdf", "csv")
                        .setRootPath(Environment.DIRECTORY_DOCUMENTS + "/EasyWaive")
                        .build());
                startActivityForResult(intent, FILE_REQUEST_CODE);
            }

        });

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile(lastPdf);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EasyWaiveRepository ewr = ((EasyWaiveApplication) HomeActivity.this.getApplication()).appContainer.easyWaiveRepository;
        ewr.billingDataSource.isActiveSubcription(SKU_EASY_WAIVE_APP_SUBSCRIPTION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent;
        switch ( item.getItemId( ) ) {
            case R.id.change_org:
                myIntent = new Intent(HomeActivity.this, ChangeOrgActivity.class);
                myIntent.putExtra("org", organization);
                HomeActivity.this.startActivity(myIntent);
                return true;

            case R.id.set_banner:
                myIntent = new Intent(HomeActivity.this, SetBannerActivity.class);
                myIntent.putExtra("banner", banner);
                HomeActivity.this.startActivity(myIntent);
                return true;

            case R.id.set_background:
                //set background
                return true;

            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                // Add the buttons
                builder.setPositiveButton(R.string.back, null);
                // Set other dialog properties
                builder.setMessage(R.string.dialog_message);
                builder.setTitle(R.string.about);

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareFile(File myFile) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        if (myFile.exists()) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FILE_REQUEST_CODE:
                    ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                    MediaFile file = files.get(0);

                    /*
                    File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/"+file.getName());
                    Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", myFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                     */
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(HomeActivity.this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>

     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity the activity from which permissions are checked
     */
    public static void verifyStoragePermissions(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }
}

    //graveyard
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

