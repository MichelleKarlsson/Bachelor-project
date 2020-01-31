package android.mnah;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {


    private Context mContext;
    private Picture mPicture;
    private File mPictureFile;
    private Uri mUri;
    private ImageView mImageView;
    private ImageButton mPictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_summary);

        mContext = getApplicationContext();
        mPicture = new Picture();
        mPictureFile = getPictureFile(mPicture);

        mImageView = findViewById(R.id.imageview);
        mPictureButton = findViewById(R.id.picture_button);
        mPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        updateImageView();


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
            Bitmap bmp = PictureUtils.getScaledBitmap(mPictureFile.getPath(), this);
            mImageView.setImageBitmap(bmp);
        }
    }

    public File getPictureFile(Picture pic){
            File filesDir = mContext.getFilesDir();
            return new File(filesDir, pic.getFileName());
        }
}
