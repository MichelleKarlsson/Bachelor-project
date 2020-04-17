package android.mnah;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.vision.label.ImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SummaryActivityTests {

    @Rule
    public IntentsTestRule<SummaryActivity> intentsRule = new IntentsTestRule<>(SummaryActivity.class);


    @Test
    public void testCameraIntent() {
        Bitmap icon = BitmapFactory.decodeResource(InstrumentationRegistry.getInstrumentation().getContext().getResources(),
                R.mipmap.ic_launcher);

        Intent resData = new Intent();
        resData.putExtra("data", icon);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resData);

        intending(toPackage("com.android.camera")).respondWith(result);
    }

    @Test
    public void clickNextButtonsInflatesExtraInfo() {
        onView(withId(R.id.next_button)).perform(click());
        onView(allOf(withId(R.id.extrainfofragment), withEffectiveVisibility(VISIBLE))).check(matches(isDisplayed()));
    }

    @Test
    public void extraInfoFragmentReturnsCorrectly() {
        onView(withId(R.id.next_button)).perform(click());
        withId(R.id.conditionspinner).matches(withSpinnerText("Like new"));
        onView(withId(R.id.price)).perform(replaceText("3000"));
        onView(withId(R.id.information_button)).perform(click());
    }


    @Test
    public void getCurrencyFindsLocale() {
        assertTrue(intentsRule.getActivity().getCurrency().equals("DKK"));
    }

    @Test
    public void setSummaryTextSetsText() {
        intentsRule.getActivity().setSummaryText("Hello world");
        onView(withId(R.id.summarytext)).check(matches(withText("Hello world")));
    }

    @Test
    public void getInitialDescriptionGivesInitialDescription() {
        FirebaseVisionImageLabel fl = new FirebaseVisionImageLabel(new ImageLabel(null,"phone-android-black", 80));
        assertEquals("Is this a black android phone? Press 'Next' to confirm, or take a new picture.", intentsRule.getActivity().createInitialDescription(fl, "black"));
    }
}



