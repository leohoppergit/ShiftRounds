package de.nulide.shiftcal.data.legacy.model;

public class CalendarDate {

    private int year;
    private int month;
    private int day;

    public CalendarDate() {
        this.year = 1990;
        this.month = 1;
        this.day = 1;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

}
