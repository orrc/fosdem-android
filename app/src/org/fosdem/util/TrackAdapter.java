package org.fosdem.util;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Track;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class TrackAdapter extends ArrayAdapter<Track> implements SpinnerAdapter {

	private int layoutResourceId;
	private int textViewResourceId;
	private ArrayList<Track> items;
	private int dropDownResourceId;

	public TrackAdapter(Context context, int layoutResourceId, int textViewResourceId, ArrayList<Track> items) {
		super(context, layoutResourceId, textViewResourceId, items);
		this.layoutResourceId = layoutResourceId;
		this.dropDownResourceId = layoutResourceId;
		this.textViewResourceId = textViewResourceId;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(layoutResourceId, null);
		}

		Track track = items.get(position);
		if (track != null) {
			TextView title = (TextView) v.findViewById(textViewResourceId);
			title.setText(track.getName());
			TextView type = (TextView) v.findViewById(R.id.type);
			if (type != null) {
				type.setText(track.getType());
			}
		}

		return v;
	}

	/**
	 * Sets the layout resource to create the drop down views.
	 */
	public void setDropDownViewResource(int resource) {
		this.dropDownResourceId = resource;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// returns a dropdown view if used as SpinnerAdapter
		View v = convertView;
		if (v == null) {
			v = LayoutInflater.from(getContext()).inflate(dropDownResourceId, null);
		}

		Track track = items.get(position);
		if (track != null) {
			TextView title = (TextView) v.findViewById(textViewResourceId);
			title.setText(track.getName());
		}

		return v;
	}

	public Integer getPositionOfTrack(String trackName) {
		int i = 0;

		for (Track track : items) {
			if (trackName.equals(track.getName())) {
				return i;
			}
			i++;
		}

		return null;
	}

}
