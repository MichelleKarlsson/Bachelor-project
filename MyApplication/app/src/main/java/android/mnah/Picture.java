package android.mnah;

import java.util.UUID;


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
