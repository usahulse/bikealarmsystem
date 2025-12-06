package com.example.bicyclealarmsystem;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityEspressoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testArmDisarmButton() {
        // Initially, the alarm should be disarmed
        onView(withId(R.id.armButton)).check(matches(withText("Arm")));

        // Click the button to arm the alarm
        onView(withId(R.id.armButton)).perform(click());

        // After clicking, the button text should change to "Disarm"
        onView(withId(R.id.armButton)).check(matches(withText("Disarm")));

        // Click the button again to disarm the alarm
        onView(withId(R.id.armButton)).perform(click());

        // After clicking again, the button text should change back to "Arm"
        onView(withId(R.id.armButton)).check(matches(withText("Arm")));
    }
}
