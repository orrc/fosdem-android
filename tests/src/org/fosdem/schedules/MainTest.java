package org.fosdem.schedules;

import android.view.KeyEvent;

public class MainTest extends RobotiumTestCase<Main> {

    public MainTest() {
        super(Main.class);
    }

    /** Asserts that pressing the Search hardware key works. */
    public void testSearchHardKey() {
        mSolo.sendKey(KeyEvent.KEYCODE_SEARCH);
        assertSearchWorks();
    }

    /** Assert that pressing the Search button works. */
    public void testSearchButton() {
        mSolo.clickOnButton("Search");
        assertSearchWorks();
    }

    /** Asserts that simply typing starts a search. */
    public void testSearchTypeahead() {
        mSolo.sendKey(KeyEvent.KEYCODE_SPACE);
        assertSearchWorks();
    }

    /** Enters a search term and asserts results are shown. */
    private void assertSearchWorks() {
        mSolo.enterText(0, "jenkins");
        assertTrue(mSolo.waitForText("R Tyler Croy"));
    }

}