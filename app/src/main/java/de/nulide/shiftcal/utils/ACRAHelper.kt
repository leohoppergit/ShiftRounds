package de.nulide.shiftcal.utils

import org.acra.ACRA

class ACRAHelper {

    companion object {

        fun sendCrash(e: Exception) {
            ACRA.errorReporter.handleException(e)
        }

        fun sendFeedback(msg: String) {
            ACRA.errorReporter.putCustomData("FEEDBACK", msg)
            ACRA.errorReporter.handleException(null)
        }

    }
}