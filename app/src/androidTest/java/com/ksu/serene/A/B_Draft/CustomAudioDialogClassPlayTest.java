package com.ksu.serene.A.B_Draft;

import com.ksu.serene.ElapsedTimeIdlingResource;
import com.ksu.serene.MainActivity;
import com.ksu.serene.R;
import com.ksu.serene.controller.main.drafts.draftsFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.ksu.serene.TestUtils.withRecyclerView;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class CustomAudioDialogClassPlayTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        activityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //set fragment
                Fragment draftFragment = new draftsFragment();
                FragmentTransaction fragmentTransaction = activityTestRule.getActivity().getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.replace(R.id.Home, CalendarF);
                fragmentTransaction.add(R.id.MainActivity, draftFragment);
                fragmentTransaction.commit();

            }
        });
        //add tiemr
        //Mack sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(6000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(6000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(6000);
        try {
            IdlingRegistry.getInstance().register(idlingResource);
            //check the activity is visible
            onView(withId(R.id.allDraft)).check(matches(isDisplayed()));
            onView(withRecyclerView(R.id.Recyclerview_All_DraftVoice).atPosition(0)).perform(click());
            //add tiemr
            //Mack sure Espresso does not time out
            IdlingPolicies.setMasterPolicyTimeout(5000 * 2, TimeUnit.MILLISECONDS);
            IdlingPolicies.setIdlingResourceTimeout(5000 * 2, TimeUnit.MILLISECONDS);
            //Now we waite
            IdlingResource idlingResource1 = new ElapsedTimeIdlingResource(5000);
            try {
                IdlingRegistry.getInstance().register(idlingResource1);
                //activity
                onView(withId(R.id.your_dialog_root_element)).check(matches(isDisplayed()));
                //title
                onView(withId(R.id.title)).check(matches(isDisplayed()));
                //currentTime
                onView(withId(R.id.currentTime)).check(matches(isDisplayed()));
                //remaining Time
                onView(withId(R.id.remainingTime)).check(matches(isDisplayed()));
                //pause
                onView(withId(R.id.pause)).check(matches(isDisplayed()));
                //back button
                onView(withId(R.id.backward)).check(matches(isDisplayed()));
                //forward button
                onView(withId(R.id.forward)).check(matches(isDisplayed()));
                //speed
                onView(withId(R.id.speed)).check(matches(isDisplayed()));
                //delete button
                onView(withId(R.id.delete)).check(matches(isDisplayed()));
                //cancel button
                onView(withId(R.id.cancel)).check(matches(isDisplayed()));
            }
            //clean upp
            finally {
                IdlingRegistry.getInstance().unregister(idlingResource1);
            }
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    @Test
    public void PlayAudioLess10 () {
        //check the remaining time != 0
        onView(withId(R.id.remainingTime)).check(matches(not(withText("0:00"))));
        //check the voice time == 0
        onView(withId(R.id.currentTime)).check(matches(withText("0:00")));
        //click on the play button
        onView(withId(R.id.pause)).perform(click());
        //add tiemr
        //Mack sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(5000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource1 = new ElapsedTimeIdlingResource(5000);
        try {
            IdlingRegistry.getInstance().register(idlingResource1);
            //check the voice time != 0
            onView(withId(R.id.currentTime)).check(matches(not(withText("0:00"))));
            //click stop
            onView(withId(R.id.pause)).perform(click());
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource1);
        }
    }

    @Test
    public void PlayAudioMore10 () {
        //click on the play button
        onView(withId(R.id.pause)).perform(click());
        //add tiemr
        //Mack sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(11000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(11000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource1 = new ElapsedTimeIdlingResource(11000);
        try {
            IdlingRegistry.getInstance().register(idlingResource1);
            //click stop
            onView(withId(R.id.pause)).perform(click());
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource1);
        }
    }

    @Test
    public void PlayAudioBack () {
        //click on the play button
        onView(withId(R.id.pause)).perform(click());
            //TODO check the text time changed
            //click the backward button
            onView(withId(R.id.backward)).perform(click());
            //TODO check the text time changed
    }

   @Test
    public void PlayAudioFor () {
        //click on the play button
        onView(withId(R.id.pause)).perform(click());
        //TODO check the text time changed
        //click the forward button
        onView(withId(R.id.forward)).perform(click());
        //TODO check the text time changed
    }

    @Test
    public void PlayAudioSpeed () {
        //click on the play button
        onView(withId(R.id.pause)).perform(click());
        //click the speed button X2
        onView(withId(R.id.speed)).perform(click());
        //Mack sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(3000 * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(3000 * 2, TimeUnit.MILLISECONDS);
        //Now we waite
        IdlingResource idlingResource1 = new ElapsedTimeIdlingResource(3000);
        try {
            IdlingRegistry.getInstance().register(idlingResource1);
            //check the voice time != 0
            onView(withId(R.id.currentTime)).check(matches(not(withText("0:00"))));
            //X1
            onView(withId(R.id.speed)).perform(click());
        }
        //clean upp
        finally {
            IdlingRegistry.getInstance().unregister(idlingResource1);
        }
    }

    //Test When cancle button click
    @Test
    public void CancleButton () {
        //cancel button click
        onView(withId(R.id.cancel)).perform(click());
        //check the activity is shown
        onView(withId(R.id.allDraft)).check(matches(isDisplayed()));
    }



}