/**
 *
 */
package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Track;
import org.fosdem.util.TrackAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class TrackListActivity extends SherlockListActivity implements OnNavigationListener {

	public static final String LOG_TAG=TrackListActivity.class.getName();

	public static final String DAY_INDEX = "dayIndex";

	private ArrayList<Track> tracks = null;
	private int dayIndex = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_list);

		// what day should we show? fetch from the parameters or saved instance
		dayIndex = savedInstanceState != null ? savedInstanceState.getInt(DAY_INDEX) : 0;

		tracks = getTracks();
        setListAdapter(new TrackAdapter(this, R.layout.track_list_item, R.id.title, tracks));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        //setTitle("Tracks for Day " + dayIndex);
        actionBar.setDisplayShowTitleEnabled(false);

        ArrayAdapter<CharSequence> mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.tracklist_spinneractions,
    		R.layout.sherlock_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);
        actionBar.setSelectedNavigationItem(dayIndex - 1);

        actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            NavUtils.navigateUpFromSameTask(this);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Track track = (Track) getListView().getItemAtPosition(position);

        Log.d(LOG_TAG, "Track selected: " + track.getName());

        Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(EventListActivity.TRACK_NAME, track.getName());
		i.putExtra(EventListActivity.DAY_INDEX, dayIndex);
		startActivity(i);
    }

	private ArrayList<Track> getTracks() {
		if (dayIndex == 0) {
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				dayIndex = extras.getInt(DAY_INDEX);
			if (dayIndex == 0 ) {
				Log.e(LOG_TAG, "You are loading this class with no valid day parameter");
				return null;
			}
		}

		// Load track list with specified day index from db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			String[] trackNames = db.getTracksByDayIndex(dayIndex);
			ArrayList<Track> tracks = new ArrayList<Track>();
			for (String trackName : trackNames) {
				tracks.add(new Track(trackName));
			}
			return tracks;
		} finally {
			db.close();
		}
	}

	public boolean onNavigationItemSelected(int position, long itemId) {
		// String[] strings = getResources().getStringArray(R.array.tracklist_spinneractions);
		// strings[position]
		if ((position + 1) != dayIndex) {
			dayIndex = position + 1;
			Log.d(LOG_TAG, "showTracksForDay(" + dayIndex + ");");
			tracks = getTracks();
			setListAdapter(new TrackAdapter(this, R.layout.track_list_item, R.id.title, tracks));
		}

	    return true;
	}
}
