package de.nulide.shiftcal.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.CalendarContract;

import java.util.Calendar;

import de.nulide.shiftcal.data.model.Shift;
import de.nulide.shiftcal.data.model.WorkDay;
import de.nulide.shiftcal.data.repository.SCRepoManager;

public class EventController {

    private ContentResolver cr;
    private long calId;
    private SCRepoManager sc;

    public EventController(ContentResolver cr, long calId, SCRepoManager sc) {
        this.cr = cr;
        this.calId = calId;
        this.sc = sc;
    }

    public EventController() {

    }

    public void createEvent(WorkDay day) {
        if (cr != null) {
            Shift s = sc.fromLocal(() -> sc.getShifts().get(day.getShiftId()));
            Calendar startCal = Calendar.getInstance();
            startCal.set(day.getDay().getYear(), day.getDay().getMonthValue() - 1, day.getDay().getDayOfMonth());
            startCal.set(Calendar.HOUR_OF_DAY, s.getStartTime().getHour());
            startCal.set(Calendar.MINUTE, s.getStartTime().getMinute());
            Calendar endCal = Calendar.getInstance();
            endCal.set(day.getDay().getYear(), day.getDay().getMonthValue() - 1, day.getDay().getDayOfMonth());
            endCal.add(Calendar.DAY_OF_MONTH, s.getEndDayOffset());
            endCal.set(Calendar.HOUR_OF_DAY, s.getEndTime().getHour());
            endCal.set(Calendar.MINUTE, s.getEndTime().getMinute());
            long start = startCal.getTimeInMillis();
            long end = endCal.getTimeInMillis();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, start);
            values.put(CalendarContract.Events.DTEND, end);
            values.put(CalendarContract.Events.TITLE, s.getShortName() + " - " + s.getName());
            values.put(CalendarContract.Events.CALENDAR_ID, calId);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Berlin");
            values.put(CalendarContract.Events.EVENT_COLOR, s.getColor());
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
        }
    }

    //not working
    public void deleteEvent(long evId) {
        if (cr != null) {
            String[] args = new String[]{Long.toString(evId)};
            cr.delete(CalendarContract.Events.CONTENT_URI, CalendarContract.Events._SYNC_ID + " =? ", args);
        }
    }


}
