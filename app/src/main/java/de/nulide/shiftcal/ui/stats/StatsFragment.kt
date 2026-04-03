package de.nulide.shiftcal.ui.stats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendar.core.daysOfWeek
import de.nulide.shiftcal.R
import de.nulide.shiftcal.data.repository.SCRepoManager
import de.nulide.shiftcal.data.settings.Settings
import de.nulide.shiftcal.data.settings.SettingsRepository
import de.nulide.shiftcal.databinding.FragmentStatsBinding
import de.nulide.shiftcal.ui.calendar.CalViewModel
import de.nulide.shiftcal.ui.helper.SFragment
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class StatsFragment : SFragment() {

    private lateinit var binding: FragmentStatsBinding
    override val fragmentName = "stats"
    lateinit var calViewModel: CalViewModel
    private var statsMonth: YearMonth? = null
    private var monthSwipeStartX = 0f
    private var monthSwipeStartY = 0f
    private var monthSwipeHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calViewModel = ViewModelProvider(main)[CalViewModel::class]

        binding = FragmentStatsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun updateActivity() {
        if (statsMonth == null) {
            statsMonth = calViewModel.getCurrentMonth()
        }
        setupMonthSwipe()
        renderStats(statsMonth ?: calViewModel.getCurrentMonth())
    }

    private fun renderStats(yearMonth: YearMonth) {
        val months = DateFormatSymbols().months.toList()
        val sc = SCRepoManager.getInstance(ctx)
        if (sc.familyMode) {
            binding.statsFromText.visibility = View.VISIBLE
            lifecycleScope.launch {
                binding.statsFromText.text = getString(R.string.stats_from, sc.users.getName())
            }
        } else {
            binding.statsFromText.visibility = View.GONE
        }

        setTitle(months[yearMonth.month.value - 1] + "\t\t\t" + yearMonth.year.toString())
        val workDays =
            sc.workDays.getWorkDaysOfMonth(yearMonth.year, yearMonth.monthValue)

        if (workDays.isNotEmpty()) {
            binding.emptyStatsContent.visibility = View.GONE
            binding.statsContent.visibility = View.VISIBLE

            var workMinutes =
                sc.workDays.getWorkMinutesForMonth(
                    yearMonth.year,
                    yearMonth.monthValue
                )
            val overtimeTotalMinutes =
                sc.workDays.getOvertimeMinutesForMonth(
                    yearMonth.year,
                    yearMonth.monthValue
                )
            workMinutes += overtimeTotalMinutes
            val workIsNegative = workMinutes < 0
            val absoluteWorkMinutes = kotlin.math.abs(workMinutes)
            val workHours = absoluteWorkMinutes / 60
            workMinutes = absoluteWorkMinutes % 60
            binding.statsWorkTimeText.text =
                ctx.getString(
                    R.string.time_stat_signed,
                    if (workIsNegative) "-" else "",
                    workHours,
                    workMinutes
                )

            var breakMinutes =
                sc.workDays.getBreakMinutesForMonth(
                    yearMonth.year,
                    yearMonth.monthValue
                )
            val breakHours = breakMinutes / 60
            breakMinutes %= 60
            binding.statsBreakTimeText.text =
                ctx.getString(R.string.time_stat, breakHours, breakMinutes)

            var overtimeMinutes = overtimeTotalMinutes
            val overtimeLabelRes = if (overtimeMinutes >= 0) {
                R.string.stats_time_overtime_label
            } else {
                R.string.stats_time_less_work_label
            }
            binding.statsOvertimeLabel.setText(overtimeLabelRes)
            val overtimeHours = kotlin.math.abs(overtimeMinutes) / 60
            overtimeMinutes = kotlin.math.abs(overtimeMinutes) % 60
            binding.statsOvertimeText.text =
                ctx.getString(R.string.time_stat, overtimeHours, overtimeMinutes)


            val shifts = workDays.map { sc.shifts.get(it.shiftId) }.distinct()
            val shiftAdapter = StatsShiftAdapter(ctx, shifts.toList(), workDays)
            binding.listViewShifts.layoutManager = LinearLayoutManager(context)
            binding.listViewShifts.adapter = shiftAdapter
            binding.listViewShifts.isNestedScrollingEnabled = false
            binding.listViewShifts.setHasFixedSize(true)

            val settings = SettingsRepository.getInstance(ctx)
            val configuredAccounts = settings.getSpecialAccounts()
            val relevantSpecialAccountIds = LinkedHashSet<String>()
            configuredAccounts.mapTo(relevantSpecialAccountIds) { it.id }
            shifts.mapNotNullTo(relevantSpecialAccountIds) { it.specialAccountId }
            if (relevantSpecialAccountIds.isEmpty()) {
                binding.specialAccountsSection.visibility = View.GONE
            } else {
                binding.specialAccountsSection.visibility = View.VISIBLE
                binding.specialAccountsContainer.removeAllViews()
                val inflater = LayoutInflater.from(ctx)
                for (accountId in relevantSpecialAccountIds) {
                    val row = inflater.inflate(R.layout.item_week_day_stat, binding.specialAccountsContainer, false)
                    val accountName = configuredAccounts.firstOrNull { it.id == accountId }?.name
                        ?: getString(R.string.special_account_missing_name)
                    val accountMinutes = sc.workDays.getSpecialAccountMinutesForMonth(
                        yearMonth.year,
                        yearMonth.monthValue,
                        accountId
                    )
                    val absoluteMinutes = kotlin.math.abs(accountMinutes)
                    row.findViewById<TextView>(R.id.statsWeekDayLabel).text =
                        getString(R.string.stats_special_account_label, accountName)
                    row.findViewById<TextView>(R.id.statsWeekDayCount).text = ctx.getString(
                        R.string.time_stat_signed,
                        if (accountMinutes < 0) "-" else "",
                        absoluteMinutes / 60,
                        absoluteMinutes % 60
                    )
                    binding.specialAccountsContainer.addView(row)
                }
            }

            val firstDayOfWeekIndex = settings.getInt(Settings.START_OF_WEEK)
            val daysOfWeek = daysOfWeek(DayOfWeek.of(firstDayOfWeekIndex + 1))
            val weekDayStats = IntArray(7)
            for (workDay in workDays) {
                weekDayStats[daysOfWeek.indexOf(workDay.day.dayOfWeek)]++
            }
            binding.statsWeekDaysContainer.children
                .map { it as LinearLayout }
                .forEachIndexed { index, layout ->
                    val dayOfWeek = daysOfWeek[index]
                    val title =
                        dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + ":"
                    layout.findViewById<TextView>(R.id.statsWeekDayLabel).text = title
                    layout.findViewById<TextView>(R.id.statsWeekDayCount).text =
                        ctx.getString(R.string.numberformat, weekDayStats[index])
                }
        } else {
            binding.emptyStatsContent.visibility = View.VISIBLE
            binding.statsContent.visibility = View.GONE
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMonthSwipe() {
        main.binding.topAppBar.setOnClickListener { }
        main.binding.topAppBar.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    monthSwipeStartX = event.x
                    monthSwipeStartY = event.y
                    monthSwipeHandled = false
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - monthSwipeStartX
                    val deltaY = event.y - monthSwipeStartY
                    val minDistancePx = 28f * resources.displayMetrics.density
                    if (!monthSwipeHandled &&
                        kotlin.math.abs(deltaX) >= minDistancePx &&
                        kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY) * 1.1f
                    ) {
                        if (deltaX < 0) {
                            statsMonth = (statsMonth ?: calViewModel.getCurrentMonth()).plusMonths(1)
                        } else {
                            statsMonth = (statsMonth ?: calViewModel.getCurrentMonth()).minusMonths(1)
                        }
                        renderStats(statsMonth ?: calViewModel.getCurrentMonth())
                        monthSwipeHandled = true
                        view.performClick()
                        true
                    } else {
                        true
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    monthSwipeHandled
                }

                else -> true
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onPause() {
        super.onPause()
        runCatching {
            main.binding.topAppBar.setOnTouchListener(null)
        }
    }

}
