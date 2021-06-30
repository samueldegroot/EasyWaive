package com.myapp.easywaiver;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    int pageHeight = 1120;
    int pageWidth = 792;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        setContentView(R.layout.activity_main);
        String waiver_text = getIntent().getStringExtra("waiver");
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setActionBar(myToolbar);

        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Toast.makeText(MainActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                mSaveButton.setAlpha(1);
                mClearButton.setAlpha(1);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
                mSaveButton.setAlpha((float) 0.3);
                mClearButton.setAlpha((float) 0.3);
            }

        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }

        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();

                EditText name = findViewById(R.id.name_text);
                String nameStr = name.getText().toString();
                EditText email = findViewById(R.id.email_text);
                String emailStr = email.getText().toString();

                if (emailStr.contains("@")) {
                    try {
                        writeEmail(emailStr);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                createPDF(nameStr, waiver_text, signatureBitmap);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Cannot write images to external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public File getDocumentStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), albumName);
        //File file = new File(getExternalFilesDir(null), albumName);
        if (!file.mkdirs()) {
            //Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    public boolean addPDFToDocuments(PdfDocument pdfDocument, String name) {
        boolean result = false;
        try {
            File pdf = new File(getDocumentStorageDir("EasyWaive"), String.format("%s Release.pdf", name));
            pdfDocument.writeTo(new FileOutputStream(pdf));
            scanMediaFile(pdf);
            pdfDocument.close();
            //shareFile(pdf);

            Toast.makeText(MainActivity.this, "Generated Signed PDF", Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // Add the buttons
            builder.setPositiveButton(R.string.continue_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent myIntent = new Intent(MainActivity.this, HomeActivity.class);
                    myIntent.putExtra("pdfFile", pdf);
                    MainActivity.this.startActivity(myIntent);
                }
            });
            // Set other dialog properties
            //builder.setMessage(R.string.dialog_message);
            builder.setTitle(R.string.thanks);

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();

            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(mediaScanIntent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            this.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        MainActivity.this.sendBroadcast(mediaScanIntent);
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

    public void writeEmail(String email) throws FileNotFoundException {
        //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "EasyWaive/emails.csv");
        //File file = new File(getExternalFilesDir(null), "EasyWaive/emails.csv");
        File file = new File(getDocumentStorageDir("EasyWaive"), "emails.csv");
        FileOutputStream fos = new FileOutputStream(file, true);
        try {
            fos.write(email.getBytes());
            fos.write(",".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        scanMediaFile(file);
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(MainActivity.this, "Wrote email", Toast.LENGTH_SHORT).show();
    }

    public void createPDF(String nameStr, String waiver_text, Bitmap signatureBitmap) {
        PdfDocument pdfDocument = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        TextPaint title = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        textPaint.setTextSize((int) (14));

        title.setTextAlign((Paint.Align.CENTER));
        title.setColor(Color.rgb(61, 61, 61));

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();

        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.
        Bitmap scaledbmp = Bitmap.createScaledBitmap(signatureBitmap, 180, 120, false);
        canvas.drawBitmap(scaledbmp, 64, 520, paint);

        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15);

        // below line is sued for setting color
        // of our text inside our PDF file.
        //title.setColor(ContextCompat.getColor(MainActivity.this, R.color.purple_200));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.
        canvas.drawText("VIDEO and PHOTOGRAPH RELEASE FORM", (792)/2, 80, title);


        int textWidth = canvas.getWidth() - 96;
        StaticLayout textLayout = new StaticLayout(waiver_text, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        //int textHeight = textLayout.getHeight();

        canvas.save();
        canvas.translate((792-textWidth)/2, 120);
        textLayout.draw(canvas);
        canvas.restore();

        //canvas.drawText(waiver_text, 209, 120, title);

        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setTextSize(14);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("_______________________________________________", 48, 640, title);
        canvas.drawText("Signature", 48, 655, title);

        canvas.drawText("Printed Name of Individual Photographed/Recorded: __________________________________________", 48, 720, title);
        canvas.drawText(nameStr, 792/2-16, 718, title);

        title.setTextAlign(Paint.Align.RIGHT);
        String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        canvas.drawText(currentDate, 792-48, 635, title);
        canvas.drawText("_______________________", 792-48, 640, title);
        canvas.drawText("Date", 792-48, 655, title);

        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);
        //Toast.makeText(MainActivity.this, "finished page", Toast.LENGTH_SHORT).show();

        addPDFToDocuments(pdfDocument, nameStr);
    }




    //Graveyard
    /*
                if (addJpgSignatureToGallery(signatureBitmap)) {
                    Toast.makeText(MainActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }

                if (addSvgSignatureToGallery(mSignaturePad.getSignatureSvg())) {
                    Toast.makeText(MainActivity.this, "SVG Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unable to store the SVG signature", Toast.LENGTH_SHORT).show();
                }


                 */
    /*
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

     */
    /*
    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }
    */

    /*
    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(getAlbumStorageDir("EasyWaive"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            scanMediaFile(photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

     */
    /*
    public boolean addSvgSignatureToGallery(String signatureSvg) {
        boolean result = false;
        try {
            File svgFile = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg", System.currentTimeMillis()));
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    */
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId( ) ) {
            case R.id.rate:

                return true;

            case R.id.settings:
                Intent myIntent = new Intent(MainActivity.this, Settings.class);
                MainActivity.this.startActivity(myIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToAnActivity(View view) {
        Intent Intent = new Intent(this, Settings.class);
        startActivity(Intent);
    }


     */
}
