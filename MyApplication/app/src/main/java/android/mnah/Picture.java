package android.mnah;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

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


}
