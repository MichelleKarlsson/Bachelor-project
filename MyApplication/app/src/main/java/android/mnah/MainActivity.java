package android.mnah;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private Button mStartButton;
    private Button mAboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);


        mStartButton = findViewById(R.id.start_button);
        mAboutButton = findViewById(R.id.about_button);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent summaryIntent = new Intent(MainActivity.this, SummaryActivity.class);
                startActivity(summaryIntent);
            }
        });

        mAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.container);
                fragment = new AboutFragment();
                fm.beginTransaction().add(R.id.container, fragment).addToBackStack("about").commit();

            }
        });
    }


}
