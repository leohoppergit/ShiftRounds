package de.nulide.shiftcal.ui.calendar.comp

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.calendar.comp.helper.ViewModelReceiver

class MonthNoteEdit @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ExtendedFloatingActionButton(context, attrs), ViewModelReceiver {

    override fun receiveViewModel(
        lifecycle: Lifecycle,
        calViewModel: CalViewModel
    ) {
        setOnClickListener { calViewModel.trigger(calViewModel.editMonthNote) }
    }

}