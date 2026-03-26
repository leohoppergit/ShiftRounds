package de.nulide.shiftcal.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

class Snack {

    companion object {
        fun not(view: View, msg: String) {
            Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }
    }
}