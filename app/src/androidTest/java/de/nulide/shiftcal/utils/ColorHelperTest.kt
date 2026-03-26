package de.nulide.shiftcal.utils

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorHelperTest {

    @Test
    fun testDarkenColor() {
        var test1 = Color.HSVToColor(floatArrayOf(1.0f, 1.0f, 1.0f))
        var test2 = Color.HSVToColor(floatArrayOf(1.0f, 0.0f, 0.2f))

        var expected1 = Color.HSVToColor(floatArrayOf(1.0f, 1.0f, 0.8f))
        var expected2 = Color.HSVToColor(floatArrayOf(1.0f, 0.2f, 0.0f))

        assertEquals(expected1, ColorHelper.darkenColor(test1))
        assertEquals(expected2, ColorHelper.darkenColor(test2))
    }

    @Test
    fun testBrightenColor() {
        var test1 = Color.HSVToColor(floatArrayOf(1.0f, 1.0f, 0.2f))
        var test2 = Color.HSVToColor(floatArrayOf(1.0f, 1.0f, 0.98f))

        var expected1 = Color.HSVToColor(floatArrayOf(1.0f, 1.0f, 0.4f))
        var expected2 = Color.HSVToColor(floatArrayOf(1.0f, 0.8f, 1.0f))

        assertEquals(expected1, ColorHelper.brightenColor(test1))
        assertEquals(expected2, ColorHelper.brightenColor(test2))
    }
}