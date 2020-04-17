package android.mnah;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class ExtraInfoFragmentTests {

    @Before
    public void setup() {
        Intents.init();
    }

    @Test
    public void testExtraInfoFragmentGUI() {
        FragmentScenario<ExtraInfoFragment> fs = FragmentScenario.launchInContainer(ExtraInfoFragment.class,null,R.style.AppTheme
                ,null);
        onView(withId(R.id.condition_text)).check(matches(withText("Condition")));
        onView(withId(R.id.price_text)).check(matches(withText("Asking price")));
    }

    @Test
    public void spinnerSelectionTest() {
        FragmentScenario<ExtraInfoFragment> fs = FragmentScenario.launchInContainer(ExtraInfoFragment.class,null,R.style.AppTheme
                ,null);
        String text = "Refurbished";
        onView(withId(R.id.conditionspinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(text))).perform(click());
        onView(withId(R.id.conditionspinner)).check(matches(withSpinnerText(containsString(text))));
    }

    @Test
    public void writingInPriceFieldTest() {
        FragmentScenario<ExtraInfoFragment> fs = FragmentScenario.launchInContainer(ExtraInfoFragment.class,null,R.style.AppTheme
                ,null);
        onView(withId(R.id.price)).perform(typeText("3000"));
        onView(withId(R.id.price)).check(matches(withText("3000")));

    }

    @After
    public void close() {
        Intents.release();
    }

}
