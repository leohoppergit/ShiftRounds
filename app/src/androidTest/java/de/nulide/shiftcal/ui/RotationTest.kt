package de.nulide.shiftcal.ui

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import de.nulide.shiftcal.Tester
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SmallTest
class RotationTest : Tester() {

    @Test
    fun testRotation() {
        launch(false)
        device.setOrientationPortrait()

        device.setOrientationLandscape()

        device.setOrientationPortrait()

        click("Save")
    }

}