package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.calendar.CalendarSpecialDate
import de.nulide.shiftcal.data.calendar.CalendarSpecialDateRepository
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.specialdate.CalendarSpecialDateUiHelper
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Runner
import java.time.LocalDate
import java.time.YearMonth

class ShiftMonthDayBinder(
    private var context: Context,
    private var calViewModel: CalViewModel,
    private var calendarView: CalendarView,
    private var sc: SCRepoManager,
) : MonthDayBinder<DayViewContainer>,
    OnDayClickedListener {

    private val today = LocalDate.now()
    private var defaultTextColor = -1
    private val settings = SettingsRepository.getInstance(context)
    private val specialDateRepository = CalendarSpecialDateRepository(
        context,
        settings
    )
    private val shiftMonthCache = mutableMapOf<YearMonth, Map<LocalDate, List<de.nulide.shiftcal.data.model.Shift>>>()
    private val specialDateMonthCache = mutableMapOf<YearMonth, Map<LocalDate, List<CalendarSpecialDate>>>()

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
            clearSpecialDateMarkers(container)
            applyMonthTileScale(container)

            val selectorDrawable =
                ContextCompat.getDrawable(context, R.drawable.shift_rounds_selected_day_outline)

            container.calViewModel = calViewModel
            container.day = data
            container.clearOnDayClickedListener()
            container.addOnDayClickedListener(this)
            container.dayContainer.background = null
            container.firstDigitDayTextView.setTypeface(null, Typeface.NORMAL)
            container.secondShiftTextView.setTypeface(null, Typeface.NORMAL)

            val dateString = data.date.dayOfMonth.toString()

            val shifts = getShiftEntriesOn(data.date)

            //ShiftLogic
            if (shifts.isNotEmpty()) {
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
                        ClipDrawable(secondBoxDrawable, Gravity.END, ClipDrawable.HORIZONTAL)
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

            applySpecialDateMarkers(container, getSpecialDatesOn(data.date), container.dayContainer.background != null)

            // Show Box over Today's day
            if (data.date == today) {
                val todayBoxDrawable = createTodayHighlight(
                    shiftColor = shifts.firstOrNull()?.color,
                    hasShiftBackground = container.dayContainer.background != null
                )
                var layer = arrayOfNulls<Drawable>(2)
                layer[0] = container.dayContainer.background
                layer[1] = todayBoxDrawable
                //Workaround for Android Oreo
                layer = layer.filter { it != null }.toTypedArray()
                container.dayContainer.background = LayerDrawable(layer)
                container.firstDigitDayTextView.setTypeface(null, Typeface.BOLD)
                container.secondDigitDayTextView.setTypeface(null, Typeface.BOLD)
                if (shifts.size <= 1) {
                    container.shiftTextView.setTypeface(null, Typeface.BOLD)
                }
                if (shifts.size > 1) {
                    container.secondShiftTextView.setTypeface(null, Typeface.BOLD)
                }
            }

            if (data.position == DayPosition.MonthDate) {
                // Show Selector over selected Day
                if (calViewModel.getLastSelectedDay().date == data.date) {
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
        val lastSelectedDay = calViewModel.getLastSelectedDay()
        val selectedDay = if (date.position == DayPosition.MonthDate) {
            date
        } else {
            CalendarDay(date.date, DayPosition.MonthDate)
        }

        if (calViewModel.getEditMode() && lastSelectedDay.date == selectedDay.date) {
            calViewModel.trigger(calViewModel.daySelected, selectedDay)
            return
        }

        calViewModel.setLastSelectedDay(selectedDay)

        if (date.position == DayPosition.MonthDate) {
            calendarView.notifyDateChanged(lastSelectedDay.date)
            calendarView.notifyDateChanged(date.date)
        } else {
            calendarView.smoothScrollToMonth(TimeFactory.convLocalDateToYearMonth(date.date))
        }
    }

    private fun decorateShiftShortName(shift: de.nulide.shiftcal.data.model.Shift): String {
        val baseShortName = shift.shortName.trim()
        return if (baseShortName.length <= 4) baseShortName else baseShortName.take(4)
    }

    private fun applyShiftMarker(textView: android.widget.TextView, shift: de.nulide.shiftcal.data.model.Shift) {
        textView.background = null
    }

    private fun clearSpecialDateMarkers(container: DayViewContainer) {
        container.specialDateMarkerViews.forEach {
            it.visibility = View.GONE
            it.background = null
        }
    }

    private fun applySpecialDateMarkers(
        container: DayViewContainer,
        specialDates: List<CalendarSpecialDate>,
        hasShiftBackground: Boolean
    ) {
        val uniqueTypes = specialDates.map { it.type }.distinct().take(container.specialDateMarkerViews.size)
        val strokeColor = if (hasShiftBackground) {
            context.getColor(R.color.textColorWhite)
        } else {
            context.getColor(R.color.shiftRoundsPanelStroke)
        }

        container.specialDateMarkerViews.forEachIndexed { index, view ->
            val type = uniqueTypes.getOrNull(index)
            if (type == null) {
                view.visibility = View.GONE
                view.background = null
            } else {
                view.visibility = View.VISIBLE
                view.background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(CalendarSpecialDateUiHelper.getColor(context, type))
                    setStroke(1, strokeColor)
                }
            }
        }
    }

    private fun getShiftEntriesOn(date: LocalDate): List<de.nulide.shiftcal.data.model.Shift> {
        val yearMonth = YearMonth.from(date)
        val monthMap = shiftMonthCache.getOrPut(yearMonth) {
            buildShiftMonthCache(yearMonth)
        }
        return monthMap[date].orEmpty()
    }

    private fun buildShiftMonthCache(yearMonth: YearMonth): Map<LocalDate, List<de.nulide.shiftcal.data.model.Shift>> {
        val shiftLookup = sc.shifts.getAll().associateBy { it.id }
        return sc.workDays.getWorkDaysOfMonth(yearMonth.year, yearMonth.monthValue)
            .groupBy { it.day }
            .mapValues { (_, workDays) ->
                workDays.mapNotNull { workDay -> shiftLookup[workDay.shiftId] }
                    .sortedWith(
                        compareBy<de.nulide.shiftcal.data.model.Shift>(
                            { it.startTime.timeInMinutes },
                            { it.endDayOffset },
                            { it.endTime.timeInMinutes },
                            { it.sortOrder },
                            { it.id }
                        )
                    )
            }
    }

    private fun getSpecialDatesOn(date: LocalDate): List<CalendarSpecialDate> {
        val yearMonth = YearMonth.from(date)
        val monthMap = specialDateMonthCache.getOrPut(yearMonth) {
            buildSpecialDateMonthCache(yearMonth)
        }
        return monthMap[date].orEmpty()
    }

    private fun buildSpecialDateMonthCache(yearMonth: YearMonth): Map<LocalDate, List<CalendarSpecialDate>> {
        val map = mutableMapOf<LocalDate, List<CalendarSpecialDate>>()
        var day = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        while (!day.isAfter(end)) {
            map[day] = specialDateRepository.getEntriesOn(day)
            day = day.plusDays(1)
        }
        return map
    }

    private fun applyMonthTileScale(container: DayViewContainer) {
        when (settings.getInt(Settings.MONTH_TILE_SCALE).coerceIn(0, 2)) {
            2 -> {
                container.firstDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                container.secondDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                container.shiftTextView.setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP)
                container.secondShiftTextView.setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP)
            }
            0 -> {
                container.firstDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                container.secondDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                container.shiftTextView.setAutoSizeTextTypeUniformWithConfiguration(7, 10, 1, TypedValue.COMPLEX_UNIT_SP)
                container.secondShiftTextView.setAutoSizeTextTypeUniformWithConfiguration(7, 10, 1, TypedValue.COMPLEX_UNIT_SP)
            }
            else -> {
                container.firstDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                container.secondDigitDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.5f)
                container.shiftTextView.setAutoSizeTextTypeUniformWithConfiguration(8, 11, 1, TypedValue.COMPLEX_UNIT_SP)
                container.secondShiftTextView.setAutoSizeTextTypeUniformWithConfiguration(8, 11, 1, TypedValue.COMPLEX_UNIT_SP)
            }
        }
    }

    private fun createTodayHighlight(shiftColor: Int?, hasShiftBackground: Boolean): Drawable {
        val strokeColor = when {
            shiftColor == null -> context.getColor(R.color.shiftRoundsActionInk)
            ColorHelper.isTooBright(shiftColor) -> context.getColor(R.color.textColorBlack)
            else -> context.getColor(R.color.textColorWhite)
        }

        val fillColor = when {
            shiftColor == null && isNightMode() -> Color.argb(42, 111, 169, 205)
            shiftColor == null -> Color.argb(26, 0, 0, 0)
            ColorHelper.isTooBright(shiftColor) -> Color.argb(34, 0, 0, 0)
            hasShiftBackground -> Color.argb(38, 255, 255, 255)
            else -> Color.argb(28, 255, 255, 255)
        }

        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                context.resources.displayMetrics
            )
            setColor(fillColor)
            setStroke(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    4f,
                    context.resources.displayMetrics
                ).toInt(),
                strokeColor
            )
        }
    }

    private fun isNightMode(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }

    fun invalidateDate(date: LocalDate) {
        val yearMonth = YearMonth.from(date)
        shiftMonthCache.remove(yearMonth)
        specialDateMonthCache.remove(yearMonth)
    }

    fun clearCaches() {
        shiftMonthCache.clear()
        specialDateMonthCache.clear()
    }


}
