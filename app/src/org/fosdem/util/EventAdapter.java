/**
 *
 */
package org.fosdem.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.fosdem.R;
import org.fosdem.pojo.Event;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class EventAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter, SectionIndexer {

	public static final String LOG_TAG= EventAdapter.class.getName();
	private ArrayList<Event> items;
	private int listItemViewResourceId;
	private LayoutInflater inflater;
	private Context context;
	private HashMap<Integer, Integer> sectionPositionMap;

	public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.listItemViewResourceId = textViewResourceId;
		this.items = items;
		this.inflater = LayoutInflater.from(context);
		this.sectionPositionMap = getSectionPositionMap(items);
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

	public Object[] getSections() {
		String sections[] = {
			"Sat",
			"Sun"
		};

		return sections;
	}

	public int getPositionForSection(int section) {
		Integer position = sectionPositionMap.get(Integer.valueOf(section));
		if (position == null) {
			return 1;
		}
		return position;
	}

	public int getSectionForPosition(int position) {
		Event event = items.get(position);

		switch (event.getDayindex()) {
		case 1:
			return 0;
		case 2:
			return 1;
		default:
			return 1;
		}
	}

	private HashMap<Integer, Integer> getSectionPositionMap(ArrayList<Event> items) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		int i = 0;
		int tmp = -1;
		int dayindexPrev = -1;
		for (Event event : items) {
			if (event.getDayindex() != dayindexPrev) {
				tmp++;
				map.put(tmp, i);
			}
			i++;
			dayindexPrev = event.getDayindex();
		}
		return map;
	}

}
