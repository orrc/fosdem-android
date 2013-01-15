package org.fosdem.schedules;

import java.util.ArrayList;
import java.util.Date;

import org.fosdem.R;
import org.fosdem.broadcast.FavoritesBroadcast;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Track;
import org.fosdem.util.EventAdapter;
import org.fosdem.util.TrackAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class EventListActivity extends SherlockActivity  implements OnScrollListener,
	AdapterView.OnItemClickListener, OnHeaderClickListener, OnNavigationListener {

	public static final String LOG_TAG = EventListActivity.class.getName();

	public static final String DAY_INDEX = "dayIndex";
	public static final String TRACK_NAME = "trackName";
	public static final String QUERY = "query";
	public static final String FAVORITES = "favorites";

	private ArrayList<Event> events = null;
	private String trackName = null;
	private int dayIndex = 0;
	private String query = null;
	private Boolean favorites = null;
	private EventAdapter eventAdapter = null;

	private static final String KEY_LIST_POSITION = "KEY_LIST_POSITION";
	private int firstVisible;
	private StickyListHeadersListView stickyList;
	private TrackAdapter trackAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// prepare sticky header list
		setContentView(R.layout.event_list);
		stickyList = (StickyListHeadersListView) findViewById(R.id.list);
		stickyList.setOnScrollListener(this);
		stickyList.setOnItemClickListener(this);
		stickyList.setOnHeaderClickListener(this);

		if (savedInstanceState != null) {
			firstVisible = savedInstanceState.getInt(KEY_LIST_POSITION);
		}

		trackName = savedInstanceState != null ? savedInstanceState.getString(TRACK_NAME) : null;

		// what room should we show? fetch from the parameters
		Bundle extras = getIntent().getExtras();
		if (trackName == null && query == null && extras != null) {
			trackName = extras.getString(TRACK_NAME);
			dayIndex = extras.getInt(DAY_INDEX);
			favorites = extras.getBoolean(FAVORITES);
			query = extras.getString(QUERY);
		}
		if (trackName != null && dayIndex != 0)
			setTitle(trackName);
		if (query != null)
			setTitle("Search for: " + query);
		if (favorites != null && favorites)
			setTitle("Favorites");

		registerReceiver(favoritesChangedReceiver, new IntentFilter(
				FavoritesBroadcast.ACTION_FAVORITES_UPDATE));

		events = getEventList(favorites);
		eventAdapter = new EventAdapter(this, R.layout.event_list_item, events);

		stickyList.setAdapter(eventAdapter);
		stickyList.setSelection(firstVisible);

		ActionBar actionBar = getSupportActionBar();

		// show action bar list navigation in track mode
		if (trackName != null && dayIndex != 0) {
	        ArrayList<Track> tracksForDayIndex = getTracks(dayIndex);
	        trackAdapter = new TrackAdapter(this, R.layout.sherlock_spinner_item,
	        	android.R.id.text1, tracksForDayIndex);
	        trackAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

	        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	        actionBar.setDisplayShowTitleEnabled(false);
	        actionBar.setListNavigationCallbacks(trackAdapter, this);
	        actionBar.setSelectedNavigationItem(trackAdapter.getPositionOfTrack(trackName));
		}

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	onBackPressed();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public boolean onNavigationItemSelected(int position, long itemId) {
	    // spinner navigation between tracks in track mode
		Track track = trackAdapter.getItem(position);
		trackName = track.getName();
		setTitle(trackName);
		events = getEventList(favorites);
		eventAdapter.clear();
		//eventAdapter.addAll(events); // note: addAll is only available starting with API 11
		for (Event event : events) {
			eventAdapter.add(event);
		}
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_LIST_POSITION, firstVisible);
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		this.firstVisible = firstVisibleItem;
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//super.onListItemClick(parent, view, position, id);
		Event event = (Event) stickyList.getItemAtPosition(position);

		Log.d(LOG_TAG, "Event selected: " + event.getId() + " - " + event.getTitle());

		Intent i = new Intent(this, DisplayEvent.class);
		i.putExtra(DisplayEvent.ID, event.getId());
		startActivity(i);
	}

	public void onHeaderClick(StickyListHeadersListView l, View header,
			int itemPosition, long headerId, boolean currentlySticky) {
		//Toast.makeText(this, "header "+headerId, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Gets the {@link Event} that was specified through the intent or null if
	 * no or wrongly specified event.
	 *
	 * @return The Event or null.
	 */
	private ArrayList<Event> getEventList(Boolean favoritesOnly) {

		if (query == null && trackName == null && (favoritesOnly == null || !favoritesOnly)) {
			Log.e(LOG_TAG, "You are loading this class with no valid room parameter");
			return null;
		}

		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();

			if (trackName != null) {
				return (ArrayList<Event>) db.getEventsByTrackNameAndDayIndex(trackName, dayIndex);
			} else if (query != null) {
				String[] queryArgs = new String[] { query };
				return (ArrayList<Event>) db.getEventsFilteredLike(null, null,
						queryArgs, queryArgs, queryArgs, queryArgs, queryArgs,
						null, queryArgs);
			} else if (favorites != null && favorites) {
				Log.e(LOG_TAG, "Getting favorites...");

				SharedPreferences prefs = getSharedPreferences(Main.PREFS, Context.MODE_PRIVATE);
				Date startDate=prefs.getBoolean(Preferences.PREF_UPCOMING, false) ? new Date() : null;

				return db.getFavoriteEvents(startDate);
			}

			return (ArrayList<Event>) db.getEventsByTrackNameAndDayIndex(trackName, dayIndex);
		} finally {
			db.close();
		}
	}

	private ArrayList<Track> getTracks(int dayIndex) {
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

	public static void doSearchWithIntent(Context context, final Intent queryIntent) {
		queryIntent.getStringExtra(SearchManager.QUERY);
		Intent i = new Intent(context, EventListActivity.class);
		i.putExtra(EventListActivity.QUERY, queryIntent.getStringExtra(SearchManager.QUERY));
		context.startActivity(i);
	}

	private BroadcastReceiver favoritesChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			eventAdapter.clear();
			events = getEventList(favorites);
			for (Event event : events) {
				eventAdapter.add(event);
			}
			if (events == null || events.size() == 0)
				EventListActivity.this.finish();
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(favoritesChangedReceiver);
	}
}
