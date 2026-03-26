package de.nulide.shiftcal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import de.nulide.shiftcal.data.legacy.repository.ShiftCalendarRepository
import de.nulide.shiftcal.data.settings.SettingsRepository
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Assert.assertThat

open class Tester {

    lateinit var device: UiDevice
    val packageName = "de.nulide.shiftcal.play.dev"

    fun launch(reset: Boolean = false) {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()

        val launcherPackage = getLauncherPackageName()
        assertThat(launcherPackage, notNullValue())
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000)

        // Open the app
        val context = getApplicationContext<Context>()

        if (reset) {
            val settings = SettingsRepository.getInstance(context)
            settings.factoryResetSettings()
            val shiftCalendar = ShiftCalendarRepository.getInstance(context)
            shiftCalendar.resetCalendar()
        }

        val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5000)
    }

    private fun getLauncherPackageName(): String {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)

        val pm = getApplicationContext<Context>().packageManager
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo!!.activityInfo.packageName
    }

    fun click(name: String) {
        val button = device.findObject(UiSelector().text(name))
        button.click()
    }

    fun clickResource(name: String) {
        val button = device.findObject(UiSelector().resourceId("$packageName:id/$name"))
        button.click()
    }

    fun getRessource(name: String): UiObject {
        val item = device.findObject(UiSelector().resourceId("$packageName:id/$name"))
        return item
    }

    fun getByText(name: String): UiObject {
        val item = device.findObject(UiSelector().text(name))
        return item
    }
}