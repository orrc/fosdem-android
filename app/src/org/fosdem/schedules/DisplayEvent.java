package org.fosdem.schedules;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Calendar;

import org.fosdem.R;
import org.fosdem.broadcast.FavoritesBroadcast;
import org.fosdem.db.DBAdapter;
import org.fosdem.pojo.Event;
import org.fosdem.util.FileUtil;
import org.fosdem.util.StringUtil;
import org.fosdem.util.UIUtil;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayEvent extends SherlockActivity {

	/** Display event action string */
	public final static String ACTION_DISPLAY_EVENT = "org.fosdem.schedules.DISPLAY_EVENT";

	/** Id extras parameter name */
	public final static String ID = "org.fosdem.Id";
	public final static int SHARE_ID = 1;

	private Drawable roomImageDrawable;

	protected static final int MAPREADY = 1120;

	private Event event;
	private boolean isFavorite;

	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event);

		mContext = getBaseContext();

		forceActionbarOverflowMenu();

		// Get the event from the intent
		event = getEvent();

		DBAdapter adapter = new DBAdapter(getBaseContext());
		adapter.open();
		isFavorite = adapter.isFavorite(event);
		Log.v(getClass().getName(),isFavorite?"Is a favorite":"Isn't a favorite");
		adapter.close();

		// No event? stop this activity
		if (event == null) {
			finish();
			return;
		}

		// populate the UI_event
		showEvent(event);

		Intent intent = new Intent(FavoritesBroadcast.ACTION_FAVORITES_UPDATE);
		intent.putExtra(FavoritesBroadcast.EXTRA_TYPE,
				FavoritesBroadcast.EXTRA_TYPE_REMOVE_NOTIFICATION);
		intent.putExtra(FavoritesBroadcast.EXTRA_ID, ((long) (event.getId())));
		sendBroadcast(intent);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg == null)
				return;
			if (msg.what == MAPREADY) {
				ImageView iv = (ImageView) findViewById(R.id.room_image);
				iv.setImageDrawable(roomImageDrawable);
				// tv.setText("Fetched "+counter+" events.");
			}
		}
	};

	/**
	 * Gets the {@link Event} that was specified through the intent or null if
	 * no or wrongly specified event.
	 *
	 * @return The Event or null.
	 */
	private Event getEvent() {

		// Get the extras
		final Bundle extras = getIntent().getExtras();
		if (extras == null)
			return null;

		// Get id from extras
		if (!(extras.get(ID) instanceof Integer))
			return null;
		final int id = (Integer) extras.get(ID);

		// Load event with specified id from the db
		final DBAdapter db = new DBAdapter(this);
		try {
			db.open();
			return db.getEventById(id);
		} finally {
			db.close();
		}
	}

	/**
	 * Helper method to set the text of the {@link TextView} identified by
	 * specified id.
	 *
	 * @param id
	 *            Id of the view (must be a TextView)
	 * @param value
	 *            Text to set.
	 */
	private void setTextViewText(int id, String value) {
		final TextView tv = (TextView) findViewById(id);

		if (value == null) {
			tv.setText("");
			return;
		}

		tv.setText(Html.fromHtml(value));
	}

	public void prefetchImageViewImageAndShowIt(final String url, final String filename) {
		Thread t = new Thread() {
			public void run() {
				try {
					roomImageDrawable = FileUtil.fetchCachedDrawable(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (roomImageDrawable == null) {
					try {
						// there are fallback room drawables, check if we have a suitable one
						int resId = getResources().getIdentifier("room_"+filename, "drawable", getPackageName());
						roomImageDrawable = getResources().getDrawable(resId);
					} catch(NotFoundException $e) {}
				}

				Message msg = Message.obtain();
				msg.what = MAPREADY;
				handler.sendMessage(msg);
			}
		};
		t.start();

	}

	/**
	 * Loads the contents of the event with into the gui.
	 *
	 * @param event
	 *            The event to show
	 */
	private void showEvent(Event event) {
		String eventAbstract = StringUtil.niceify(event
				.getAbstract_description());
		if (eventAbstract.length() == 0)
			eventAbstract = "No abstract available.";
		String eventDescription = StringUtil.niceify(event.getDescription());
		if (eventDescription.length() == 0)
			eventDescription = "No lecture description avablable.";

		setTextViewText(R.id.event_title, event.getTitle());
		setTextViewText(R.id.event_track, event.getTrack());
		setTextViewText(R.id.event_room, event.getRoom());
		setTextViewText(R.id.event_time, StringUtil.datesToString(event
				.getStart(), event.getDuration()));
		setTextViewText(R.id.event_speaker, StringUtil.personsToString(event
				.getPersons()));
		setTextViewText(R.id.event_abstract, eventAbstract);
		setTextViewText(R.id.event_description, eventDescription);

		// setImageViewImage(R.id.room_image,
		// StringUtil.roomNameToURL(event.getRoom()));
		prefetchImageViewImageAndShowIt(StringUtil.roomNameToURL(event.getRoom()),
				StringUtil.roomNameToFilename(event.getRoom()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inf = getSupportMenuInflater();
		inf.inflate(R.menu.event_menu, menu);

		// TODO: move favorite functionality to an ActionProvider!
		MenuItem item = menu.findItem(R.id.MENU_TOGGLEFAVORITES);
		setFavoriteActionImageResource(item);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MENU_SHARE:
			share();
			return true;
		case R.id.MENU_TOGGLEFAVORITES:
			toggleFavoriteStatus(item);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void share() {
		final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		String extra = "I'm attending '" + event.getTitle() + "' (Day "
				+ (event.getDayindex()) + " at "
				+ String.format("%02d", event.getStart().getHours()) + ":"
				+ String.format("%02d", event.getStart().getMinutes()) + " @ " + event.getRoom()
				+ ") #fosdem";
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime >= event.getStart().getTime()
				&& currentTime <= (event.getStart().getTime() + ((event
						.getDuration() + 10) * 60 * 1000)))
			extra = "I'm currently attending '" + event.getTitle() + "' ("
					+ event.getRoom() + ") #fosdem";
		intent.putExtra(Intent.EXTRA_TEXT, extra);
		startActivity(Intent.createChooser(intent, getString(R.string.share)));
	}

	private void forceActionbarOverflowMenu() {
		// Force overflow control for action bar even if the device has got a physical menu button.
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setFavoriteActionImageResource(MenuItem item) {
		if (isFavorite) {
			item.setIcon(R.drawable.rating_important);
		} else {
			item.setIcon(R.drawable.rating_not_important);
		}
	}

	private void toggleFavoriteStatus(MenuItem menuItem) {
		DBAdapter db = new DBAdapter(getBaseContext());
		db.open();
		if (isFavorite) {
			// Unmark
			db.deleteBookmark(event.getId());
			UIUtil.showToast(mContext, mContext.getString(R.string.favorites_event_removed));
		} else {
			// Mark
			db.addBookmark(event);
			UIUtil.showToast(mContext, mContext.getString(R.string.favorites_event_added));
		}
		db.close();
		isFavorite = !isFavorite;

		setFavoriteActionImageResource(menuItem);

	}
}
