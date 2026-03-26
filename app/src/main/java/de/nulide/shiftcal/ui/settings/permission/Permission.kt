package de.nulide.shiftcal.ui.settings.permission

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.nulide.shiftcal.R

abstract class Permission(val context: Context, titleId: Int) : View.OnClickListener {

    private var view: LinearLayout
    private var permText: TextView
    private var permButton: Button
    private var permGrantedIcon: ImageView

    init {
        val layoutInflater = LayoutInflater.from(context)
        view = layoutInflater.inflate(R.layout.item_perm, null) as LinearLayout
        permText = view.findViewById(R.id.permText)
        permText.text = context.getString(titleId)

        permButton = view.findViewById(R.id.permButton)
        permButton.setOnClickListener(this)

        permGrantedIcon = view.findViewById(R.id.grantendIcon)
    }


    fun getView(): LinearLayout {
        return view
    }

    override fun onClick(v: View?) {
        updatePerm()
        if (checkPerm()) {
            return
        } else {
            requestPerm()
        }
    }

    fun updatePerm() {
        if (checkPerm()) {
            permButton.visibility = View.GONE
            permGrantedIcon.visibility = View.VISIBLE
        } else {
            permButton.visibility = View.VISIBLE
            permGrantedIcon.visibility = View.GONE
        }
    }

    abstract fun requestPerm()

    abstract fun checkPerm(): Boolean

    fun onResume() {
        updatePerm()
    }
}