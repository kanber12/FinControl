package by.kanber.fincontrol.transaction

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.SpinnerAdapter
import by.kanber.fincontrol.base.SpinnerItem
import by.kanber.fincontrol.database.DBHelper
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.DateTimePickerDialogFragment
import by.kanber.fincontrol.model.*
import by.kanber.fincontrol.model.Currency
import by.kanber.fincontrol.util.DateUtil
import by.kanber.fincontrol.util.TextUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_add_transaction.view.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.util.*

class AddTransactionFragment : Fragment() {
    private var listener: OnAddTransactionListener? = null
    private lateinit var transTypeImageView: ImageView
    private lateinit var sumEditText: TextInputEditText
    private lateinit var sumInputLayout: TextInputLayout
    private lateinit var descriptionEditText: EditText
    private lateinit var currencyAutoComplete: AutoCompleteTextView
    private lateinit var currencyInputLayout: TextInputLayout
    private lateinit var placeAutoComplete: AutoCompleteTextView
    private lateinit var placeInputLayout: TextInputLayout
    private lateinit var methodSpinner: AppCompatSpinner
    private lateinit var categorySpinner: AppCompatSpinner
    private lateinit var currTimeCheckBox: CheckBox
    private lateinit var dateTimeLayout: LinearLayout
    private lateinit var dateButton: MaterialButton
    private lateinit var timeButton: MaterialButton
    private lateinit var addPlaceTextView: TextView

    private lateinit var placeList: MutableList<String>
    private lateinit var placeAdapter: ArrayAdapter<String>
    private lateinit var calendar: Calendar
    private var isCustomDate = false
    private var isChargeOff = true

    private var isFromBrowse = false
    private var isEdit = false
    private lateinit var oldTrans: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        calendar = Calendar.getInstance()
        isEdit = arguments?.getBoolean("isEdit") ?: false
        isFromBrowse = arguments?.getBoolean("isFromBrowse") ?: false

        if (isEdit) {
            oldTrans = arguments?.getParcelable("trans")!!
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

        val toolbar = view.toolbar
        transTypeImageView = view.add_transaction_type_image_view
        sumEditText = view.add_transaction_sum_edit_text
        sumInputLayout = view.add_transaction_sum_input_layout
        descriptionEditText = view.add_transaction_description_edit_text
        currencyAutoComplete = view.add_transaction_currency_text_view
        currencyInputLayout = view.add_transaction_currency_input_layout
        placeAutoComplete = view.add_transaction_place_text_view
        placeInputLayout = view.add_transaction_place_input_layout
        methodSpinner = view.add_transaction_payment_method_spinner
        categorySpinner = view.add_transaction_category_spinner
        currTimeCheckBox = view.add_transaction_current_time_check_box
        dateTimeLayout = view.add_transaction_date_time_layout
        dateButton = view.add_transaction_date_button
        timeButton = view.add_transaction_time_button
        addPlaceTextView = view.add_transaction_add_place_text_view

        toolbar.title =
            if (isEdit) getString(R.string.edit_transaction) else getString(R.string.add_transaction)

        toolbar.setNavigationIcon(R.drawable.ic_close_white)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            if (checkFields()) {
                closeFragment()
            } else {
                showConfirmDialog()
            }
        }

        transTypeImageView.setOnClickListener {
            isChargeOff = if (isChargeOff) {
                transTypeImageView.setImageResource(R.drawable.ic_refill)
                placeInputLayout.hint = getString(R.string.from)
                false
            } else {
                transTypeImageView.setImageResource(R.drawable.ic_charge_off)
                placeInputLayout.hint = getString(R.string.place)
                true
            }
        }

        currTimeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dateTimeLayout.visibility = View.GONE
            } else {
                dateTimeLayout.visibility = View.VISIBLE

                if (!isCustomDate) {
                    if (!isEdit) {
                        calendar.timeInMillis = System.currentTimeMillis()
                    }

                    setButtonsText()
                }
            }
        }

        addPlaceTextView.setOnClickListener { addPlace() }
        timeButton.setOnClickListener { showTimePickerDialog() }
        dateButton.setOnClickListener { showDatePickerDialog() }

        placeAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                addPlaceTextView.visibility =
                    if (TextUtil.isEmpty(s.toString()) || placeList.contains(s.toString().trim())) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

        sumEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val sum = s.toString()
                val error = if (TextUtil.isEmpty(sum)) {
                    ""
                } else {
                    try {
                        sum.toDouble()
                        ""
                    } catch (e: NumberFormatException) {
                        "Sum must be decimal number"
                    }
                }

                sumInputLayout.error = error

                if (sum.contains('.') && sum.substring(sum.indexOf(".") + 1).length > 2) {
                    sumEditText.setText("")
                    sumEditText.append(sum.substring(0, sum.indexOf(".") + 3))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        currencyAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currencyInputLayout.error = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        placeAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                placeInputLayout.error = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        initMethodSpinner((activity as MainActivity).helper)
        initCategorySpinner((activity as MainActivity).helper)
        initPlaceAutoComplete((activity as MainActivity).helper)
        initCurrencyAutoComplete((activity as MainActivity).helper)

        if (isEdit) {
            calendar.timeInMillis = oldTrans.date
            sumEditText.setText(oldTrans.sum.toString())
            currencyAutoComplete.setText(oldTrans.currency)
            placeAutoComplete.setText(oldTrans.place)
            descriptionEditText.setText(oldTrans.description)
            currTimeCheckBox.text = getString(R.string.use_original_time)
            setButtonsText()
            selectCategory()
            selectMethod()

            if (!oldTrans.type) {
                transTypeImageView.performClick()
            }
        }

        val keyListener = View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                if (checkFields()) {
                    closeFragment()
                } else {
                    showConfirmDialog()
                }

                return@OnKeyListener true
            }

            return@OnKeyListener false
        }

        sumEditText.setOnKeyListener(keyListener)
        currencyAutoComplete.setOnKeyListener(keyListener)
        placeAutoComplete.setOnKeyListener(keyListener)
        descriptionEditText.setOnKeyListener(keyListener)

        sumEditText.requestFocus()

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_transaction_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (isEdit) {
            editTransaction()
        } else {
            addTransaction()
        }

        return true
    }

    private fun setButtonsText() {
        dateButton.text = DateUtil.getDate(calendar.timeInMillis)
        timeButton.text = DateUtil.getTime(calendar.timeInMillis)
    }

    private fun initMethodSpinner(helper: DBHelper) {
        val idDefault = PaymentMethod.getDefaultMethodId(helper)
        val methodList = mutableListOf<SpinnerItem>()
        methodList.addAll(PaymentMethod.getAllMethods(helper))

        val adapter = SpinnerAdapter(
            activity!!,
            R.layout.spinner_item,
            R.layout.spinner_dropdown_item,
            methodList,
            true
        )

        methodSpinner.adapter = adapter

        if (idDefault != -1) {
            for ((index, method) in methodList.withIndex()) {
                method as PaymentMethod
                if (method.id == idDefault) {
                    methodSpinner.setSelection(index)
                    break
                }
            }
        } else {
            methodSpinner.setSelection(0)
        }
    }

    private fun initCategorySpinner(helper: DBHelper) {
        val categoryList = mutableListOf<SpinnerItem>()
        categoryList.addAll(Category.getAllCategories(helper, ""))

        val adapter = SpinnerAdapter(
            activity!!,
            R.layout.spinner_item,
            R.layout.spinner_dropdown_item,
            categoryList,
            false
        )

        categorySpinner.adapter = adapter
        categorySpinner.setSelection(0)
    }

    private fun initPlaceAutoComplete(helper: DBHelper) {
        placeList = Place.getAllPlaces(helper)
        placeAdapter =
            ArrayAdapter(activity!!, android.R.layout.simple_dropdown_item_1line, placeList)
        placeAutoComplete.setAdapter(placeAdapter)
    }

    private fun initCurrencyAutoComplete(helper: DBHelper) {
        val currencyList = Currency.getAllCurrencies(helper)
        val currencyAdapter = ArrayAdapter<String>(
            activity!!,
            android.R.layout.simple_dropdown_item_1line,
            currencyList
        )
        currencyAutoComplete.setAdapter(currencyAdapter)
    }

    private fun selectCategory() {
        val transCat = oldTrans.category
        val adapter = categorySpinner.adapter

        for (index in 0..adapter.count) {
            val cat = adapter.getItem(index) as Category

            if (cat.id == transCat.id) {
                categorySpinner.setSelection(index)
                break
            }
        }
    }

    private fun selectMethod() {
        val transMethod = oldTrans.method
        val adapter = methodSpinner.adapter

        for (index in 0..adapter.count) {
            val method = adapter.getItem(index) as PaymentMethod

            if (method.id == transMethod.id) {
                methodSpinner.setSelection(index)
                break
            }
        }
    }

    private fun addPlace() {
        val place = placeAutoComplete.text.toString().trim()
        Place.insertPlace((activity as MainActivity).helper, place)
        placeList.add(place)
        placeAdapter.add(place)
        placeAdapter.notifyDataSetChanged()
        addPlaceTextView.visibility = View.GONE
    }

    private fun editTransaction() {
        val sum = sumEditText.text.toString()
        val currency = currencyAutoComplete.text.toString()
        val place = placeAutoComplete.text.toString()

        val date = if (isCustomDate) {
            calendar.timeInMillis
        } else {
            oldTrans.date
        }

        if (checkTransactionData(sum, currency, place)) {
            val trans = createTransaction(sum, currency, place, date)

            if (trans == oldTrans) {
                closeFragment()
            } else {
                showConfirmDialog(trans)
            }
        }
    }

    private fun openEditedTransaction(trans: Transaction) {
        if (isFromBrowse) {
            val fragment = activity?.supportFragmentManager?.findFragmentByTag(BrowseTransactionFragment::class.java.name)!!
            val tr = activity?.supportFragmentManager?.beginTransaction()
            tr?.remove(fragment)?.commit()

            listener?.editTransaction(trans, isCustomDate)
            closeFragment()

            activity?.supportFragmentManager?.popBackStack()
        } else {
            listener?.editTransaction(trans, isCustomDate)
            closeFragment()
        }

        val fragment = BrowseTransactionFragment.newInstance(trans)
        val tr = activity?.supportFragmentManager?.beginTransaction()
        tr?.replace(R.id.fragment_container, fragment, fragment.javaClass.name)
        tr?.addToBackStack(fragment.javaClass.name)
        tr?.commit()
    }

    private fun addTransaction() {
        val sum = sumEditText.text.toString()
        val currency = currencyAutoComplete.text.toString()
        val place = placeAutoComplete.text.toString()

        val date = if (currTimeCheckBox.isChecked) {
            System.currentTimeMillis()
        } else {
            calendar.timeInMillis
        }

        if (checkTransactionData(sum, currency, place)) {
            val trans = createTransaction(sum, currency, place, date)

            listener?.addTransaction(trans, !currTimeCheckBox.isChecked)
            closeFragment()
        }
    }

    private fun createTransaction(
        sum: String,
        currency: String,
        place: String,
        date: Long
    ): Transaction {
        val description = descriptionEditText.text.toString().trim()
        val category = categorySpinner.selectedItem as Category
        val method = methodSpinner.selectedItem as PaymentMethod
        val id = if (isEdit) oldTrans.id else 0

        return Transaction(
            id, isChargeOff, sum.toDouble(), currency, date, description, category, method, place
        )
    }

    private fun checkTransactionData(sum: String, currency: String, place: String): Boolean {
        if (TextUtil.isEmpty(sum)) {
            sumInputLayout.error = "Transaction sum is required"

            return false
        } else {
            try {
                sum.toDouble()
            } catch (exception: NumberFormatException) {
                return false
            }
        }

        if (TextUtil.isEmpty(currency)) {
            currencyInputLayout.error = "Required"

            return false
        }

        if (TextUtil.isEmpty(place)) {
            placeInputLayout.error = "Transaction place is required"

            return false
        }

        return true
    }

    private fun showConfirmDialog() {
        val fragment = ConfirmationDialogFragment.newInstance(
            "Are you sure you want to discard all changes?",
            "Discard"
        )
        fragment.setOnConfirmClickListener {
            closeFragment()
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showConfirmDialog(trans: Transaction) {
        val fragment = ConfirmationDialogFragment.newInstance("Save changes?")
        fragment.setOnConfirmClickListener {
            openEditedTransaction(trans)
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showDatePickerDialog() {
        val fragment = DateTimePickerDialogFragment.newInstance(calendar, true)
        fragment.setOnDateTimePickerInteractionListener {
            isCustomDate = true
            dateButton.text = DateUtil.getDate(calendar.timeInMillis)
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showTimePickerDialog() {
        val fragment = DateTimePickerDialogFragment.newInstance(calendar, false)
        fragment.setOnDateTimePickerInteractionListener {
            isCustomDate = true
            timeButton.text = DateUtil.getTime(calendar.timeInMillis)
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun checkFields(): Boolean {
        val sum = sumEditText.text.toString()
        val currency = currencyAutoComplete.text.toString()
        val place = placeAutoComplete.text.toString()
        val descr = descriptionEditText.text.toString()

        if (isEdit) {
            return isEditCheckFields(sum, currency, place, descr)
        }

        return TextUtil.isEmpty(sum) && TextUtil.isEmpty(currency) && TextUtil.isEmpty(place) && TextUtil.isEmpty(
            descr
        ) && isChargeOff && currTimeCheckBox.isChecked
    }

    private fun isEditCheckFields(
        sumStr: String,
        currency: String,
        place: String,
        descr: String
    ): Boolean {
        val cat = categorySpinner.selectedItem as Category
        val meth = methodSpinner.selectedItem as PaymentMethod
        val sum = try {
            sumStr.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
        val date = if (isCustomDate) {
            calendar.timeInMillis
        } else {
            oldTrans.date
        }

        val trans =
            Transaction(oldTrans.id, isChargeOff, sum, currency, date, descr, cat, meth, place)

        return trans == oldTrans
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fragment = (context as MainActivity).supportFragmentManager.findFragmentByTag(
            TransactionListFragment::class.java.name
        )

        if (fragment is OnAddTransactionListener) {
            listener = fragment
        } else {
            throw RuntimeException(fragment.toString() + " must implement OnAddTransactionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnAddTransactionListener {
        fun addTransaction(trans: Transaction, isCustomTime: Boolean)
        fun editTransaction(trans: Transaction, isCustomTime: Boolean)
    }

    companion object {
        @JvmStatic
        fun newInstance(trans: Transaction, isFromBrowse: Boolean) =
            AddTransactionFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEdit", true)
                    putParcelable("trans", trans)
                    putBoolean("isFromBrowse", isFromBrowse)
                }
            }
    }
}
