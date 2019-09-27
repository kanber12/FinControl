package by.kanber.fincontrol.summary


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.dialog.PeriodPickerDialogFragment
import by.kanber.fincontrol.util.DateUtil
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_simple_summary.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*
import java.util.*

class SimpleSummaryFragment : Fragment() {
    private lateinit var startPeriodButton: MaterialButton
    private lateinit var endPeriodButton: MaterialButton
    private lateinit var summaryLayout: ConstraintLayout

    private lateinit var startCalendar: Calendar
    private lateinit var endCalendar: Calendar
    private var isStartPicked = false
    private var isEndPicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCalendars()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_simple_summary, container, false)

        val toolbar = view.scrollable_toolbar
        startPeriodButton = view.simple_summary_start_period_button
        endPeriodButton = view.simple_summary_end_period_button
        summaryLayout = view.simple_summary_data_constraint_layout

        toolbar.title = getString(R.string.summary)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { closeFragment() }

        startPeriodButton.setOnClickListener { showStartPeriodPickerDialog() }
        endPeriodButton.setOnClickListener { showEndPeriodPickerDialog() }

        return view
    }

    private fun showStartPeriodPickerDialog() {
        val fragment = PeriodPickerDialogFragment.newInstance(startCalendar, true, endCalendar.timeInMillis)

        fragment.setOnPeriodPickerFragmentInteractionListener {
            startPeriodButton.text = DateUtil.getDate(startCalendar.timeInMillis)
            isStartPicked = true

            if (isStartPicked && isEndPicked) {
                showSummary()
            }
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showEndPeriodPickerDialog() {
        val fragment = PeriodPickerDialogFragment.newInstance(endCalendar, false, startCalendar.timeInMillis)

        fragment.setOnPeriodPickerFragmentInteractionListener {
            endPeriodButton.text = DateUtil.getDate(endCalendar.timeInMillis)
            isEndPicked = true

            if (isStartPicked && isEndPicked) {
                showSummary()
            }
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showSummary() {
        summaryLayout.visibility = View.VISIBLE
    }

    private fun initCalendars() {
        startCalendar = Calendar.getInstance()
        endCalendar = Calendar.getInstance()

        val (year, month, day) = arrayOf(
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        )

        startCalendar.timeInMillis = 0
        endCalendar.clear()
        endCalendar.set(year, month, day)
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }
}
