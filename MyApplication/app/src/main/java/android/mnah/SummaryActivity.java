package android.mnah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;


import java.io.File;
import java.io.IOException;
import java.util.Currency;
import java.util.List;

public class SummaryActivity extends AppCompatActivity implements ExtraInfoFragment.SendData {


    private Context mContext;
    private Picture mPicture;
    private File mPictureFile;
    private Uri mUri;
    private ImageView mImageView;
    private ImageButton mPictureButton;
    private TextView mSummaryText;
    private Button mNextButton;

    private int price;
    private String condition;
    private String currency;
    private String color = "";

    private FirebaseVisionImageLabel mDeviceLabel;
    private FirebaseAutoMLRemoteModel remoteDeviceModel;
    private FirebaseAutoMLRemoteModel remoteColorModel;
    private FirebaseAutoMLLocalModel localDeviceModel;
    private FirebaseAutoMLLocalModel localColorModel;
    private FirebaseVisionImageLabeler deviceLabeler;
    private FirebaseVisionImageLabeler colorLabeler;
    private Bitmap bmp;

    private static final String[] LOCATION_PERMISSIONS = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};



    @Override
    public void setCondition(String condition) {
        this.condition = condition;
        //Since the condition cannot be nothing we can resume operation when this method has been called.
        updateFinalDescription();


    }

    @Override
    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_summary);

        mContext = getApplicationContext();
        mPicture = new Picture();
        mPictureFile = getPictureFile(mPicture);

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setEnabled(false);
        mSummaryText = findViewById(R.id.summarytext);
        mImageView = findViewById(R.id.imageview);
        mPictureButton = findViewById(R.id.picture_button);
        mPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        this.currency = getCurrency();
        updateImageView();


        //Initialize the remote ML models:
        remoteDeviceModel = new FirebaseAutoMLRemoteModel.Builder("Devices_202022792454").build();
        remoteColorModel = new FirebaseAutoMLRemoteModel.Builder("Colors_202043105816").build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build(); //add .requireWifi() before build here
        FirebaseModelManager.getInstance().download(remoteDeviceModel,conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println("Download of device model successful");
                    }
                });
        FirebaseModelManager.getInstance().download(remoteColorModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println("Download of color model successful");
                    }
                });

        //Initialize the local ML models:
        localDeviceModel = new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("manifest.json").build();
        localColorModel = new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("colormanifest.json").build();

        makeLabelers();

    }


    private void makeLabelers() {

        FirebaseModelManager.getInstance().isModelDownloaded(remoteDeviceModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                if (downloaded) {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteDeviceModel);
                } else {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localDeviceModel);
                }
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                        .setConfidenceThreshold(0.5f)
                        .build();

                try {
                    deviceLabeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                } catch (FirebaseMLException e) {
                    Log.e("DeviceLabeler", "Labeler init failed", e);
                }
            }
        });

        FirebaseModelManager.getInstance().isModelDownloaded(remoteColorModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optsBuilder;
                if (downloaded) {
                    optsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteColorModel);
                } else {
                    optsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localColorModel);
                }
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions opts = optsBuilder
                        .setConfidenceThreshold(0.5f)
                        .build();

                try {
                    colorLabeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(opts);
                } catch (FirebaseMLException e) {
                    Log.e("ColorLabeler", "Labeler init failed", e);
                }
            }
        });

    }



    private void takePicture() {
        final Intent capturePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (mPictureFile != null && capturePicture.resolveActivity(getPackageManager()) != null) {
            mUri = FileProvider.getUriForFile(this, "android.mnah.fileprovider", mPictureFile);
            capturePicture.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

            List<ResolveInfo> cameraActivities = this.getPackageManager().queryIntentActivities(capturePicture, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo act : cameraActivities) {
                this.grantUriPermission(act.activityInfo.packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            startActivityForResult(capturePicture, 2);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 2 :
                if (resultCode == RESULT_OK) {
                    updateImageView();
                    this.revokeUriPermission(mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                }
                break;
        }
    }



    private void updateImageView() {
        if (mPictureFile == null || !mPictureFile.exists()) {
            mImageView.setImageDrawable(null);
        } else {
            mSummaryText.setText("");
            bmp = PictureUtils.getScaledBitmap(mPictureFile.getPath(), this);
            mImageView.setImageBitmap(bmp);
            FirebaseVisionImage img = FirebaseVisionImage.fromBitmap(bmp);
            detectImage(img);
            detectColor(img);

        }
    }

    protected Task<List<FirebaseVisionImageLabel>> detectColor(final FirebaseVisionImage img) {
        return colorLabeler.processImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                for (FirebaseVisionImageLabel lab : firebaseVisionImageLabels) {
                    System.out.println("Color detected: " + lab.getText());
                    color = lab.getText();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Color detection error", "exception while running color model: ", e);
            }
        });
    }


    protected Task<List<FirebaseVisionImageLabel>> detectImage(final FirebaseVisionImage img) {
        return deviceLabeler.processImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                if (firebaseVisionImageLabels.isEmpty()) {
                    Toast.makeText(SummaryActivity.this, "Nothing detected, please take another picture", Toast.LENGTH_SHORT).show();
                }
                for (FirebaseVisionImageLabel lab : firebaseVisionImageLabels) {
                    System.out.println(String.format("Label: %s, Confidence: %4.2f", lab.getText(), lab.getConfidence()));
                    createInitialDescription(lab);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Device detection error", "exception while running device model: ", e);
            }
        });
    }

    public void createInitialDescription(FirebaseVisionImageLabel label) {
        mDeviceLabel = label;
        final String entity = label.getText();
        String[] parts = entity.split("-");
        switch (parts[0]) {
            case "phone" :
                setSummaryText("Is this a " + color + " " + parts[1] + " " + parts[0] + "? Press 'Next' to confirm, or take a new picture.");
                break;
            case "laptop":
                setSummaryText("Is this a " + color + " " + parts[1] + " " + parts[0] + "? Press 'Next' to confirm, or take a new picture.");
        }

        mNextButton.setEnabled(true);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = new ExtraInfoFragment();
                fm.beginTransaction().add(R.id.container_summary, fragment).addToBackStack("extrainfo").commit();
            }
        });

    }

    public void setSummaryText(String text) {
        mSummaryText.setText(text);
    }

    public void updateFinalDescription(){
        SimpleNLG simpleNLG = new SimpleNLG();
        String entity = mDeviceLabel.getText();
        String[] parts = entity.split("-"); //the categories: parts[0] = laptop/phone, parts[1] = brand
        String desc = simpleNLG.getFullDescription(parts[0], parts[1], color, condition, price, currency);
        setSummaryText(desc);
    }


    public String getCurrency() {
        String currencyCode = "";
        int r = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[0]);
        if (r == 0) {
            LocationManager locationManager = (LocationManager) getSystemService(SummaryActivity.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                Geocoder geo = new Geocoder(this);
                try {
                    List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Address addr = addresses.get(0);
                    currencyCode = Currency.getInstance(addr.getLocale()).getCurrencyCode();
                } catch (IOException e) {
                    Log.e("SummaryActivity", "IOException: " + e);
                }
            }
        } else {
            requestPermissions(LOCATION_PERMISSIONS, 0);
        }
        return currencyCode;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
                Toast.makeText(this,  "Please enable access to location services.", Toast.LENGTH_SHORT);
        }
    }


    public File getPictureFile(Picture pic){
            File filesDir = mContext.getFilesDir();
            return new File(filesDir, pic.getFileName());

        }
}
