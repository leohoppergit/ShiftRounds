package de.nulide.shiftcal.ui.calendar.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginBottom
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.CompWeekShiftCalBinding
import de.nulide.shiftcal.ui.helper.NameInfoViewClickHandler
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Runner

class UserNameRowHelper(val context: Context, binding: CompWeekShiftCalBinding) {

    init {
        val userNameRow = binding.root.findViewById<LinearLayout>(R.id.userNamesContainer)
        val weekDayName = binding.root.findViewById<LinearLayout>(R.id.weekTitlesContainer)

        val dummyView =
            LayoutInflater.from(context).inflate(R.layout.calendar_week_day_layout, null)
        dummyView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,  // or a fixed width if needed
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        dummyView.measure(widthMeasureSpec, heightMeasureSpec)
        Runner.runLate {
            userNameRow.y =
                weekDayName.y + weekDayName.measuredHeight + weekDayName.marginBottom + dummyView.measuredHeight
        }

        userNameRow.removeAllViews()
        val nameList = mutableMapOf<String, Boolean>()
        val sc = SCRepoManager.getInstance(context)
        val local = sc.users.getLocal()
        val subscribed = sc.users.getSubscribed()
        if (local.name.isNotEmpty()) {
            nameList[local.name] = sc.fromLocal { sc.workDays.hasDualShift() }
            if (subscribed != null && subscribed.name.isNotEmpty()) {
                nameList[subscribed.name] = sc.fromNet { sc.workDays.hasDualShift() } == true
            }
            for (name in nameList) {
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.item_shift, null) as FrameLayout
                val textView = view.findViewById<TextView>(R.id.shiftName)
                textView.text = name.key.substring(0, 1)
                textView.setTextColor(
                    ColorHelper.getColorAttr(
                        context,
                        androidx.appcompat.R.attr.colorPrimary
                    )
                )
                NameInfoViewClickHandler(context, textView, name.key)
                userNameRow.addView(view)

                if (name.value) {
                    val filler = inflater.inflate(R.layout.item_shift, null) as FrameLayout
                    val fillerTextView = filler.findViewById<TextView>(R.id.shiftName)
                    fillerTextView.text = " "
                    userNameRow.addView(filler)
                }

            }

        }
    }
}