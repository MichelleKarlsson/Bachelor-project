package android.mnah;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

public class Picture {

    private UUID mPictureID;

    public Picture() {
        mPictureID = UUID.randomUUID();
    }

    public UUID getId() {
        return this.mPictureID;
    }

    public String getFileName() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public static FirebaseVisionImage getVisionImage(Bitmap bmp) {
        return FirebaseVisionImage.fromBitmap(bmp);
    }

}
