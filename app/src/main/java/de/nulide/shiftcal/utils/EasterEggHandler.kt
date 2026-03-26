package de.nulide.shiftcal.utils

import android.content.Context
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import de.nulide.shiftcal.ui.editor.list.ShiftBlockGradientBackground

class EasterEggHandler(val context: Context, val editFab: ExtendedFloatingActionButton) {

    private var easterEggLastClick = 0L
    private var easterEggClickC = 0

    fun onClick() {
        val easterEggNow = System.currentTimeMillis()
        if (easterEggLastClick + 750 > easterEggNow) {
            easterEggClickC += 1
            if (easterEggClickC > 2) {
                editFab.backgroundTintList = null
                editFab.background = ShiftBlockGradientBackground.getRainbowBackground()
            }
        } else {
            easterEggClickC = 0
        }
        easterEggLastClick = System.currentTimeMillis()
    }

}