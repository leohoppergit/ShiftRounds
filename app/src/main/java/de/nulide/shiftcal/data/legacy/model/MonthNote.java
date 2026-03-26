package de.nulide.shiftcal.data.legacy.model;

public class MonthNote {
    private int year;
    private int month;
    private String note;

    public MonthNote() {
        this.year = 1990;
        this.month = 1;
        this.note = "";
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
