package com.ksu.serene.B.D_Calendar;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import com.ksu.serene.ElapsedTimeIdlingResource;
import com.ksu.serene.R;
import com.ksu.serene.ToastMatcher;
import com.ksu.serene.controller.main.calendar.PatientMedicineDetailPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.TimeUnit;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)

public class PatientMedicineDetailPageTest {

    @Rule
    public ActivityTestRule<PatientMedicineDetailPage> activityTestRule = new ActivityTestRule<PatientMedicineDetailPage>(PatientMedicineDetailPage.class) {

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, PatientMedicineDetailPage.class);
            intent.putExtra("MedicineID", "868eafbd-c834-49d1-b5be-f14cb18b3f62");
            //cb2bf37a-41de-4330-a3bc-0476cded3286
            //d3277108-5c63-477c-af5b-a78c8bef0709
            return intent;
        }
    };

    @Before
    public void setUp() throws Exception {
       //medicineDetailPage = activityTestRule.getActivity();
        //add timer for all tests
        //Mack sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5000);
        try {
            IdlingRegistry.getInstance().register(idlingResource);
            //activityTestRule.launchActivity(new Intent());
            onView(withId(R.id.PatientMedicineDetailPage)).check(matches(isDisplayed()));
            //check button visible
            onView(withId(R.id.DeleteMedicine)).check(matches(isDisplayed()));
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }
    @Test
    public void DeleteCancle () {
        onView(withId(R.id.nameET)).check(matches(isDisplayed()));
        onView(withId(R.id.MFromDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTillDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTime)).check(matches(isDisplayed()));
        onView(withId(R.id.MDose)).check(matches(isDisplayed()));
        //check when click button
        onView(withId(R.id.DeleteMedicine)).perform(click());
        //check the dialog appear
        onView(withText(R.string.DeleteMessageMed))
                .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()));
        //click the cancel button
        onView(withText(R.string.DeleteCancleMed)).perform(click());
        //nothing happened
        assertFalse(activityTestRule.getActivity().isFinishing());
        onView(withId(R.id.PatientMedicineDetailPage)).check(matches(isDisplayed()));
    }

    @Test
    public void DeleteOK () {
        onView(withId(R.id.nameET)).check(matches(isDisplayed()));
        onView(withId(R.id.MFromDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTillDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTime)).check(matches(isDisplayed()));
        onView(withId(R.id.MDose)).check(matches(isDisplayed()));
        //check when click button
        onView(withId(R.id.DeleteMedicine)).perform(click());
        //check the dialog appear
        onView(withText(R.string.DeleteMessageMed))
                .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()));
        //click the delete button
        onView(withText(R.string.DeleteOKMed)).perform(click());
        // check toast visibility
        onView(withText(R.string.MedDeletedSuccess))
                .inRoot(new ToastMatcher())
                .check(matches(withText(R.string.MedDeletedSuccess)));
        //check activity finished
        assertTrue(activityTestRule.getActivity().isFinishing());
    }

    //@Test
    public void DeleteOKFailer () {
        onView(withId(R.id.nameET)).check(matches(isDisplayed()));
        onView(withId(R.id.MFromDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTillDays)).check(matches(isDisplayed()));
        onView(withId(R.id.MTime)).check(matches(isDisplayed()));
        onView(withId(R.id.MDose)).check(matches(isDisplayed()));
        //check when click button
        onView(withId(R.id.DeleteMedicine)).perform(click());
        //check the dialog appear
        onView(withText(R.string.DeleteMessageMed))
                .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()));
        //add timer to disconnect from intrnet connection
        IdlingPolicies.setMasterPolicyTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5000);
        try {
            IdlingRegistry.getInstance().register(idlingResource);
            //click the delete button
            onView(withText(R.string.DeleteOKMed)).perform(click());
            // check toast visibility
            onView(withText(R.string.MedDeletedFialed))
                    .inRoot(new ToastMatcher())
                        .check(matches(withText(R.string.MedDeletedFialed)));
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    @After
    public void tearDown() throws Exception {
        //medicineDetailPage = null;
    }

}