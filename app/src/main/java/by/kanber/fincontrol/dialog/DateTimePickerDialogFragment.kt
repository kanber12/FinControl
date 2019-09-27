package by.kanber.fincontrol.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DateTimePickerDialogFragment : DialogFragment() {
    private var listener: (() -> Unit)? = null
    private lateinit var calendar: Calendar
    private var isDate: Boolean = true

    companion object {
        fun newInstance(calendar: Calendar, isDate: Boolean): DateTimePickerDialogFragment {
            val fragment = DateTimePickerDialogFragment()
            val args = Bundle()
            args.putSerializable("calendar", calendar)
            args.putBoolean("isDate", isDate)
            fragment.arguments = args

            return fragment
        }
    }

    fun setOnDateTimePickerInteractionListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isDate = arguments?.getBoolean("isDate") ?: true
        calendar = arguments?.getSerializable("calendar") as Calendar
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog

        if (isDate) {
            val (year, month, day) = arrayOf(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            val listener =
                DatePickerDialog.OnDateSetListener { _, selectedYear, monthOfYear, dayOfMonth ->
                    calendar.set(selectedYear, monthOfYear, dayOfMonth)
                    listener?.invoke()
                }

            dialog = DatePickerDialog(activity!!, listener, year, month, day)

            dialog.datePicker.maxDate = System.currentTimeMillis()
        } else {
            val (hour, minute) = arrayOf(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )

            val listener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minuteOfHour ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minuteOfHour)
                listener?.invoke()
            }

            dialog = TimePickerDialog(activity!!, listener, hour, minute, true)
        }

        return dialog
    }
}