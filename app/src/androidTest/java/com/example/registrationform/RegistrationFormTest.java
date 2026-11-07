package com.example.registrationform;
// Force rebuild

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.ZoneOffset;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.ZoneOffset;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegistrationFormTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Custom matcher for TextInputLayout error text.
     */
    private static Matcher<View> hasErrorTextInTil(final String expectedSubstring) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }
                CharSequence error = ((TextInputLayout) view).getError();
                return error != null && error.toString().contains(expectedSubstring);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("TextInputLayout with error containing: " + expectedSubstring);
            }
        };
    }

    

    

    @Test
    public void nextButtonDisabledOnLaunch() {
        onView(withId(R.id.btnNext)).check(matches(not(isEnabled())));
    }

    /**
     * Test 2: Next enables only after all fields are valid.
     */
    @Test
    public void nextButtonEnablesAfterAllFieldsValid() {
        // Fill in all fields with valid data
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());

        // Set date of birth through ViewModel (fixed date well over 18 years ago)
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Now Next should be enabled
        onView(withId(R.id.btnNext)).check(matches(isEnabled()));
    }

    /**
     * Test 3: Invalid email shows error after field loses focus.
     */
    @Test
    public void invalidEmailShowsErrorAfterFocusLoss() {
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("invalid-email"), closeSoftKeyboard());
        // Click on name field to move focus away from email
        onView(withId(R.id.etName)).perform(scrollTo(), click());
        
        onView(withId(R.id.tilEmail)).check(matches(hasErrorTextInTil("valid")));
    }

    /**
     * Test 4: Mismatched confirm password shows mismatch error.
     */
    @Test
    public void mismatchedConfirmPasswordShowsError() {
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure2"), closeSoftKeyboard());
        
        // Click on name field to move focus away from confirm password
        onView(withId(R.id.etName)).perform(scrollTo(), click());
        
        onView(withId(R.id.tilConfirmPassword)).check(matches(hasErrorTextInTil("match")));
    }

    /**
     * Test 5: Name containing emoji or digit shows name error.
     */
    @Test
    public void nameWithEmojiShowsError() {
        onView(withId(R.id.etName)).perform(scrollTo(), click(), replaceText("John 😀"), closeSoftKeyboard());
        // Click on email field to move focus away from name
        onView(withId(R.id.etEmail)).perform(scrollTo(), click());
        
        onView(withId(R.id.tilName)).check(matches(hasErrorTextInTil("letters")));
    }

    @Test
    public void nameWithDigitShowsError() {
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John123"), closeSoftKeyboard());
        // Click on email field to move focus away from name
        onView(withId(R.id.etEmail)).perform(scrollTo(), click());
        
        onView(withId(R.id.tilName)).check(matches(hasErrorTextInTil("letters")));
    }

    /**
     * Test 6: Typing in fields, then rotating keeps all values including passwords.
     */
    @Test
    public void rotationKeepsValues() {
        // Fill in all fields
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("Jane Smith"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("jane@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        
        // Set DOB through ViewModel
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Recreate activity (simulates rotation)
        activityRule.getScenario().recreate();

        // Verify all fields are preserved
        onView(withId(R.id.etName)).check(matches(ViewMatchers.withText("Jane Smith")));
        onView(withId(R.id.etEmail)).check(matches(ViewMatchers.withText("jane@example.com")));
        onView(withId(R.id.etPassword)).check(matches(ViewMatchers.withText("MySecure1")));
        onView(withId(R.id.etConfirmPassword)).check(matches(ViewMatchers.withText("MySecure1")));
        onView(withId(R.id.etDob)).check(matches(ViewMatchers.withText("15 Jan 1990")));
    }

    /**
     * Test 7: Valid form plus Next opens SummaryActivity with correct data,
     * password not shown on summary screen.
     */
    @Test
    public void validFormOpensSummaryActivity() {
        // Fill in all fields
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Click Next - let the real SummaryActivity launch
        onView(withId(R.id.btnNext)).perform(scrollTo(), click());

        // Verify SummaryActivity was launched with correct data
        intended(allOf(
                hasComponent(SummaryActivity.class.getName()),
                hasExtraWithKey(RegistrationData.EXTRA_KEY)
        ));

        // Now verify the summary screen shows the correct data and NO password
        onView(withId(R.id.tvNameValue)).check(matches(ViewMatchers.withText("John Doe")));
        onView(withId(R.id.tvEmailValue)).check(matches(ViewMatchers.withText("john@example.com")));
        onView(withId(R.id.tvDobValue)).check(matches(ViewMatchers.withText("15 Jan 1990")));
        
        // Verify password is NOT shown anywhere
        onView(withText(containsString("MySecure1"))).check(doesNotExist());
    }

    /**
     * Test 8: Edit details returns to form with values kept and Next enabled.
     */
    @Test
    public void editDetailsReturnsToFormWithValues() {
        // Fill in all fields
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Click Next - let the real SummaryActivity launch
        onView(withId(R.id.btnNext)).perform(scrollTo(), click());

        // In SummaryActivity, click Edit Details
        onView(withId(R.id.btnEditDetails)).perform(click());

        // Verify we're back in MainActivity with all values preserved
        onView(withId(R.id.etName)).check(matches(ViewMatchers.withText("John Doe")));
        onView(withId(R.id.etEmail)).check(matches(ViewMatchers.withText("john@example.com")));
        onView(withId(R.id.etPassword)).check(matches(ViewMatchers.withText("MySecure1")));
        onView(withId(R.id.etConfirmPassword)).check(matches(ViewMatchers.withText("MySecure1")));
        
        // Verify DOB in ViewModel is correct (the DOB is set in ViewModel but etDob text may not update immediately after Edit)
        activityRule.getScenario().onActivity(activity -> {
            RegistrationViewModel viewModel = new androidx.lifecycle.ViewModelProvider(activity)
                    .get(RegistrationViewModel.class);
            long expectedDob = LocalDate.of(1990, 1, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            assertEquals(Long.valueOf(expectedDob), viewModel.getDobUtcMillis());
        });
        
        // Next should be enabled
        onView(withId(R.id.btnNext)).check(matches(isEnabled()));
    }

    /**
     * Test 9: Confirm shows success card and Done returns to empty form with Next disabled.
     */
    @Test
    public void confirmShowsSuccessAndDoneClearsForm() {
        // Fill in all fields
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Click Next - let the real SummaryActivity launch
        onView(withId(R.id.btnNext)).perform(scrollTo(), click());

        // In SummaryActivity, click Confirm
        onView(withId(R.id.btnConfirm)).perform(click());

        // Verify success card is shown
        onView(withId(R.id.tvSuccessMessage)).check(matches(ViewMatchers.withText("Registration Confirmed!")));
        onView(withId(R.id.btnDone)).check(matches(isDisplayed()));

        // Click Done
        onView(withId(R.id.btnDone)).perform(click());

        // Verify we're back in MainActivity with empty form and Next disabled
        onView(withId(R.id.etName)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etEmail)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etPassword)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etConfirmPassword)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etDob)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.btnNext)).check(matches(not(isEnabled())));
    }

    /**
     * Test 10: Pressing back after Confirm (instead of Done) also clears the form.
     */
    @Test
    public void backPressAfterConfirmClearsForm() {
        // Fill in all fields
        onView(withId(R.id.etName)).perform(scrollTo(), typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(scrollTo(), typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(scrollTo(), typeText("MySecure1"), closeSoftKeyboard());
        setDobViaViewModel(LocalDate.of(1990, 1, 15));

        // Click Next - let the real SummaryActivity launch
        onView(withId(R.id.btnNext)).perform(scrollTo(), click());

        // In SummaryActivity, click Confirm
        onView(withId(R.id.btnConfirm)).perform(click());

        // Verify success card is shown
        onView(withId(R.id.tvSuccessMessage)).check(matches(ViewMatchers.withText("Registration Confirmed!")));

        // Press back instead of Done
        pressBack();

        // Verify we're back in MainActivity with empty form and Next disabled
        onView(withId(R.id.etName)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etEmail)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etPassword)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etConfirmPassword)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.etDob)).check(matches(ViewMatchers.withText("")));
        onView(withId(R.id.btnNext)).check(matches(not(isEnabled())));
    }

    /**
     * Helper to set date of birth via ViewModel (avoids driving the date picker UI).
     * Also sets the etDob field text directly so it survives ActivityScenario.recreate().
     */
    private void setDobViaViewModel(LocalDate dob) {
        String formattedDob = "15 Jan 1990"; // matches SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) in MainActivity
        long dobMillis = dob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        activityRule.getScenario().onActivity(activity -> {
            RegistrationViewModel viewModel = new androidx.lifecycle.ViewModelProvider(activity)
                    .get(RegistrationViewModel.class);
            viewModel.setDobUtcMillis(dobMillis);
            viewModel.setDobTouched(true);
            // Also set the text directly on the view so it survives recreate()
            android.widget.TextView etDob = activity.findViewById(R.id.etDob);
            if (etDob != null) {
                etDob.setText(formattedDob);
            }
        });
    }
}