package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.google.android.material.card.MaterialCardView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.calendar.CalendarSpecialDateRepository
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import de.nulide.shiftcal.ui.calendar.specialdate.CalendarSpecialDateUiHelper
import de.nulide.shiftcal.utils.Runner
import java.time.LocalDate

class CalendarSpecialDateBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MaterialCardView(context, attrs), ViewModelReceiver {

    private val specialDateRepository = CalendarSpecialDateRepository(context, SettingsRepository.getInstance(context))
    private lateinit var calViewModel: CalViewModel
    private val titleText: TextView
    private val container: LinearLayout

    init {
        LayoutInflater.from(context).inflate(R.layout.comp_calendar_special_date_box, this, true)
        titleText = findViewById(R.id.specialDateTitleText)
        container = findViewById(R.id.specialDateList)
    }

    override fun receiveViewModel(lifecycle: Lifecycle, calViewModel: CalViewModel) {
        this.calViewModel = calViewModel

        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            if (calViewModel.getEditMode()) {
                hide()
            } else {
                update(day.date)
            }
        }

        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            update(calViewModel.getLastSelectedDay().date)
        }

        calViewModel.register(lifecycle, calViewModel.newMonth) {
            update(calViewModel.getLastSelectedDay().date)
        }

        calViewModel.register(lifecycle, calViewModel.switchDisp) { disp ->
            if (disp == DISP.WEEK) {
                update(calViewModel.getLastSelectedDay().date)
            }
        }
    }

    private fun update(date: LocalDate) {
        Runner.runCo {
            val entries = specialDateRepository.getEntriesOn(date)
            if (entries.isEmpty()) {
                hide()
                return@runCo
            }

            titleText.text = context.getString(R.string.calendar_special_date_box_title)
            container.removeAllViews()
            val inflater = LayoutInflater.from(context)
            entries.forEach { entry ->
                val row = inflater.inflate(R.layout.item_calendar_special_date, container, false)
                val dot = row.findViewById<View>(R.id.specialDateDot)
                val name = row.findViewById<TextView>(R.id.specialDateNameText)
                val type = row.findViewById<TextView>(R.id.specialDateTypeText)
                val range = row.findViewById<TextView>(R.id.specialDateRangeText)

                dot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    CalendarSpecialDateUiHelper.getColor(context, entry.type)
                )
                name.text = entry.name
                type.text = CalendarSpecialDateUiHelper.getTypeLabel(context, entry.type)
                range.text = CalendarSpecialDateUiHelper.formatRange(entry.startDate, entry.endDate)

                container.addView(row)
            }
            isVisible = true
        }
    }

    private fun hide() {
        container.removeAllViews()
        isVisible = false
    }
}
