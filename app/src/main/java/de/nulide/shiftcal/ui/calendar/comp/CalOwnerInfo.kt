package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver
import de.nulide.shiftcal.ui.calendar.comp.usecase.SwitchCalendarViewUseCase.DISP

class CalOwnerInfo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs), ViewModelReceiver {

    val sc = SCRepoManager.getInstance(context)

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        calViewModel.register(lifecycle, calViewModel.switchDisp) { disp ->
            updateInfo(disp)
        }

        calViewModel.register(lifecycle, calViewModel.calendarChange) {
            updateInfo(calViewModel.getCurrentDisp())
        }

        calViewModel.register(lifecycle, calViewModel.resume) {
            updateInfo(calViewModel.getCurrentDisp())
        }

    }

    fun updateInfo(disp: DISP) {
        visibility = VISIBLE
        if (disp == DISP.WEEK) {
            text = context.getString(R.string.calendar_warning_combined_family_synced)
        } else {
            if (sc.familyMode) {
                text =
                    context.getString(R.string.calendar_warning_family_synced, sc.users.getName())
            } else {
                text = ""
                visibility = GONE
            }
        }
    }
}