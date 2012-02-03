/**
 * 
 */
package org.fosdem.util;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Track;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class TrackAdapter extends ArrayAdapter<Track> {
	
	private ArrayList<Track> items;

	public TrackAdapter(Context context, int textViewResourceId, ArrayList<Track> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(R.layout.track_list, null);
		}

		Track track = items.get(position);
		if (track != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			title.setText(track.getName());
			TextView type = (TextView) v.findViewById(R.id.type);
			type.setText(track.getType());
		}

		return v;
	}
	
}
