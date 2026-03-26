package de.nulide.shiftcal.ui.editor.list

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.toColorInt
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.repository.wrapper.ShiftBlockDTO
import de.nulide.shiftcal.utils.Runner

class ShiftBlockGradientBackground {

    companion object {
        fun getBackground(context: Context, shiftBlock: ShiftBlockDTO): GradientDrawable {
            val sc = SCRepoManager.getInstance(context)
            val colors = mutableListOf<Int>()

            Runner.runBlocked {
                for (shift in shiftBlock.entries) {
                    val shift = sc.shifts.get(shift.shiftId)
                    for (i in 0..20) {
                        colors.add(shift.color)
                    }
                    colors.add(shift.color)
                }
            }
            return getGradientDrawableFromColors(colors)
        }

        fun getRainbowBackground(): GradientDrawable {
            val baseColors = mutableListOf<Int>()
            val colors = mutableListOf<Int>()
            baseColors.add("#E53935".toColorInt())
            baseColors.add("#FB8C00".toColorInt())
            baseColors.add("#FDD835".toColorInt())
            baseColors.add("#43A047".toColorInt())
            baseColors.add("#1E88E5".toColorInt())
            baseColors.add("#8E24AA".toColorInt())

            for (color in baseColors) {
                for (i in 0..20) {
                    colors.add(color)
                }
            }
            return getGradientDrawableFromColors(colors)
        }

        private fun getGradientDrawableFromColors(colors: MutableList<Int>): GradientDrawable {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors.toIntArray()
            )
            gradientDrawable.cornerRadius = 32f
            return gradientDrawable
        }
    }
}