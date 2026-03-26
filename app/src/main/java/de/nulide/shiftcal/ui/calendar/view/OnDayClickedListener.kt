package de.nulide.shiftcal.ui.calendar.view

import com.kizitonwose.calendar.core.CalendarDay

interface OnDayClickedListener {
    fun onDayClicked(date: CalendarDay);
}