package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.ViewContainer
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.utils.ColorHelper
import java.util.LinkedList

class WeekDayViewContainer(view: View) : ViewContainer(view) {

    val root = view

    val dayTextView = view.findViewById<TextView>(R.id.dayText)

    val shiftList = view.findViewById<LinearLayout>(R.id.shiftList)

    lateinit var day: CalendarDay

    lateinit var calViewModel: CalViewModel
    private var onDayClickedListeners = LinkedList<OnDayClickedListener>()

    init {
        view.setOnClickListener {
            for (onDayClickedListener in onDayClickedListeners) {
                onDayClickedListener.onDayClicked(day)
            }
        }
    }


    fun clear() {
        shiftList.removeAllViews()
    }

    fun addShift(context: Context, shift: Shift?) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_shift, shiftList, false) as FrameLayout
        val textView = view.findViewById<TextView>(R.id.shiftName)
        if (shift != null) {
            textView.text = shift.shortName
            val boxDrawable = ContextCompat.getDrawable(context, R.drawable.rounded_box)
            boxDrawable?.setTint(shift.color)
            textView.background = boxDrawable
            if (ColorHelper.isTooBright(shift.color)) {
                textView.setTextColor(context.getColor(R.color.textColorBlack))
            } else {
                textView.setTextColor(context.getColor(R.color.textColorWhite))
            }
        }
        shiftList.addView(view)
    }

    fun addOnDayClickedListener(onDayClickedListener: OnDayClickedListener) {
        onDayClickedListeners.add(onDayClickedListener)
    }

    fun clearOnDayClickedListener() {
        onDayClickedListeners.clear()
    }

    fun addDivider(context: Context) {
        val dp = 1
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        val divider = View(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                px
            )
            background = ContextCompat.getDrawable(context, R.drawable.username_divider)
        }
        shiftList.addView(divider)
    }


}
