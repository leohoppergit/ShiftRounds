package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Runner
import java.time.LocalDate

class ShiftMonthDayBinder(
    private var context: Context,
    private var calViewModel: CalViewModel,
    private var calendarView: CalendarView,
    private var sc: SCRepoManager,
) : MonthDayBinder<DayViewContainer>,
    OnDayClickedListener {

    private val today = LocalDate.now()
    private var defaultTextColor = -1

    override fun bind(container: DayViewContainer, data: CalendarDay) {
        Runner.runCo {
            if (defaultTextColor == -1) {
                defaultTextColor = container.firstDigitDayTextView.textColors.defaultColor
            }
            container.firstDigitDayTextView.setTextColor(defaultTextColor)
            container.secondShiftTextView.setTextColor(defaultTextColor)
            container.shiftTextView.setTextColor(defaultTextColor)
            container.shiftTextView.setTypeface(null, Typeface.NORMAL)
            container.secondShiftTextView.setTypeface(null, Typeface.NORMAL)
            container.shiftTextView.background = null
            container.secondShiftTextView.background = null

            val selectorDrawable =
                ContextCompat.getDrawable(context, R.drawable.rounded_box_outline)
            val todayBoxDrawable = ContextCompat.getDrawable(context, R.drawable.today_box)

            container.calViewModel = calViewModel
            container.day = data
            container.clearOnDayClickedListener()
            container.addOnDayClickedListener(this)
            container.dayContainer.background = null
            container.firstDigitDayTextView.setTypeface(null, Typeface.NORMAL)
            container.secondShiftTextView.setTypeface(null, Typeface.NORMAL)

            val dateString = data.date.dayOfMonth.toString()

            //ShiftLogic
            if (sc.workDays.hasWork(data.date)) {
                val shifts = sc.shifts.getOn(data.date)
                val firstShift = shifts[0]

                container.shiftTextView.text = decorateShiftShortName(firstShift)
                applyShiftMarker(container.shiftTextView, firstShift)

                val boxDrawable = ContextCompat.getDrawable(context, R.drawable.rounded_box)
                    ?: return@runCo
                val shiftBoxDrawable = DrawableCompat.wrap(boxDrawable)
                DrawableCompat.setTint(shiftBoxDrawable, firstShift.color)

                if (shifts.size > 1) {
                    val secondShift = shifts[1]

                    //Text
                    container.secondShiftTextView.text = decorateShiftShortName(secondShift)
                    applyShiftMarker(container.secondShiftTextView, secondShift)
                    container.secondShiftTextView.visibility = View.VISIBLE

                    //Half-Box
                    val secondBoxDrawable =
                        ContextCompat.getDrawable(context, R.drawable.rounded_box)
                    val halfBoxDrawable =
                        ClipDrawable(secondBoxDrawable, Gravity.RIGHT, ClipDrawable.HORIZONTAL)
                    halfBoxDrawable.setLevel(5000)
                    halfBoxDrawable.setTint(secondShift.color)
                    val layer = arrayOfNulls<Drawable>(2)
                    layer[0] = shiftBoxDrawable
                    layer[1] = halfBoxDrawable
                    container.dayContainer.background = LayerDrawable(layer)
                    if (ColorHelper.isTooBright(secondShift.color)) {
                        container.secondDigitDayTextView.setTextColor(context.getColor(R.color.textColorBlack))
                        container.secondShiftTextView.setTextColor(context.getColor(R.color.textColorBlack))
                    } else {
                        container.secondDigitDayTextView.setTextColor(context.getColor(R.color.textColorWhite))
                        container.secondShiftTextView.setTextColor(context.getColor(R.color.textColorWhite))
                    }

                    // Show Day on View
                    if (dateString.length > 1) {
                        container.firstDigitDayTextView.text = dateString.subSequence(0, 1)
                        container.secondDigitDayTextView.text = dateString.subSequence(1, 2)
                        container.secondDigitDayTextView.visibility = View.VISIBLE
                    } else {
                        container.firstDigitDayTextView.text = dateString
                        container.secondDigitDayTextView.visibility = View.GONE
                    }
                } else {
                    container.dayContainer.background = shiftBoxDrawable
                    container.secondShiftTextView.visibility = View.GONE
                    container.secondShiftTextView.background = null
                    // Show Day on View
                    container.firstDigitDayTextView.text = dateString
                    container.secondDigitDayTextView.visibility = View.GONE
                }


                if (ColorHelper.isTooBright(firstShift.color)) {
                    container.firstDigitDayTextView.setTextColor(context.getColor(R.color.textColorBlack))
                    container.shiftTextView.setTextColor(context.getColor(R.color.textColorBlack))
                } else {
                    container.firstDigitDayTextView.setTextColor(context.getColor(R.color.textColorWhite))
                    container.shiftTextView.setTextColor(context.getColor(R.color.textColorWhite))
                }

            } else {
                container.firstDigitDayTextView.text = dateString
                container.secondDigitDayTextView.visibility = View.GONE
                container.shiftTextView.text = ""
                container.secondShiftTextView.text = ""
                container.shiftTextView.background = null
                container.secondShiftTextView.background = null
            }

            // Show Box over Today's day
            if (data.date == today) {
                var layer = arrayOfNulls<Drawable>(2)
                layer[0] = container.dayContainer.background
                layer[1] = todayBoxDrawable
                //Workaround for Android Oreo
                layer = layer.filter { it != null }.toTypedArray()
                container.dayContainer.background = LayerDrawable(layer)
                container.firstDigitDayTextView.setTypeface(null, Typeface.BOLD)
                container.secondDigitDayTextView.setTypeface(null, Typeface.BOLD)
            }

            if (data.position == DayPosition.MonthDate) {
                // Show Selector over selected Day
                if (calViewModel.getLastSelectedDay() == data) {
                    if (container.dayContainer.background != null) {
                        val layer = arrayOfNulls<Drawable>(2)
                        layer[0] = container.dayContainer.background
                        layer[1] = selectorDrawable
                        container.dayContainer.background = LayerDrawable(layer)
                    } else {
                        container.dayContainer.background = selectorDrawable
                    }
                }
                container.dayContainer.alpha = 1F
            } else {
                // Make in and out dates slightly transparent
                container.dayContainer.alpha = 0.39F
            }
        }

    }

    override fun create(view: View) = DayViewContainer(view)

    override fun onDayClicked(date: CalendarDay) {
        if (date.position == DayPosition.MonthDate) {
            val lastSelectedDay = calViewModel.getLastSelectedDay()
            calViewModel.setLastSelectedDay(date)
            calendarView.notifyDayChanged(lastSelectedDay)
            calendarView.notifyDayChanged(date)
        }
    }

    private fun decorateShiftShortName(shift: de.nulide.shiftcal.data.model.Shift): String {
        return when {
            shift.customBalanceMinutes == null -> shift.shortName
            shift.customBalanceMinutes < 0 -> "${shift.shortName}-"
            else -> "${shift.shortName}+"
        }
    }

    private fun applyShiftMarker(textView: android.widget.TextView, shift: de.nulide.shiftcal.data.model.Shift) {
        if (shift.customBalanceMinutes != null) {
            textView.setTypeface(null, Typeface.BOLD)
            val marker = ContextCompat.getDrawable(context, R.drawable.today_box)?.mutate()
            marker?.alpha = 90
            textView.background = marker
        } else {
            textView.background = null
        }
    }


}
