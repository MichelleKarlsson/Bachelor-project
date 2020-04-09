package android.mnah;

import android.content.Intent;
import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
@Config(sdk=28)
public class MainActivityTests {

    MainActivity act;
    @Before
    public void setup() throws Exception {
        act = Robolectric.setupActivity(MainActivity.class);

    }

    @Test
    public void clickStartNavigatesToSummaryActivity() {
        act.findViewById(R.id.start_button).performClick();

        Intent expected = new Intent(act, SummaryActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expected.getComponent(),actual.getComponent());
    }


    @Test
    public void mainActivityHasButtons() {

        assertNotNull(act.findViewById(R.id.start_button));
        assertNotNull(act.findViewById(R.id.about_button));
    }

}
