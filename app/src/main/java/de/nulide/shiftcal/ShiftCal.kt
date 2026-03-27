package de.nulide.shiftcal

import android.app.Application
import com.google.android.material.color.DynamicColors
import de.nulide.shiftcal.data.migration.DBMigrator

class ShiftCal : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        val dbMigrator = DBMigrator(this)
        dbMigrator.migrate()

    }
}
