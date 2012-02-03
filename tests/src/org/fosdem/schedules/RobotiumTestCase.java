package org.fosdem.schedules;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

public abstract class RobotiumTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

    protected T mActivity;

    protected Solo mSolo;

    public RobotiumTestCase(Class<T> activityClass) {
        super("org.fosdem.schedules", activityClass);
    }

    public void setUp() throws Exception {
        mActivity = getActivity();
        mSolo = new Solo(getInstrumentation(), mActivity);
    }

    @Override
    public void tearDown() throws Exception {
        mSolo.finishOpenedActivities();
    }

    protected String getString(int resId, Object... formatArgs) {
        return mActivity.getString(resId, formatArgs);
    }

}