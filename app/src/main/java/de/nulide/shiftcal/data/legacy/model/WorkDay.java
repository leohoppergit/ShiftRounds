package de.nulide.shiftcal.data.legacy.model;


import java.util.HashSet;
import java.util.Set;

public class WorkDay {

    private int id;
    private CalendarDate date;
    private int shift;
    private Set<Integer> icons;
    private long eventID;
    private boolean dismissed;

    public WorkDay() {
        this.id = -1;
        this.date = new CalendarDate();
        this.shift = -1;
        this.icons = new HashSet<>();
        this.eventID = -1;
        this.dismissed = false;
    }

    public CalendarDate getDate() {
        return date;
    }

    public void setDate(CalendarDate date) {
        this.date = date;
    }

    public int getShift() {
        return shift;
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    private void setShift(int shift) {
        this.shift = shift;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Integer> getIcons() {
        return icons;
    }

    public void setIcons(Set<Integer> icons) {
        this.icons = icons;
    }

    public void addIcon(int icon) {
        this.icons.add(icon);
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }
}
