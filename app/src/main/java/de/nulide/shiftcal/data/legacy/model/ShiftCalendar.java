package de.nulide.shiftcal.data.legacy.model;


import java.util.ArrayList;
import java.util.List;

public class ShiftCalendar {

    private String name;
    private final List<WorkDay> calendar;
    private final List<Shift> shifts;

    private final List<ShiftBlock> shiftBlocks;

    private final List<MonthNote> notes;

    private int dataVersion;

    public ShiftCalendar() {
        name = "";
        calendar = new ArrayList<>();
        shifts = new ArrayList<>();
        shiftBlocks = new ArrayList<>();
        notes = new ArrayList<>();
        dataVersion = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WorkDay> getCalendar() {
        return calendar;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public List<ShiftBlock> getShiftBlocks() {
        return shiftBlocks;
    }

    public List<MonthNote> getNotes() {
        return notes;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }
}
