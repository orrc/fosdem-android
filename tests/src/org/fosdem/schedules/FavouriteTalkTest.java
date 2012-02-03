package org.fosdem.schedules;

import android.view.KeyEvent;

public class FavouriteTalkTest extends RobotiumTestCase<Main> {

    public FavouriteTalkTest() {
        super(Main.class);
    }

    /**
     * Searches for a talk, marks it as a favourite
     * then ensures it appears in the favourites list.
     */
    public void testJenkinsIsMyFavourite() {
        final String talkAuthor = "R Tyler Croy";
        final String searchTerm = "jenkins";

        // Open the favourites and check there are none
        mSolo.clickOnButton("Favorites");
        assertFalse("Jenkins was already a favourite!",
                mSolo.searchText(talkAuthor));

        // Search for the Jenkins talk
        mSolo.sendKey(KeyEvent.KEYCODE_SEARCH);
        mSolo.enterText(0, searchTerm);
        assertTrue("Didn't find Tyler's talk!",
                mSolo.waitForText(talkAuthor));

        // Open it
        mSolo.clickOnText(talkAuthor);

        // We should be on the event details screen
        mSolo.assertCurrentActivity("Expected the event details...",
                DisplayEvent.class);

        // Click on the star
        mSolo.clickOnImage(0);
        assertTrue("Event should have been added to favourites!",
                mSolo.waitForText("Event added to favorites"));

        // Go way back to the home screen
        mSolo.goBackToActivity("Main");

        // Open the favourites
        mSolo.clickOnButton("Favorites");

        // Jenkins talk should now be there!
        assertTrue(mSolo.searchText(talkAuthor));
    }

}