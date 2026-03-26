package de.nulide.shiftcal.data.legacy.model;

public class ShiftTime {

    private int hour;
    private int minute;

    public ShiftTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public ShiftTime() {
        this.hour = 0;
        this.minute = 0;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int toInt() {
        return hour * 60 + minute;
    }

}
