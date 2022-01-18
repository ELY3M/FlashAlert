package fr.jnda.android.flashalert

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import java.util.*


@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mGrantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.RECEIVE_SMS",
                    "android.permission.READ_CONTACTS",
                    "android.permission.READ_CALL_LOG",
                    "android.permission.RECEIVE_MMS",
                    "android.permission.READ_PHONE_STATE")


    @Rule
    @JvmField
    var myActivityRule = ActivityTestRule(MainActivity::class.java)

    private var context: Context? = null

    @Before
    fun setUp() {
       context = InstrumentationRegistry.getInstrumentation().context
    }
    companion object{
        @ClassRule
        @JvmField
        val localeTestRule = ForceLocaleRule(Locale.ENGLISH)
    }

    @Test
    fun mainActivityTest() {
        Screengrab.screenshot("Start")
        val recyclerView = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val recyclerView2 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        Screengrab.screenshot("Start1")
        val recyclerView3 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView3.perform(actionOnItemAtPosition<ViewHolder>(3, click()))

        val recyclerView4 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView4.perform(actionOnItemAtPosition<ViewHolder>(4, click()))
        Screengrab.screenshot("Start2")
        val recyclerView5 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView5.perform(actionOnItemAtPosition<ViewHolder>(7, click()))

        val recyclerView6 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView6.perform(actionOnItemAtPosition<ViewHolder>(8, click()))
        Screengrab.screenshot("Start3")
        val recyclerView7 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView7.perform(actionOnItemAtPosition<ViewHolder>(10, click()))
        Screengrab.screenshot("Start4")
        val recyclerView8 = onView(
                allOf(withId(R.id.recycler_view),
                        childAtPosition(
                                withId(android.R.id.list_container),
                                0)))
        recyclerView8.perform(actionOnItemAtPosition<ViewHolder>(13, click()))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
