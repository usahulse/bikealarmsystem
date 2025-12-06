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

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void armButton_changesTextOnToggle() {
        // Initially, the button should say "Arm"
        onView(withId(R.id.armButton)).check(matches(withText("Arm")));

        // After clicking, the button should say "Disarm"
        onView(withId(R.id.armButton)).perform(click());
        onView(withId(R.id.armButton)).check(matches(withText("Disarm")));

        // After clicking again, the button should say "Arm"
        onView(withId(R.id.armButton)).perform(click());
        onView(withId(R.id.armButton)).check(matches(withText("Arm")));
    }
}
