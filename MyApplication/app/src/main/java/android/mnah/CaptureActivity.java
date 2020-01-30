package android.mnah;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_capture);
    }
}
