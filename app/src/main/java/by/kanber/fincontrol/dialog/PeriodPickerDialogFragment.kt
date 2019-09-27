package by.kanber.fincontrol.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class PeriodPickerDialogFragment : DialogFragment() {
    private var listener: (() -> Unit)? = null
    private lateinit var calendar: Calendar
    private var isStart = true
    private var limit = 0L

    companion object {
        fun newInstance(calendar: Calendar, isStart: Boolean, limit: Long): PeriodPickerDialogFragment {
            val fragment = PeriodPickerDialogFragment()
            val args = Bundle()
            args.putSerializable("calendar", calendar)
            args.putBoolean("isStart", isStart)
            args.putLong("limit", limit)
            fragment.arguments = args

            return fragment
        }
    }

    fun setOnPeriodPickerFragmentInteractionListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendar = arguments?.getSerializable("calendar") as Calendar
        isStart = arguments?.getBoolean("isStart", true) ?: true
        limit = arguments?.getLong("limit", 0L) ?: 0L
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog

        val date = if (isStart && calendar.timeInMillis == 0L) Calendar.getInstance() else calendar

        val (year, month, day) = arrayOf(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )

        val listener =
            DatePickerDialog.OnDateSetListener { _, selectedYear, monthOfYear, dayOfMonth ->
                calendar.set(selectedYear, monthOfYear, dayOfMonth)
                listener?.invoke()
            }

        dialog = DatePickerDialog(activity!!, listener, year, month, day)

        if (isStart) {
            dialog.datePicker.minDate = 0
            dialog.datePicker.maxDate = limit
        } else {
            dialog.datePicker.minDate = limit
            dialog.datePicker.maxDate = System.currentTimeMillis()
        }

        return dialog
    }
}