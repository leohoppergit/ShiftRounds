package de.nulide.shiftcal.utils

import android.os.Handler
import android.os.Looper

class Runner {

    companion object {
        fun run(runOnUi: () -> Unit) {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                runOnUi()
            }
        }

        fun runLate(delay: Long = 500, runOnUi: () -> Unit) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(runOnUi, delay)
        }


        fun runCo(run: () -> Unit) {
            run()
        }

        fun runBlocked(run: () -> Unit) {
            run()
        }
    }
}