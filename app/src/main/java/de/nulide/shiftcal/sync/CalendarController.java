package de.nulide.shiftcal.sync;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import de.nulide.shiftcal.utils.permission.Perm;

public class CalendarController {
    public static final String ACCOUNT_NAME = "ShiftRounds";
    public static final String OLD_ACCOUNT_NAME = "ShiftCal";
    public static final String DISPLAY_NAME = "Shifts";
    public static final String ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL;

    public static final String[] PROJECTION = new String[]{
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR
    };

    public static void addShiftCalCalendar(Context context, int color) {
        ContentResolver cr = context.getContentResolver();
        Uri.Builder builder = CalendarContract.Calendars.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
        builder.appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        builder.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true");
        cr.insert(builder.build(), buildContentValues(color));
    }

    private static Uri buildCalUri() {
        return CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
    }

    private static Uri buildOldCalUri() {
        return CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, OLD_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE).build();
    }

    private static ContentValues buildContentValues(int color) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, DISPLAY_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, DISPLAY_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, color);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Europe/Berlin");
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        return values;
    }

    public static void deleteCalendar(Context c) {
        if (Perm.Companion.check(c, Perm.CALENDAR)) {
            ContentResolver cr = c.getContentResolver();
            Uri calUri = ContentUris.withAppendedId(buildCalUri(), getCalendarId(cr));
            Uri oldCalUri = ContentUris.withAppendedId(buildOldCalUri(), getCalendarId(cr));
            try {
                cr.delete(calUri, null, null);
                cr.delete(oldCalUri, null, null);
            } catch (IllegalArgumentException e) {
                // ignore error
            }
        }
    }

    public static long getCalendarId(ContentResolver cr) {
        Cursor calCursor = cr.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null, CalendarContract.Calendars._ID + " ASC");
        assert calCursor != null;
        if (calCursor.moveToFirst()) {
            do {
                long id = calCursor.getLong(0);
                String displayName = calCursor.getString(1);
                if (displayName.equals(DISPLAY_NAME)) {
                    calCursor.close();
                    return id;
                }
            } while (calCursor.moveToNext());
        }
        return -1;
    }
}


