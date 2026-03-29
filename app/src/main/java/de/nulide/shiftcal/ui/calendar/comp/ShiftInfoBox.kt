package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.data.factory.TimeFactory
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.list.ShiftInfoBoxAdapter
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import java.time.LocalDate

class ShiftInfoBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs), ViewModelReceiver {

    val sc = SCRepoManager.getInstance(context)
    lateinit var calViewModel: CalViewModel
    val shiftInfoBoxAdapter: ShiftInfoBoxAdapter = ShiftInfoBoxAdapter(sc, context, this)

    init {
        adapter = shiftInfoBoxAdapter
        layoutManager = LinearLayoutManager(context)
        // The cards already animate their own expand/collapse state.
        // Disabling RecyclerView item animations avoids transient oversize flashes
        // when switching between empty days and populated days.
        itemAnimator = null
    }

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel
        calViewModel.register(
            lifecycle,
            calViewModel.newMonth
        ) { newYearMonth ->
            val currentSelectedYearMonth =
                TimeFactory.convLocalDateToYearMonth(calViewModel.getLastSelectedDay().date)
            if (currentSelectedYearMonth != newYearMonth) {
                shiftInfoBoxAdapter.clear()
            }
        }

        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            updatBox(calViewModel.getLastSelectedDay().date)
        }

        calViewModel.register(lifecycle, calViewModel.daySelected) { day ->
            if (calViewModel.getEditMode()) {
                shiftInfoBoxAdapter.clear()
            } else {
                updatBox(day.date)
            }
        }


    }

    fun updatBox(date: LocalDate) {
        if (calViewModel.getCurrentDisp() == DISP.WEEK) {
            val wdays =
                sc.fromLocal { sc.workDays.getCombinedOnDay(date) }.toMutableList()
            val famWdays = sc.fromNet { sc.workDays.getCombinedOnDay(date) }
            famWdays?.let { wdays.addAll(it) }
            shiftInfoBoxAdapter.updateData(wdays)
        } else {
            val wdays = sc.workDays.getCombinedOnDay(date)
            shiftInfoBoxAdapter.updateData(wdays)
        }
    }


}
