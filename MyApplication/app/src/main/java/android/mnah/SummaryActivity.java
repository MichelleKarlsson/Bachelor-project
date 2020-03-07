package android.mnah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import simplenlg.framework.NLGElement;
import simplenlg.phrasespec.SPhraseSpec;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
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

import simplenlg.framework.*;
import simplenlg.lexicon.*;
import simplenlg.realiser.english.*;
import simplenlg.phrasespec.*;
import simplenlg.features.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {


    private Context mContext;
    private Picture mPicture;
    private File mPictureFile;
    private Uri mUri;
    private ImageView mImageView;
    private ImageButton mPictureButton;
    private TextView mSummaryText;
    private Button mDescribeButton;

    private FirebaseAutoMLRemoteModel remoteModel;
    private FirebaseAutoMLLocalModel localModel;
    private FirebaseVisionImageLabeler labeler;
    private Bitmap bmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_summary);

        mContext = getApplicationContext();
        mPicture = new Picture();
        mPictureFile = getPictureFile(mPicture);

        mSummaryText = findViewById(R.id.summarytext);
        mImageView = findViewById(R.id.imageview);
        mPictureButton = findViewById(R.id.picture_button);
        mPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mDescribeButton = findViewById(R.id.describe_button);
        mDescribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImageView();

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

        System.out.println("********************** Some kind of model has been initialized");
        makeLabeler();

    }


    private void makeLabeler() {

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                if (downloaded) {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                    System.out.println("*************** remote labeler");
                } else {
                    optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                    System.out.println("************* local labeler");
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

        System.out.println("**************** Labeler has been initialized");
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
            //runOnImage();

            System.out.println("******************* updateImageView()");
        }
    }

    /*private void runOnImage() {
        FirebaseVisionImage img = FirebaseVisionImage.fromBitmap(bmp);
        labeler.processImage(img).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                for (FirebaseVisionImageLabel label : firebaseVisionImageLabels) {
                    System.out.println("********************* A label: " + label);
                    String text = label.getText();
                    float conf = label.getConfidence();
                    mSummaryText.append(text + " " + conf + "\n");
                }
                System.out.println("************************ runOnImage successful");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("runOnImage()", "Model failed", e);
            }
        });
    }*/

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
        /*String[] parts;
        String article = "";
        String description;
        String type;
        String brand;
        String color = "";
        String entity = label.getText();

        //Split the label into it's parts: type (laptop/phone), brand and if phone then also color
        parts = entity.split("-");
        type = parts[0];
        brand = parts[1];

        if (type.equals("phone")) {
            color = parts[2];
            article = "a"; //because the two possible colors both start with a consonant.
        } else {
            String[] vowels = new String[] {"a","e","i","o","u","y"};
            for (int i = 0; i < vowels.length; i++) {
                if (brand.startsWith(vowels[i])) {
                    article = "an";
                } else {
                    article = "a";
                }
            }

        }


        if (type.equals("phone")) {
            description = "This is " + article + " " + color + " " + brand + " " + type;
        } else {
            description = "This is " + article + " " + brand + " " + type;
        }

        mSummaryText.setText(description + " (Confidence: " + (label.getConfidence() * 100 + "%") + ")");
        */

        //NLGElement s1 = SimpleNLG.getFactory().createSentence("i am happy");

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
        mSummaryText.setText(output);
    }

    public File getPictureFile(Picture pic){
            File filesDir = mContext.getFilesDir();
            return new File(filesDir, pic.getFileName());

        }
}
