/**
 * 
 */
package org.fosdem.schedules;

import java.util.ArrayList;

import org.fosdem.R;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Room;
import org.fosdem.util.RoomAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * @author Christophe Vandeplas <christophe@vandeplas.com>
 *
 */
public class RoomListActivity extends ListActivity  {

	public static final String LOG_TAG=RoomListActivity.class.getName();
    
	private ArrayList<Room> rooms = null;
	private Day day = null;
	private int dayIndex = 0;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// what day should we show? fetch from the parameters or saved instance
		dayIndex = savedInstanceState != null ? savedInstanceState.getInt(Day.CLASSNAME) : 0;
		
		if (dayIndex == 0) { 
			Bundle extras = getIntent().getExtras();
			if (extras != null)
				dayIndex = extras.getInt(Day.CLASSNAME);
			if (dayIndex == 0 ) {
				Log.e(LOG_TAG, "You are loading this class with no valid day parameter");
				return;
			}
		}
		
		setTitle("Rooms for Day " + dayIndex);
		
//		day = new Day();
//		day.setIndex(dayIndex);

		// FIXME rooms = database.getRoomsByDayIndex(day_i);
		
		Room room1 = new Room("Janson");
		Room room2 = new Room("Chavanne");
		Room room3 = new Room("Ferrer");
		
		rooms = new ArrayList<Room>();
		rooms.add(room1);
		rooms.add(room2);
		rooms.add(room3);
		
		
//		String[] room_a = { "foo", "bar" };
//		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room_a));
			
		// TODO chri - adapt layout to show a right arrow 
        setListAdapter(new RoomAdapter(this, R.layout.simple_list_tab_indicator, rooms));
       
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Room room = (Room) getListView().getItemAtPosition(position);
        
        Log.d(LOG_TAG, "Room selected: " + room.getName());
        
        // TODO load list of Events in Room
        Intent i = new Intent(this, EventListActivity.class);
		i.putExtra(Room.CLASSNAME, room.getName());
		startActivity(i);
    }
    
    
	
}
