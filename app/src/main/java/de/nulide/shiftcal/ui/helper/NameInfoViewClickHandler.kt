package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.nulide.shiftcal.utils.TooltipHelper

class NameInfoViewClickHandler constructor(
    val context: Context,
    val view: TextView,
    val name: String
) : View.OnClickListener {

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val activity = context as? AppCompatActivity
        if (activity != null) {
            val balloon = TooltipHelper.createSmallTooltip(
                context,
                activity,
                TooltipHelper.START,
                name
            )
            balloon.showAlignEnd(view)
        }
    }

}
