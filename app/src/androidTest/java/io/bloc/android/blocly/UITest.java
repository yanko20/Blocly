package io.bloc.android.blocly;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;

import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.ui.activity.BloclyActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * Created by ykomizor on 9/8/2016.
 */


    @RunWith(AndroidJUnit4.class)
    @LargeTest
    public class UITest {

    private static String TAG = "yanstag";
    private String mStringToBetyped;
    private DatabaseOpenHelper databaseOpenHelper;

    @Rule
    public ActivityTestRule<BloclyActivity> mActivityRule = new ActivityTestRule<>(
            BloclyActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        //mStringToBetyped = "Espresso";
    }

    @Test
    public void selectFavoriteItem() {
        onView(withId(R.id.rv_fragment_rss_list)).perform(swipeDown());
        clickOnFirstFavorite();
        databaseOpenHelper = new DatabaseOpenHelper(mActivityRule.getActivity());
        SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();
        String testGuid = BloclyApplication.getSharedDataSource().getTestGuid();
        String isFavored = getFavoriteValue(writableDatabase, testGuid);
        assertEquals("1", isFavored);

        clickOnFirstFavorite();
        isFavored = getFavoriteValue(writableDatabase, testGuid);
        assertEquals("0", isFavored);
    }

    private String getFavoriteValue(SQLiteDatabase writableDatabase, String testGuid) {
        Log.v(TAG, "TEST guid: " + testGuid);
        String query = "SELECT * FROM rss_items WHERE guid='" + testGuid + "'";
        Cursor c = writableDatabase.rawQuery(query, new String[]{});
        c.moveToNext();
        int index = c.getColumnIndex("is_favorite");
        Log.v(TAG, "TEST is_favorite = " + c.getString(index));
        String isFavored = c.getString(index);
        c.close();
        return isFavored;
    }

    private void clickOnFirstFavorite() {
        onView(allOf(withId(R.id.cb_rss_item_favorite_star), FirstViewMatcher.firstView())).perform(click());
    }

}

class FirstViewMatcher extends BaseMatcher<View> {


    public static boolean matchedBefore = false;

    public FirstViewMatcher() {
        matchedBefore = false;
    }

    @Override
    public boolean matches(Object o) {
        if (matchedBefore) {
            return false;
        } else {
            matchedBefore = true;
            return true;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(" is the first view that comes along ");
    }

    @Factory
    public static <T> Matcher<View> firstView() {
        return new FirstViewMatcher();
    }
}
