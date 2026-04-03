package de.nulide.shiftcal.ui.calendar.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import de.nulide.shiftcal.R
import de.nulide.shiftcal.ui.calendar.CalViewModel
import java.util.LinkedList

class DayViewContainer(view: View) : ViewContainer(view) {

    val dayContainer = view.findViewById<ViewGroup>(R.id.dayContainer)
    val firstDigitDayTextView = view.findViewById<TextView>(R.id.firstDigitCalendarDayText)
    val secondDigitDayTextView = view.findViewById<TextView>(R.id.secondDigitCalendarDayText)
    val shiftTextView = view.findViewById<TextView>(R.id.shiftText)
    val secondShiftTextView = view.findViewById<TextView>(R.id.secondShiftText)
    val specialDateMarkerViews = listOf(
        view.findViewById<View>(R.id.specialDateMarker1),
        view.findViewById<View>(R.id.specialDateMarker2),
        view.findViewById<View>(R.id.specialDateMarker3)
    )

    lateinit var calViewModel: CalViewModel
    private var onDayClickedListeners = LinkedList<OnDayClickedListener>()

    lateinit var day: CalendarDay

    init {
        view.setOnClickListener {
            for (onDayClickedListener in onDayClickedListeners) {
                onDayClickedListener.onDayClicked(day)
            }
        }
    }

    fun addOnDayClickedListener(onDayClickedListener: OnDayClickedListener) {
        onDayClickedListeners.add(onDayClickedListener)
    }

    fun clearOnDayClickedListener() {
        onDayClickedListeners.clear()
    }

}
