package de.nulide.shiftcal.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import de.nulide.shiftcal.Tester
import de.nulide.shiftcal.data.legacy.model.Shift
import de.nulide.shiftcal.data.legacy.model.ShiftTime
import de.nulide.shiftcal.data.legacy.model.WorkDay
import de.nulide.shiftcal.data.legacy.repository.ShiftCalendarRepository
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.time.LocalDate
import java.time.LocalDateTime


@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SmallTest
class MainActivityTest : Tester() {

    @Test
    fun testAWelcomeIntro() {
        launch(true)
        click("Save")

        click("Start exploring ShiftSwift")

        click("21")
        click("21")
        click("21")
        click("21")
        click("21")
        click("21")
    }

    @Test
    fun testBCreateShiftAndAddShift() {
        launch(false)
        clickResource("editFab")
        clickResource("shiftSelectorFab")
        click("Create new Shift")

        var text = getRessource("shiftNameEdit")
        text.click()
        text.setText("TestShift")

        text = getRessource("shortShiftNameEdit")
        text.click()
        text.setText("TS")

        val doneButton = getRessource("fabDoneShift")
        if (!doneButton.waitForExists(1000)) {
            device.pressBack()
        }

        clickResource("fabDoneShift")

        click("13")
        click("18")
        click("21")

        clickResource("editFab")

        click("TS")

    }

    @Test
    fun testCMonthNote() {
        launch(false)
        clickResource("editFab")

        clickResource("monthNoteFab")

        var text = getRessource("$packageName:id/monthNoteEdit")
        text.setText("noti: note")

        click("OK")

        getByText("noti: note")
    }

    @Test
    fun testDDeleteShift() {
        launch(false)
        clickResource("settings")
        click("Shifts")
        val button = getByText("TS")
        button.longClick()
        click("Delete")
        click("Yes")
        device.pressBack()
        device.pressBack()
        val result = getByText("TS")
        assert(!result.waitForExists(2000))
    }

    @Test
    fun testEAlarm() {
        launch(false)
        clickResource("settings")
        click("Advanced Settings")

        device.swipe(100, 1000, 100, 0, 25)

        clickResource("alarmCheckBox")

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O_MR1) {
            for (i in 0..<2) {
                click("Grant")
                click("Allow")
            }

            click("Grant")
            click("ShiftDev")
            click("Allow display over other apps")
            device.pressBack()
            device.pressBack()
        } else {
            click("Grant")
            click("ALLOW")
            click("Grant")
            click("Allow display over other apps")
            device.pressBack()
            click("Grant")
        }
        click("OK")
        device.pressBack()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val shiftCalendar = ShiftCalendarRepository.getInstance(context)

        shiftCalendar.shifts.deleteShift(0)
        var now = LocalDateTime.now()
        now = now.plusMinutes(16)
        val shiftId = shiftCalendar.shifts.getNextShiftID()
        val newShift = Shift(
            "AlarmShift",
            "as",
            shiftId,
            ShiftTime(now.hour, now.minute),
            ShiftTime(0, 0),
            0,
            1,
            0,
            true,
            false
        )
        shiftCalendar.shifts.addShift(newShift)
        val wday = WorkDay(shiftCalendar.calendar.getNextWorkDayID(), LocalDate.now(), shiftId)
        shiftCalendar.calendar.addWDay(wday)
        shiftCalendar.increaseVersion()
        device.pressBack()

        clickResource("editFab")
        clickResource("editFab")

        device.pressHome()

        val result = getByText("Shift Alarm")
        assert(result.waitForExists(60000))
    }

}