package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.WorkDay


class TagsSelectorDialog(
    context: Context,
    wday: WorkDay,
    val tagSelectedListener: OnTagSelectedListener
) : MaterialAlertDialogBuilder(context),
    View.OnClickListener {

    companion object {
        private val IC_MONEY = R.drawable.ic_shift_money
        private val IC_STAR = R.drawable.ic_shift_star
        private val IC_SICK = R.drawable.ic_shift_sick
        private val IC_HOME = R.drawable.ic_shift_home

        private val icList = listOf(IC_MONEY, IC_STAR, IC_SICK, IC_HOME)

        lateinit var dialog: AlertDialog

        fun getIconRes(id: Int): Int {
            return icList[id]
        }

    }

    init {
        setTitle(context.getString(R.string.tags_dialog_title))
        val inflater = LayoutInflater.from(context)
        val iconsView = inflater.inflate(R.layout.dialog_icon_selector, null) as LinearLayout

        val iconsContainer = iconsView.findViewById<GridLayout>(R.id.iconSelector)

        for (id in icList.indices) {
            iconsContainer.addView(genImageView(id, wday.icons.contains(id)))
        }

        setView(iconsView)

        dialog = show()
    }

    fun genImageView(iconID: Int, selected: Boolean): ImageView {
        val view = ImageView(context)
        view.setImageResource(icList[iconID])
        view.tag = icList[iconID]
        val paddingInDp = 8
        val scale = context.resources.displayMetrics.density
        val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
        view.setPadding(paddingInPx)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
        view.layoutParams = params
        view.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, R.color.textColor))
        view.setOnClickListener(this)
        if (selected) {
            view.background = ContextCompat.getDrawable(context, R.drawable.today_box)

        }
        return view
    }

    override fun onClick(v: View?) {
        val iconRes = (v as ImageView).tag
        val iconID = icList.indexOf(iconRes)
        tagSelectedListener.onTagSelected(iconID)

        dialog.dismiss()
    }

}