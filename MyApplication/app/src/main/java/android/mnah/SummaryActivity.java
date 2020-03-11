package android.mnah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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


import java.io.ByteArrayOutputStream;
import java.io.File;
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

    private FirebaseAutoMLRemoteModel remoteModel;
    private FirebaseAutoMLLocalModel localModel;
    private FirebaseVisionImageLabeler labeler;
    private Bitmap bmp;



    @Override
    public void setCondition(String condition) {
        this.condition = condition;
        System.out.println("***************************************** condition: " + condition);

    }

    @Override
    public void setPrice(int price) {
        this.price = price;
        System.out.println("***************************************** price: " + price);

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


        updateImageView();


        //Initialize the remote ML model:
        remoteModel = new FirebaseAutoMLRemoteModel.Builder("Devices_202022792454").build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build(); //add .requireWifi() before build here
        FirebaseModelManager.getInstance().download(remoteModel,conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        System.out.println("Download of model successful");
                    }
                });

        //Initialize the local ML model:
        localModel = new FirebaseAutoMLLocalModel.Builder().setAssetFilePath("manifest.json").build();

        makeLabeler();

    }


    private void makeLabeler() {

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                if (downloaded) {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                } else {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                }
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                        .setConfidenceThreshold(0.5f)
                        .build();

                try {
                    labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                } catch (FirebaseMLException e) {
                    Log.e("Labeler", "Labeler init failed", e);
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

        }
    }


    protected Task<List<FirebaseVisionImageLabel>> detectImage(final FirebaseVisionImage img) {
        return labeler.processImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                //TODO: Find out how to alert the user if no label was found
                for (FirebaseVisionImageLabel lab : firebaseVisionImageLabels) {
                    System.out.println(String.format("Label: %s, Confidence: %4.2f", lab.getText(), lab.getConfidence()));
                    createDescription(lab);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Detection error", "Exception occurred while trying to run model on image", e);
            }
        });
    }

    public void createDescription(FirebaseVisionImageLabel label) {

        //NLGElement s1 = SimpleNLG.getFactory().createSentence("i am happy");

        /*
        SimpleNLG simpleNLG = new SimpleNLG();
        String entity = label.getText();
        String[] parts = entity.split("-"); //the categories: parts[0] = laptop/phone, parts[1] = brand, parts[2] = color (only phones)
        SPhraseSpec s1 = simpleNLG.getFactory().createClause();

        //create initial sentence, make it present tense and start it with "This is.."
        s1.setFeature(Feature.TENSE, Tense.PRESENT);
        s1.setSubject("This");
        s1.setVerb("be");
        //Look up the words identified in the lexicon
        WordElement brand = simpleNLG.getLexicon().getWord(parts[1]);
        WordElement type = simpleNLG.getLexicon().getWord(parts[0]);
        String brandword = simpleNLG.getRealiser().realise(brand).getRealisation();
        String brandcap = brandword.substring(0,1).toUpperCase() + brandword.substring(1);
        String typeword = simpleNLG.getRealiser().realise(type).getRealisation();

        switch (parts[0]) {
            case "laptop":
                NPPhraseSpec item = simpleNLG.getFactory().createNounPhrase(brandcap + " " + typeword);
                item.setDeterminer("a");
                s1.addComplement(item);
                break;
            case "phone":
                WordElement color = simpleNLG.getLexicon().getWord(parts[2]);
                String colorString = simpleNLG.getRealiser().realise(color).getRealisation();
                NPPhraseSpec phoneitem = simpleNLG.getFactory().createNounPhrase(colorString + " " + brandcap + " " + typeword);
                phoneitem.setDeterminer("a");
                s1.addComplement(phoneitem);
                break;
            default:
                break;
        }

        String output = simpleNLG.getRealiser().realiseSentence(s1);
        mSummaryText.setText(output);*/

        final String entity = label.getText();
        String[] parts = entity.split("-");
        switch (parts[0]) {
            case "phone" :
                mSummaryText.setText("Is this a " + parts[2] + " " + parts[1] + " " + parts[0] + "? Press 'Next' to confirm, or take a new picture.");
                break;
            case "laptop":
                mSummaryText.setText("Is this a " + parts[1] + " " + parts[0] + "? Press 'Next' to confirm, or take a new picture.");
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


    public File getPictureFile(Picture pic){
            File filesDir = mContext.getFilesDir();
            return new File(filesDir, pic.getFileName());

        }
}
