package de.nulide.shiftcal.ui.calendar.comp.helper

import androidx.lifecycle.Lifecycle
import de.nulide.shiftcal.ui.calendar.CalViewModel

interface ViewModelReceiver {

    fun receiveViewModel(lifecycle: Lifecycle, calViewModel: CalViewModel)

}