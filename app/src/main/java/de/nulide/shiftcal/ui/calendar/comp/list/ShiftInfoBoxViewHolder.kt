package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.ExpandableLinearLayout
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.OnExpandableLinearLayoutCollapsedListener
import de.nulide.shiftcal.ui.helper.OnTagSelectedListener
import de.nulide.shiftcal.ui.helper.TagsSelectorDialog
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Runner
import java.time.LocalDate

class ShiftInfoBoxViewHolder(
    val sc: SCRepoManager,
    val context: Context,
    itemView: View,
    val shiftInfoBoxAdapter: ShiftInfoBoxAdapter
) : RecyclerView.ViewHolder(itemView),
    OnExpandableLinearLayoutCollapsedListener, View.OnClickListener, OnTagSelectedListener {

    private val shiftInfoBox: ExpandableLinearLayout = itemView.findViewById(R.id.shiftInfoBox)
    private val shiftNameText: TextView = itemView.findViewById(R.id.shiftNameText)
    private val startTimeLabel: TextView = itemView.findViewById(R.id.startTimeLabel)
    private val startTimeText: TextView = itemView.findViewById(R.id.startTimeText)
    private val endTimeLabel: TextView = itemView.findViewById(R.id.endTimeLabel)
    private val endTimeText: TextView = itemView.findViewById(R.id.endTimeText)
    private val overtimeText: TextView = itemView.findViewById(R.id.overtimeText)
    private val balanceTypeText: TextView = itemView.findViewById(R.id.balanceTypeText)
    private val noteText: TextView = itemView.findViewById(R.id.noteText)
    private val iconHolder: GridLayout = itemView.findViewById(R.id.iconHolder)

    private var initialized = false

    lateinit var wday: WorkDayDTO
    var pos = -1

    fun bind(wday: WorkDayDTO, position: Int, isUpdate: Boolean) {
        Runner.runCo {
            this.pos = position
            shiftInfoBox.setOnExpandableLinerLayoutCollapsedListener(this)
            this.wday = wday
            val shift = wday.shift
            val isEditable = wday.wday.calendarId == sc.calendar.getLocal()

            val newBackground =
                ContextCompat.getDrawable(context, R.drawable.calendar_box)?.mutate()
            newBackground?.setTint(shift.color)
            shiftInfoBox.background = newBackground

            val today = LocalDate.now()
            val workDay = wday.wday.day
            val isToday = (today == workDay)

            shiftNameText.text = shift.name

            if (isToday && wday.isRunning) {
                val layer = arrayOfNulls<Drawable>(2)
                val secondBackground =
                    ContextCompat.getDrawable(context, R.drawable.loading_box)?.mutate()
                if (ColorHelper.isTooBright(shift.color)) {
                    newBackground?.setTint(ColorHelper.brightenColor(shift.color))
                    secondBackground?.setTint(shift.color)
                } else {
                    secondBackground?.setTint(ColorHelper.darkenColor(shift.color))
                }
                secondBackground?.setLevel((wday.amountFinished * 10000).toInt())
                layer[0] = newBackground
                layer[1] = secondBackground
                shiftInfoBox.background = LayerDrawable(layer)
            }
            iconHolder.removeAllViewsInLayout()
            if (ColorHelper.isTooBright(shift.color)) {
                val black = ContextCompat.getColor(context, R.color.textColorBlack)
                shiftNameText.setTextColor(black)
                startTimeText.setTextColor(black)
                endTimeText.setTextColor(black)
                startTimeLabel.setTextColor(black)
                endTimeLabel.setTextColor(black)
                overtimeText.setTextColor(black)
                balanceTypeText.setTextColor(black)
                noteText.setTextColor(black)
                overtimeText.background = createInfoBackground(true)
                balanceTypeText.background = createInfoBackground(true)
                noteText.background = createInfoBackground(true)
                addIcons(black)
            } else {
                val white = ContextCompat.getColor(context, R.color.textColorWhite)
                shiftNameText.setTextColor(white)
                startTimeText.setTextColor(white)
                endTimeText.setTextColor(white)
                startTimeLabel.setTextColor(white)
                endTimeLabel.setTextColor(white)
                overtimeText.setTextColor(white)
                balanceTypeText.setTextColor(white)
                noteText.setTextColor(white)
                overtimeText.background = createInfoBackground(false)
                balanceTypeText.background = createInfoBackground(false)
                noteText.background = createInfoBackground(false)
                addIcons(white)
            }
            startTimeText.text = shift.startTime.toString()
            val endTimeTextBuilder = StringBuilder(shift.endTime.toString())
            if (shift.endDayOffset > 0) {
                endTimeTextBuilder.append(" ")
                    .append(context.getString(R.string.shift_creator_end_day_offset_short, shift.endDayOffset))
            }
            endTimeText.text = endTimeTextBuilder.toString()

            if (wday.wday.overtimeMinutes > 0) {
                overtimeText.visibility = View.VISIBLE
                overtimeText.text = context.getString(
                    R.string.work_day_details_overtime_value,
                    wday.wday.overtimeMinutes / 60,
                    wday.wday.overtimeMinutes % 60
                )
            } else if (wday.wday.overtimeMinutes < 0) {
                overtimeText.visibility = View.VISIBLE
                val absoluteMinutes = kotlin.math.abs(wday.wday.overtimeMinutes)
                overtimeText.text = context.getString(
                    R.string.work_day_details_less_work_value,
                    absoluteMinutes / 60,
                    absoluteMinutes % 60
                )
            } else {
                overtimeText.visibility = View.GONE
            }

            if (shift.customBalanceMinutes != null) {
                balanceTypeText.visibility = View.VISIBLE
                balanceTypeText.text = if (shift.customBalanceMinutes < 0) {
                    context.getString(R.string.shift_balance_type_negative)
                } else {
                    context.getString(R.string.shift_balance_type_positive)
                }
            } else {
                balanceTypeText.visibility = View.GONE
            }

            if (wday.wday.note.isNotBlank()) {
                noteText.visibility = View.VISIBLE
                noteText.text = context.getString(R.string.work_day_details_note_value, wday.wday.note)
            } else {
                noteText.visibility = View.GONE
            }

            if (isEditable) {
                shiftInfoBox.setOnClickListener {
                    WorkDayDetailsDialog(context, wday.wday) { updatedWorkDay ->
                        sc.workDays.update(updatedWorkDay)
                        shiftInfoBoxAdapter.notifyItemChanged(pos)
                    }.show()
                }
            } else {
                shiftInfoBox.setOnClickListener(null)
            }

            if (isUpdate) {
                shiftInfoBox.expandNow()
            } else {
                shiftInfoBox.expand()
            }
            initialized = true
        }
    }

    fun addIcons(color: Int) {
        if (wday.wday.calendarId == sc.calendar.getLocal()) {
            iconHolder.setOnClickListener(this)
        } else {
            iconHolder.setOnClickListener(null)
        }
        if (wday.wday.icons.isNotEmpty()) {
            for (iconIDs in wday.wday.icons) {
                val imageView = ImageView(context)
                imageView.setImageResource(TagsSelectorDialog.getIconRes(iconIDs))
                imageView.imageTintList = ColorStateList.valueOf(color)
                iconHolder.addView(imageView)
            }
        } else if (wday.wday.calendarId == sc.calendar.getLocal()) {
            val imageView = ImageView(context)
            imageView.setImageResource(R.drawable.ic_add)
            imageView.imageTintList = ColorStateList.valueOf(color)
            iconHolder.addView(imageView)
        }
    }

    override fun onCollapsed(expandableLinearLayout: ExpandableLinearLayout) {
        initialized = false
        shiftInfoBoxAdapter.removeItem(wday, pos)
    }

    fun dismiss() {
        shiftInfoBox.collapse()
    }

    override fun onClick(v: View?) {
        TagsSelectorDialog(context, wday.wday, this)
    }

    override fun onTagSelected(tagID: Int) {
        if (wday.wday.icons.contains(tagID)) {
            wday.wday.icons.remove(tagID)
        } else {
            wday.wday.icons.add(tagID)
        }
        sc.workDays.update(wday.wday)
        shiftInfoBoxAdapter.notifyItemChanged(pos)
    }

    private fun createInfoBackground(forBrightShift: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = 24f
            setColor(
                ContextCompat.getColor(
                    context,
                    if (forBrightShift) android.R.color.white else android.R.color.black
                )
            )
            alpha = if (forBrightShift) 150 else 95
        }
    }

}
