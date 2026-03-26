package de.nulide.shiftcal

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import de.nulide.shiftcal.data.migration.DBMigrator
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.utils.NotificationHandler
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class ShiftCal : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        NotificationHandler.initNotificationChannels(this)

        val dbMigrator = DBMigrator(this)
        dbMigrator.migrate()

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        if (BuildConfig.CRASHLYTICS_ENABLED) {
            val settings = SettingsRepository.getInstance(this)

            initAcra {
                buildConfigClass = BuildConfig::class.java
                reportFormat = StringFormat.JSON

                httpSender {
                    enabled = settings.getBoolean(Settings.CRASHLYTICS)
                    uri = "https://acra.nulide.de/report"
                    httpMethod = HttpSender.Method.POST
                    connectionTimeout = 5000
                    socketTimeout = 20000
                    dropReportsOnTimeout = false
                }
            }
        }
    }
}