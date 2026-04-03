package de.nulide.shiftcal.ui.calendar

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalViewModel : ViewModel() {
    private val currentMonth = MutableLiveData<YearMonth>()
    private val editMode = MutableLiveData<Boolean>()
    private val oldestMonth = MutableLiveData<YearMonth>()
    private val newestMonth = MutableLiveData<YearMonth>()

    private val lastSelectedDay = MutableLiveData<CalendarDay>()
    private val currentDisp = MutableLiveData<DISP>()

    // Triggers when calendar is changed
    val calendarChange = MutableSharedFlow<Unit>()

    // Triggers when the current calendar has changed data
    val update = MutableSharedFlow<Unit>()

    // Triggers when the user swipes to a new month
    val newMonth = MutableSharedFlow<YearMonth>()

    // Triggers when the user clicks on a new day
    val daySelected = MutableSharedFlow<CalendarDay>()

    // Triggers when a specific day has changed
    val dayUpdated = MutableSharedFlow<LocalDate>()

    val scrollTo = MutableSharedFlow<LocalDate>()

    val shiftSelected = MutableSharedFlow<Unit>()

    val shiftBlockSelected = MutableSharedFlow<Unit>()
    val switchDisp = MutableSharedFlow<DISP>()

    val editMonthNote = MutableSharedFlow<Unit>()

    val resume = MutableSharedFlow<Unit>()

    fun setCurrentMonth(currentMonth: YearMonth) {
        if (this.currentMonth.value == currentMonth) {
            return
        }
        this.currentMonth.value = currentMonth
        trigger(newMonth, currentMonth)
    }

    fun getCurrentMonth(): YearMonth {
        return currentMonth.value ?: YearMonth.now()
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode.value = editMode
    }

    fun getEditMode(): Boolean {
        return editMode.value ?: false
    }

    fun getLiveEditMode(): MutableLiveData<Boolean> {
        return editMode
    }

    fun setOldestMonth(yearMonth: YearMonth) {
        this.oldestMonth.value = yearMonth
    }

    fun getOldestMonth(): YearMonth {
        return oldestMonth.value ?: YearMonth.now()
    }

    fun setNewestMonth(yearMonth: YearMonth) {
        this.newestMonth.value = yearMonth
    }

    fun getNewestMonth(): YearMonth {
        return newestMonth.value ?: YearMonth.now()
    }

    fun getCurrentMonthLive(): MutableLiveData<YearMonth> {
        return currentMonth
    }

    fun getCurrentDisp(): DISP {
        return currentDisp.value ?: DISP.MONTH
    }

    fun setCurrentDisp(currentDisp: DISP) {
        this.currentDisp.value = currentDisp
        trigger(switchDisp, currentDisp)
    }

    fun getLastSelectedDay(): CalendarDay {
        return lastSelectedDay.value ?: CalendarDay(LocalDate.now(), DayPosition.MonthDate)
    }

    fun setLastSelectedDay(day: CalendarDay) {
        val current = lastSelectedDay.value
        if (current?.date == day.date && current.position == day.position) {
            return
        }
        lastSelectedDay.value = day
        trigger(daySelected, day)
    }


    fun <T> set(data: MutableLiveData<T>, t: T) {
        data.value = t
    }

    fun <T> trigger(flow: MutableSharedFlow<T>, t: T) {
        viewModelScope.launch {
            flow.emit(t)
        }
    }

    fun trigger(flow: MutableSharedFlow<Unit>) {
        viewModelScope.launch {
            flow.emit(Unit)
        }
    }

    fun <T> register(
        lifecycle: Lifecycle,
        flow: MutableSharedFlow<T>,
        run: suspend (T) -> Unit
    ) {
        lifecycle.coroutineScope.launch {
            flow.asSharedFlow().collect { dat -> run(dat) }
        }
    }
}
