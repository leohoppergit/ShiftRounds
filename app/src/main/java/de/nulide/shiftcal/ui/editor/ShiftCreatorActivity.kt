package de.nulide.shiftcal.ui.editor

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RadioGroup
import android.widget.CompoundButton
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.model.Shift
import de.nulide.shiftcal.data.model.ShiftTime
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.databinding.ActivityShiftCreatorBinding
import de.nulide.shiftcal.ui.helper.SpecialShifts
import de.nulide.shiftcal.ui.helper.WarningDialog
import de.nulide.shiftcal.utils.ColorHelper
import de.nulide.shiftcal.utils.Snack

class ShiftCreatorActivity : AppCompatActivity(), View.OnClickListener, TextWatcher,
    CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    companion object {
        private const val TIME_PICKER_START_TIME_TAG = "start"
        private const val TIME_PICKER_END_TIME_TAG = "end"
        private val DEFAULT_SHIFT_COLORS = listOf(
            "#E53935".toColorInt(),
            "#FB8C00".toColorInt(),
            "#FDD835".toColorInt(),
            "#43A047".toColorInt(),
            "#1E88E5".toColorInt(),
            "#8E24AA".toColorInt(),
            "#00897B".toColorInt(),
            "#6D4C41".toColorInt(),
            "#3949AB".toColorInt(),
            "#D81B60".toColorInt()
        )

        const val SHIFT_ID_TAG = "toedit"
    }

    private lateinit var sc: SCRepoManager

    private var toEditShiftId = SpecialShifts.NONE_ID
    private lateinit var shiftStartTime: ShiftTime
    private lateinit var shiftEndTime: ShiftTime
    private var shiftEndDayOffset = 0

    private var archived = false

    private var shiftColor: Int = -1
    private var selectedBalanceType = 0

    private lateinit var binding: ActivityShiftCreatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        binding = ActivityShiftCreatorBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.shiftCreator)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sc = SCRepoManager.getInstance(this)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            toEditShiftId = bundle.getInt(SHIFT_ID_TAG)
        } else {
            finish()
        }

        binding.fabDoneShift.setOnClickListener(this)

        binding.shortShiftNameEdit.addTextChangedListener(this)
        binding.breakMinutesEdit.addTextChangedListener(this)
        binding.balanceHoursEdit.addTextChangedListener(this)
        binding.balanceMinutesEdit.addTextChangedListener(this)

        binding.startTimeButton.setOnClickListener(this)

        binding.endTimeButton.setOnClickListener(this)
        binding.endDayOffsetGroup.setOnCheckedChangeListener(this)
        binding.balanceTypeGroup.setOnCheckedChangeListener(this)

        shiftStartTime = ShiftTime(8, 0)
        shiftEndTime = ShiftTime(16, 0)
        updateTime()

        binding.colorPickerButton.setOnClickListener(this)

        updateButtonColors(getInitialShiftColor())

        if (toEditShiftId != SpecialShifts.NONE_ID) {
            val toEditShift = sc.shifts.get(toEditShiftId)
            binding.shiftNameEdit.setText(toEditShift.name)
            binding.shortShiftNameEdit.setText(toEditShift.shortName)
            shiftStartTime = toEditShift.startTime
            shiftEndTime = toEditShift.endTime
            shiftEndDayOffset = toEditShift.endDayOffset
            if (toEditShift.customBalanceMinutes != null) {
                val customBalanceMinutes = kotlin.math.abs(toEditShift.customBalanceMinutes)
                selectedBalanceType = if (toEditShift.customBalanceMinutes < 0) 2 else 1
                val hours = customBalanceMinutes / 60
                val minutes = customBalanceMinutes % 60
                if (hours > 0) {
                    binding.balanceHoursEdit.setText(hours.toString())
                }
                if (minutes > 0) {
                    binding.balanceMinutesEdit.setText(minutes.toString())
                }
            } else {
                selectedBalanceType = 0
            }
            binding.breakMinutesEdit.setText(
                getString(
                    R.string.numberformat,
                    toEditShift.breakMinutes
                )
            )
            archived = toEditShift.archived
            updateButtonColors(toEditShift.color)
            updateTime()
            updateEditText()
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val warning = WarningDialog(
                    this@ShiftCreatorActivity, WarningDialog.ICON_WARNING,
                    this@ShiftCreatorActivity.getString(R.string.warning_shift_not_saved)
                )
                warning.enableSaveButton { onClick(binding.fabDoneShift) }
                warning.enablePositiveButton {
                    this.isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
                warning.enableNegativeButton()

                if (toEditShiftId != SpecialShifts.NONE_ID) {
                    val newShift = genShift()
                    val oldShift = sc.shifts.get(toEditShiftId)
                    if (newShift != oldShift) {
                        warning.show()
                    } else {
                        this.isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                } else {
                    warning.show()
                }
            }
        }
        onBackPressedDispatcher.addCallback(backCallback)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.fabDoneShift -> {
                val newShift = genShift()

                if (newShift.name.isNotEmpty() && newShift.shortName.isNotEmpty() && newShift.durationMinutes > 0) {
                    if (toEditShiftId != SpecialShifts.NONE_ID) {
                        sc.shifts.update(newShift)
                    } else {
                        sc.shifts.add(newShift)
                    }
                    this.finish()
                } else {
                    val msg = if (newShift.durationMinutes <= 0) {
                        getString(R.string.shift_creator_error_invalid_duration)
                    } else {
                        getString(R.string.shift_creator_error_information)
                    }
                    Snack.not(view, msg)

                    handleWarnEmptyEdit(newShift.name, binding.shiftNameEdit)
                    handleWarnEmptyEdit(newShift.shortName, binding.shortShiftNameEdit)
                }
            }

            binding.startTimeButton -> {
                showTimePicker(shiftStartTime, TIME_PICKER_START_TIME_TAG)
            }

            binding.endTimeButton -> {
                showTimePicker(shiftEndTime, TIME_PICKER_END_TIME_TAG)
            }

            binding.colorPickerButton -> {
                ColorPickerDialogBuilder
                    .with(this)
                    .showAlphaSlider(false)
                    .showLightnessSlider(true)
                    .density(12)
                    .setTitle(R.string.shift_creator_color_picker_title)
                    .setPositiveButton(R.string.ok)
                    { _: DialogInterface?, lastSelectedColor: Int, _: Array<Int?>? ->
                        updateButtonColors(lastSelectedColor)
                    }
                    .initialColor(shiftColor)
                    .build().show()
            }

        }
    }

    private fun genShift(): Shift {
        val name: String = binding.shiftNameEdit.text.toString()
        val shortName: String = binding.shortShiftNameEdit.text.toString()
        var shiftID = toEditShiftId
        if (toEditShiftId == SpecialShifts.NONE_ID) {
            shiftID = sc.shifts.getNextId()
        }
        val sortOrder = if (toEditShiftId == SpecialShifts.NONE_ID) {
            sc.shifts.getAll().count { it.id >= 0 }
        } else {
            sc.shifts.get(toEditShiftId).sortOrder
        }
        val calId = sc.curCalId
        var breakMinutes = 0
        if (binding.breakMinutesEdit.text.toString().isNotEmpty()) {
            breakMinutes = binding.breakMinutesEdit.text.toString().toInt()
        }
        val balanceHours = binding.balanceHoursEdit.text.toString().toIntOrNull() ?: 0
        val balanceMinutes = binding.balanceMinutesEdit.text.toString().toIntOrNull() ?: 0
        val customBalanceMinutes = when (selectedBalanceType) {
            1 -> balanceHours * 60 + balanceMinutes
            2 -> -(balanceHours * 60 + balanceMinutes)
            else -> null
        }
        val nS = Shift(
            shiftID,
            calId,
            name,
            shortName,
            shiftStartTime,
            shiftEndTime,
            shiftEndDayOffset,
            customBalanceMinutes,
            sortOrder,
            breakMinutes,
            null,
            shiftColor,
            false,
            archived
        )
        return nS
    }

    private fun getInitialShiftColor(): Int {
        if (toEditShiftId != SpecialShifts.NONE_ID) {
            return sc.shifts.get(toEditShiftId).color
        }

        val usedColors = sc.shifts.getAll().map { it.color }.toSet()
        val nextPaletteColor = DEFAULT_SHIFT_COLORS.firstOrNull { it !in usedColors }
        if (nextPaletteColor != null) {
            return nextPaletteColor
        }

        val hue = ((sc.shifts.getAll().size * 37) % 360).toFloat()
        return Color.HSVToColor(floatArrayOf(hue, 0.72f, 0.88f))
    }

    private fun handleWarnEmptyEdit(text: String, edit: EditText) {
        if (text.isEmpty()) {
            edit.background.setTint(Color.RED)
        } else {
            edit.background.setTint(getColor(R.color.textColor))
        }
    }

    private fun showTimePicker(shiftTime: ShiftTime, id: String) {
        val hourOfDay: Int = shiftTime.hour
        val minuteOfDay: Int = shiftTime.minute
        val timePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(hourOfDay)
            .setMinute(minuteOfDay)
            .build()
        timePicker.addOnPositiveButtonClickListener({
            when (id) {
                TIME_PICKER_START_TIME_TAG -> {
                    shiftStartTime = ShiftTime(timePicker.hour, timePicker.minute)
                }

                TIME_PICKER_END_TIME_TAG -> {
                    shiftEndTime = ShiftTime(timePicker.hour, timePicker.minute)
                }

            }
            updateTime()
        })
        timePicker.show(this.supportFragmentManager, id)
    }

    private fun updateTime() {
        binding.startTimeButton.text = shiftStartTime.toString()
        binding.endTimeButton.text = shiftEndTime.toString()
        binding.endDayOffsetGroup.check(
            if (shiftEndDayOffset == 0) binding.endSameDayOption.id else binding.endNextDayOption.id
        )
        binding.balanceTypeGroup.check(
            when (selectedBalanceType) {
                1 -> binding.balancePositiveOption.id
                2 -> binding.balanceNegativeOption.id
                else -> binding.balanceDefaultOption.id
            }
        )
        val useCustomBalance = selectedBalanceType != 0
        binding.balanceHoursEdit.isEnabled = useCustomBalance
        binding.balanceMinutesEdit.isEnabled = useCustomBalance
        binding.breakMinutesEdit.isEnabled = !useCustomBalance
        binding.balanceInfoText.visibility = if (useCustomBalance) View.VISIBLE else View.GONE
        binding.balanceHoursLabel.visibility = if (useCustomBalance) View.VISIBLE else View.GONE
        binding.balanceHoursEdit.visibility = if (useCustomBalance) View.VISIBLE else View.GONE
        binding.balanceMinutesLabel.visibility = if (useCustomBalance) View.VISIBLE else View.GONE
        binding.balanceMinutesEdit.visibility = if (useCustomBalance) View.VISIBLE else View.GONE
        val durationMinutes = Shift(
            0,
            0,
            "",
            "",
            shiftStartTime,
            shiftEndTime,
            shiftEndDayOffset,
            null,
            0,
            0,
            null,
            0,
            false,
            false
        ).durationMinutes
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        binding.durationValueText.text = getString(R.string.time_stat, hours, minutes)
        val balancePreviewMinutes = if (useCustomBalance) {
            val balanceHours = binding.balanceHoursEdit.text.toString().toIntOrNull() ?: 0
            val balanceMinutes = binding.balanceMinutesEdit.text.toString().toIntOrNull() ?: 0
            val absMinutes = (balanceHours * 60) + balanceMinutes
            if (selectedBalanceType == 2) -absMinutes else absMinutes
        } else {
            durationMinutes - (binding.breakMinutesEdit.text.toString().toIntOrNull() ?: 0)
        }
        val isNegative = balancePreviewMinutes < 0
        val absPreview = kotlin.math.abs(balancePreviewMinutes)
        binding.balancePreviewText.text = getString(
            R.string.time_stat_signed,
            if (isNegative) "-" else "",
            absPreview / 60,
            absPreview % 60
        )
    }

    private fun updateEditText() {
        if (binding.shortShiftNameEdit.text.toString().length > 4) {
            binding.shortShiftNameEdit.background.setTint(Color.YELLOW)
            binding.shortNameInfoView.visibility = View.VISIBLE
        } else {
            binding.shortShiftNameEdit.background.setTint(getColor(R.color.textColor))
            binding.shortNameInfoView.visibility = View.GONE
        }
    }

    private fun updateButtonColors(color: Int) {
        binding.colorPickerButton.setBackgroundColor(color)
        binding.colorPickerButton.text = String.format("#%06X", (0xFFFFFF and color))
        shiftColor = color

        if (ColorHelper.isTooBright(color)) {
            binding.colorPickerButton.setTextColor(Color.BLACK)
        } else {
            binding.colorPickerButton.setTextColor(Color.WHITE)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        updateEditText()
        updateTime()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        // Alarm options were removed from the editor.
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (group == binding.endDayOffsetGroup) {
            shiftEndDayOffset = if (checkedId == binding.endNextDayOption.id) 1 else 0
            updateTime()
        } else if (group == binding.balanceTypeGroup) {
            selectedBalanceType = when (checkedId) {
                binding.balancePositiveOption.id -> 1
                binding.balanceNegativeOption.id -> 2
                else -> 0
            }
            updateTime()
        }
    }

}
