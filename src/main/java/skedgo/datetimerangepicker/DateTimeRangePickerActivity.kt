package skedgo.datetimerangepicker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.squareup.timessquare.CalendarPickerView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import org.joda.time.DateTime
import skedgo.datetimerangepicker.databinding.DateTimeRangePickerBinding
import java.util.*
import android.content.Intent as Intent1


class DateTimeRangePickerActivity : AppCompatActivity() {
    companion object {
        fun newIntent(
            context: Context?,
            timeZone: TimeZone?,
            startTimeInMillis: Long?,
            endTimeInMillis: Long?
        ): Intent1 {
            val intent = Intent1(context!!, DateTimeRangePickerActivity::class.java)
            startTimeInMillis?.let {
                intent.putExtra(
                    DateTimeRangePickerViewModel.KEY_START_TIME_IN_MILLIS,
                    it
                )
            }
            endTimeInMillis?.let {
                intent.putExtra(
                    DateTimeRangePickerViewModel.KEY_END_TIME_IN_MILLIS,
                    it
                )
            }
            intent.putExtra(DateTimeRangePickerViewModel.KEY_TIME_ZONE, timeZone!!.id)
            return intent
        }
    }

    private val viewModel: DateTimeRangePickerViewModel by lazy {
        DateTimeRangePickerViewModel(TimeFormatter(applicationContext))
    }
    private val binding: DateTimeRangePickerBinding by lazy {
        DataBindingUtil.setContentView<DateTimeRangePickerBinding>(
            this,
            R.layout.date_time_range_picker
        )
    }

    private fun checkDate(): Boolean {
        var passes = true
        val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
        val date = c.time
        viewModel.startDateTime.value.let { start ->
            val end = viewModel.endDateTime.value
            if (DateFormat.format("dd", start.toDate()) == DateFormat.format("dd", end.toDate())) {
                passes = false
                Toast.makeText(
                    applicationContext,
                    "Tanggal mulai dan berakhir tidak boleh sama",
                    Toast.LENGTH_SHORT
                ).show()
            }else if (start.toDate() <= date) {
                passes = false
                Toast.makeText(
                    applicationContext,
                    "Waktu Pengambilan harus +6 jam dari waktu sekarang",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return passes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.handleArgs(intent.extras)
        binding.viewModel = viewModel

        val toolbar = binding.toolbar
        toolbar.inflateMenu(R.menu.date_time_range_picker)
        toolbar.setNavigationOnClickListener { v -> finish() }
        toolbar.setOnMenuItemClickListener { item ->
            when {
                item.itemId == R.id.dateTimeRangePickerDoneItem -> {
                    if (checkDate()) {
                        setResult(Activity.RESULT_OK, viewModel.createResultIntent())
                        finish()
                    }
                }
            }
            true
        }

        val calendarPickerView = binding.calendarPickerView
        calendarPickerView.init(viewModel.minDate, viewModel.maxDate)
            .inMode(CalendarPickerView.SelectionMode.RANGE)
        viewModel.startDateTime.value?.let { calendarPickerView.selectDate(it.toDate()) }
        viewModel.endDateTime.value?.let { calendarPickerView.selectDate(it.toDate()) }

        calendarPickerView.setOnDateSelectedListener(object :
            CalendarPickerView.OnDateSelectedListener {
            override fun onDateSelected(date: Date) {
                viewModel.updateSelectedDates(calendarPickerView.selectedDates)
            }

            override fun onDateUnselected(date: Date) {
                viewModel.updateSelectedDates(calendarPickerView.selectedDates)
            }
        })

        binding.pickStartTimeView.setOnClickListener { v ->
            showTimePicker(viewModel.startDateTime.value, viewModel.onStartTimeSelected)
        }
//        binding.pickEndTimeView.setOnClickListener { v ->
//            showTimePicker(viewModel.endDateTime.value, viewModel.onEndTimeSelected)
//        }
    }

    private fun showTimePicker(
        initialTime: DateTime,
        listener: TimePickerDialog.OnTimeSetListener
    ) {
        val tpd = TimePickerDialog.newInstance(
            listener,
            initialTime.hourOfDay,
            initialTime.minuteOfHour,
            true
        )

        viewModel.startDateTime.value?.let {
            val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
            val date = c.time
            if (DateFormat.format("dd", it.toDate()) == DateFormat.format("dd", date))
                tpd.setMinTime(c.get(Calendar.HOUR_OF_DAY) + 5, c.get(Calendar.MINUTE), 0)
            else
                tpd.setMinTime(5, 0, 0)
        }

        tpd.show(supportFragmentManager, "TimePickerDialog")
    }
}
