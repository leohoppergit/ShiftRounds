package de.nulide.shiftcal.data.legacy.model;

import android.graphics.Color;

public class Shift {
    private String name;
    private String short_name;
    private int id;
    private ShiftTime startTime;
    private ShiftTime endTime;
    private int breakMinutes;
    private int timeTillAlarm;
    private int color;
    private boolean toAlarm;
    private boolean archived;

    public Shift() {
        this.name = "Error";
        this.short_name = "err";
        startTime = new ShiftTime(0, 0);
        endTime = new ShiftTime(0, 0);
        breakMinutes = 0;
        this.color = Color.BLACK;
        this.toAlarm = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ShiftTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ShiftTime startTime) {
        this.startTime = startTime;
    }

    public ShiftTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ShiftTime endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public boolean isToAlarm() {
        return toAlarm;
    }

    public boolean isArchived() {
        return this.archived;
    }

    public int getTimeTillAlarm() {
        return timeTillAlarm;
    }
}

