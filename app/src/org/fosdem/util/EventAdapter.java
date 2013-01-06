/**
 *
 */
package org.fosdem.util;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Event;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class EventAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter {

	public static final String LOG_TAG= EventAdapter.class.getName();
	private ArrayList<Event> items;
	private int listItemViewResourceId;
	private LayoutInflater inflater;
	private Context context;

	public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.listItemViewResourceId = textViewResourceId;
		this.items = items;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = inflater.inflate(listItemViewResourceId, null);
		}

		Event event = items.get(position);
		if (event != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			TextView speaker = (TextView) v.findViewById(R.id.speakers);
			TextView room = (TextView) v.findViewById(R.id.room);
			TextView time = (TextView) v.findViewById(R.id.time);
			ImageView favorite = (ImageView) v.findViewById(R.id.favorite);

			title.setText(event.getTitle());
			speaker.setText(StringUtil.personsToString(event.getPersons()));
			room.setText(event.getRoom());
			time.setText(StringUtil.datesToString(event.getStart(), event.getDuration()));

			if (event.isFavorite()) {
				favorite.setImageDrawable(v.getResources().getDrawable(R.drawable.rating_important));
			} else {
				favorite.setImageDrawable(null);
			}
		}

		return v;
	}

	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = inflater.inflate(R.layout.event_list_section, null);
		}

		Event event = items.get(position);
		TextView title = (TextView) v.findViewById(R.id.sectiontitle);

		String headerText = "";
		switch (event.getDayindex()) {
		case 1:
			headerText = context.getString(R.string.eventlist_header_saturday);
			break;
		case 2:
			headerText = context.getString(R.string.eventlist_header_sunday);
			break;
		default:
			headerText = "Day (unknown)";
			break;
		}

		title.setText(headerText);

		return v;
	}

	public long getHeaderId(int position) {
		Event event = items.get(position);
		return event.getDayindex();
	}

	class HeaderViewHolder {
		TextView text;
	}

	class ViewHolder {
		TextView text;
	}

}
