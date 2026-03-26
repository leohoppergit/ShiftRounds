package de.nulide.shiftcal.data.legacy.factory;

import java.time.LocalDate;

import de.nulide.shiftcal.data.legacy.model.CalendarDate;

public class TimeFactory {
    public static LocalDate convertCalendarDateToLocalDate(CalendarDate date) {
        return LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
    }

}
