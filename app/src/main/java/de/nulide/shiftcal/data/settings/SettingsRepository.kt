package de.nulide.shiftcal.data.settings

import android.content.Context
import de.nulide.shiftcal.data.factory.JIO
import de.nulide.shiftcal.utils.SingletonHolder
import java.io.File

const val SETTINGS_FILENAME = "settings.json"

class SettingsRepository private constructor(private val context: Context) {

    companion object :
        SingletonHolder<SettingsRepository, Context>(::SettingsRepository) {
    }

    private var settings: Settings

    init {
        settings = JIO.readAsJSON(context, Settings::class.java, SETTINGS_FILENAME)
    }

    fun factoryResetSettings() {
        settings = Settings()
        saveSettings()
    }

    private fun saveSettings() {
        save()
    }

    private fun save() {
        val file = File(context.filesDir, SETTINGS_FILENAME)
        JIO.saveAsJSON(settings, file)
    }

    fun <T> set(key: String, value: T) {
        settings.setSetting(key, value.toString())
        saveSettings()
    }

    fun get(key: String): String {
        return settings.getSetting(key)
    }

    fun has(key: String): Boolean {
        return settings.isAvailable(key)
    }

    fun getBoolean(key: String): Boolean {
        return settings.getSetting(key).toBoolean()
    }

    fun getInt(key: String): Int {
        return settings.getSetting(key).toInt()
    }

    fun getLong(key: String): Long {
        return settings.getSetting(key).toLong()
    }

    fun reset(key: String) {
        settings.removeSetting(key)
        saveSettings()
    }

    fun resetSharingAccount() {
        reset(Settings.SERVER_SYNC_UUID)
        reset(Settings.SERVER_SYNC_HPW)
        reset(Settings.SERVER_SYNC_PW)
        reset(Settings.FAMILY_SYNC_LAST_UPLOAD_FAILED)
    }

    fun exportSettings(): Map<String, String> {
        return settings.settings.toMap()
    }

    fun importSettings(importedSettings: Map<String, String>) {
        settings.settings = HashMap(importedSettings)
        saveSettings()
    }
}
