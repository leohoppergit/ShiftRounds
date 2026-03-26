package de.nulide.shiftcal.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

class TabletHelper {

    companion object {

        fun isTablet(context: Context): Boolean {
            val screenLayout = context.resources.configuration.screenLayout
            return (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }

        @SuppressLint("SourceLockedOrientationActivity")
        fun lockScreenOrientation(activity: AppCompatActivity) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        @SuppressLint("SourceLockedOrientationActivity")
        fun unlockScreenOrientation(activity: AppCompatActivity) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        fun orientationSetup(activity: AppCompatActivity) {
            if (isTablet(activity)) {
                unlockScreenOrientation(activity)
            } else {
                lockScreenOrientation(activity)
            }
        }
    }

}