package org.fosdem.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.fosdem.db.DBAdapter;
import org.fosdem.exceptions.ParserException;
import org.fosdem.parsers.ScheduleParser;
import org.fosdem.pojo.Day;
import org.fosdem.pojo.Event;
import org.fosdem.pojo.Room;
import org.fosdem.pojo.Schedule;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.test.AndroidTestCase;
import android.util.Log;

public class ScheduleParseTest extends AndroidTestCase {

	private static Schedule schedule;

	public void testScheduleParses() throws NameNotFoundException {
		ScheduleParser sp = null;
		try {
			Context context = getContext().createPackageContext("org.fosdem.tests",
				Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			InputStream xml = context.getAssets().open("schedule-2012.xml");
			sp = new ScheduleParser(xml);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Failed to parse schedule");
		}
		assertNotNull(sp);

		Schedule s = null;
		try {
			s = sp.parse();
		} catch (ParserException e) {
			e.printStackTrace();
			fail("Failed to parse");
		}
		assertNotNull(s);
		assertNotNull(s.getDays());
		assertEquals(2, s.getDays().size());
		assertEquals("Brussels", s.getConference().getCity());
		assertTrue(((Day) (s.getDays().toArray()[0])).getRooms().size() > 0);
		Collection<Room> rooms = ((Day) (s.getDays().toArray()[0])).getRooms();
		assertTrue(((Room) (rooms.toArray()[0])).getEvents().size() > 0);
		schedule = s;
	}

	public void testSchedulePersistence() {
		DBAdapter db = new DBAdapter(getContext());

		db.open();
		try {
			db.clearEvents();
			assertEquals(0, db.getEvents().size());

			// Persist parsed schedule data to DB
			db.persistSchedule(schedule, new Handler());
			int total = 0;
			for (Day day : schedule.getDays()) {
				for (Room room : day.getRooms()) {
					total += room.getEvents().size();
				}
			}

			// Expect that some events were persisted
			assertTrue(total > 0);

			// Pull events from DB and assert count is as expected
			List<Event> events = db.getEvents();
			assertEquals(total, events.size());
		} finally {
			db.close();
		}
	}

	public void testScheduleQueryByCriteria() {
		DBAdapter db = new DBAdapter(getContext());
		db.open();
		try {
			// Should be 3 Certification events on Saturday
			List<Event> events = db.getEventsFiltered(null, null,
					new String[] { "Certification" }, null, null, null, null, 1);
			assertEquals(3, events.size());
		} finally {
			db.close();
		}
	}

	public void testScheduleQueriesByDate() {
		DBAdapter db = new DBAdapter(getContext());
		db.open();
		try {
			String[] roomsByDay = db.getRoomsByDayIndex(1);
			String[] tracksByDay = db.getRoomsByDayIndex(1);

			assertTrue(roomsByDay.length > 0);
			assertTrue(tracksByDay.length > 0);
			Log.v(getClass().getName(), "Rooms by day: " + roomsByDay.length
					+ " " + roomsByDay.toString());
			Log.v(getClass().getName(), "Tracks by day: " + tracksByDay.length
					+ " " + tracksByDay.toString());
		} finally {
			db.close();
		}
	}

	public void testScheduleQueryByRoomByTrack() {
		DBAdapter db = new DBAdapter(getContext());
		db.open();

		String[] rooms = db.getRooms();
		String[] tracks = db.getTracks();

		assertTrue(rooms.length > 0);
		assertTrue(tracks.length > 0);

		db.close();
	}
}
