package de.nulide.shiftcal.utils

import android.content.Context
import android.view.View
import de.nulide.shiftcal.ui.editor.list.ShiftBlockGradientBackground

class EasterEggHandler(val context: Context, val editFab: View) {

    private var easterEggLastClick = 0L
    private var easterEggClickC = 0

    fun onClick() {
        val easterEggNow = System.currentTimeMillis()
        if (easterEggLastClick + 750 > easterEggNow) {
            easterEggClickC += 1
            if (easterEggClickC > 2) {
                editFab.background = ShiftBlockGradientBackground.getRainbowBackground()
            }
        } else {
            easterEggClickC = 0
        }
        easterEggLastClick = System.currentTimeMillis()
    }

}
