package de.nulide.shiftcal.ui.calendar.comp.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.WorkDayDTO
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.ExpandableLinearLayout
import de.nulide.shiftcal.ui.helper.ExpandableLinearLayout.OnExpandableLinearLayoutCollapsedListener
import de.nulide.shiftcal.ui.helper.OnTagSelectedListener
import de.nulide.shiftcal.ui.helper.TagsSelectorDialog
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Runner
import java.time.LocalDate
import java.util.Locale

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
    private val overtimeMultiplierText: TextView = itemView.findViewById(R.id.overtimeMultiplierText)
    private val noteText: TextView = itemView.findViewById(R.id.noteText)
    private val iconHolder: GridLayout = itemView.findViewById(R.id.iconHolder)

    private var initialized = false

    lateinit var wday: WorkDayDTO
    var pos = -1

    fun bind(wday: WorkDayDTO, position: Int) {
        Runner.runCo {
            this.pos = position
            shiftInfoBox.setOnExpandableLinerLayoutCollapsedListener(this)
            this.wday = wday
            val shift = wday.shift
            val isEditable = wday.wday.calendarId == sc.calendar.getLocal()

            val newBackground =
                ContextCompat.getDrawable(context, R.drawable.shift_rounds_shift_card)?.mutate()
            newBackground?.setTint(shift.color)
            shiftInfoBox.background = newBackground

            val today = LocalDate.now()
            val workDay = wday.wday.day
            val isToday = (today == workDay)

            shiftNameText.text = shift.name

            if (isToday && wday.isRunning) {
                val layer = arrayOfNulls<Drawable>(2)
                val secondBackground =
                    ContextCompat.getDrawable(context, R.drawable.shift_rounds_loading_box)?.mutate()
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
                overtimeMultiplierText.setTextColor(black)
                noteText.setTextColor(black)
                overtimeText.background = createInfoBackground(true)
                balanceTypeText.background = createInfoBackground(true)
                overtimeMultiplierText.background = createInfoBackground(true)
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
                overtimeMultiplierText.setTextColor(white)
                noteText.setTextColor(white)
                overtimeText.background = createInfoBackground(false)
                balanceTypeText.background = createInfoBackground(false)
                overtimeMultiplierText.background = createInfoBackground(false)
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

            val displayedOvertimeMinutes = shift.adjustedOvertimeMinutes(wday.wday.overtimeMinutes)
            if (displayedOvertimeMinutes > 0) {
                overtimeText.visibility = View.VISIBLE
                overtimeText.text = context.getString(
                    R.string.work_day_details_overtime_value,
                    displayedOvertimeMinutes / 60,
                    displayedOvertimeMinutes % 60
                )
            } else if (displayedOvertimeMinutes < 0) {
                overtimeText.visibility = View.VISIBLE
                val absoluteMinutes = kotlin.math.abs(displayedOvertimeMinutes)
                overtimeText.text = context.getString(
                    R.string.work_day_details_less_work_value,
                    absoluteMinutes / 60,
                    absoluteMinutes % 60
                )
            } else {
                overtimeText.visibility = View.GONE
            }

            if (shift.overtimeMultiplier != 1.0) {
                overtimeMultiplierText.visibility = View.VISIBLE
                overtimeMultiplierText.text = context.getString(
                    R.string.work_day_details_multiplier_badge,
                    formatMultiplier(shift.overtimeMultiplier)
                )
            } else {
                overtimeMultiplierText.visibility = View.GONE
            }

            if (shift.specialAccountId != null && shift.specialAccountMinutes != null) {
                val account = SettingsRepository.getInstance(context).getSpecialAccount(shift.specialAccountId)
                val absoluteMinutes = kotlin.math.abs(shift.specialAccountMinutes)
                balanceTypeText.visibility = View.VISIBLE
                balanceTypeText.text = context.getString(
                    R.string.special_account_balance_value,
                    account?.name ?: context.getString(R.string.special_account_missing_name),
                    if (shift.specialAccountMinutes < 0) "-" else "",
                    absoluteMinutes / 60,
                    absoluteMinutes % 60
                )
            } else if (shift.customBalanceMinutes != null) {
                if (shift.customBalanceMinutes < 0) {
                    balanceTypeText.visibility = View.VISIBLE
                    context.getString(R.string.shift_balance_type_negative)
                } else {
                    balanceTypeText.visibility = View.GONE
                    ""
                }
            } else {
                balanceTypeText.visibility = View.GONE
            }

            if (wday.wday.note.isNotBlank()) {
                noteText.visibility = View.VISIBLE
                noteText.text = context.getString(R.string.work_day_details_note_value, wday.wday.note)
                noteText.setOnClickListener {
                    showNoteDialog(isEditable)
                }
            } else {
                noteText.visibility = View.GONE
                noteText.setOnClickListener(null)
            }

            if (isEditable) {
                shiftInfoBox.setOnClickListener {
                    WorkDayDetailsDialog(context, wday.shift, wday.wday) { updatedWorkDay ->
                        sc.workDays.update(updatedWorkDay)
                        shiftInfoBoxAdapter.notifyItemChanged(pos)
                    }.show()
                }
            } else {
                shiftInfoBox.setOnClickListener(null)
            }

            shiftInfoBox.expandNow()
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
            cornerRadius = 28f
            setColor(
                ContextCompat.getColor(
                    context,
                    if (forBrightShift) android.R.color.white else android.R.color.black
                )
            )
            alpha = if (forBrightShift) 158 else 72
        }
    }

    private fun formatMultiplier(value: Double): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.1f", value)
        } else {
            value.toString()
        }
    }

    private fun showNoteDialog(isEditable: Boolean) {
        val noteEdit = EditText(context).apply {
            setText(wday.wday.note)
            minLines = 6
            maxLines = 12
            setPadding(36, 28, 36, 28)
            isSingleLine = false
            isFocusable = isEditable
            isFocusableInTouchMode = isEditable
            isClickable = isEditable
            if (!isEditable) {
                keyListener = null
            }
        }

        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.work_day_note_dialog_title)
            .setView(noteEdit)
            .setNegativeButton(android.R.string.cancel, null)

        if (isEditable) {
            builder.setPositiveButton(R.string.save) { _, _ ->
                wday.wday.note = noteEdit.text.toString().trim()
                sc.workDays.update(wday.wday)
                shiftInfoBoxAdapter.notifyItemChanged(pos)
            }
        }

        builder.show()
    }

}
