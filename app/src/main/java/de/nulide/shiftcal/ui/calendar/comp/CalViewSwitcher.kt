package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Lifecycle
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase

class CalViewSwitcher @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ExtendedFloatingActionButton(context, attrs), ViewModelReceiver, View.OnClickListener {

    val sc = SCRepoManager.getInstance(context)
    val settings = SettingsRepository.getInstance(context)
    lateinit var calViewModel: CalViewModel
    lateinit var switchCalendarViewUseCase: SwitchCalendarViewUseCase

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        this.calViewModel = calViewModel

        switchCalendarViewUseCase =
            SwitchCalendarViewUseCase(settings, calViewModel)

        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            updateVisiblity()
        }

        calViewModel.register(lifecycle, calViewModel.resume) {
            updateVisiblity()
        }

        setOnClickListener(this)
    }

    fun updateVisiblity() {
        if (sc.familyMode) {
            visibility = VISIBLE
        } else {
            visibility = GONE
        }
    }


    override fun onClick(p0: View?) {
        switchCalendarViewUseCase.switch()
    }

}