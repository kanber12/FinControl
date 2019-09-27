package by.kanber.fincontrol.transaction

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.model.Transaction
import by.kanber.fincontrol.util.DateUtil
import kotlinx.android.synthetic.main.fragment_browse_transaction.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*

class BrowseTransactionFragment : Fragment() {
    private lateinit var placeTextView: TextView
    private lateinit var sumTextView: TextView
    private lateinit var currencyTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var methodTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var transaction: Transaction
    private var fromArchive = false
    private var listener: OnBrowseTransactionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transaction = arguments?.getParcelable("trans")!!

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse_transaction, container, false)

        val toolbar = view.scrollable_toolbar
        placeTextView = view.browse_trans_place_text_view
        sumTextView = view.browse_trans_sum_text_view
        currencyTextView = view.browse_trans_currency_text_view
        categoryTextView = view.browse_trans_category_text_view
        methodTextView = view.browse_trans_method_text_view
        descriptionTextView = view.browse_trans_description_text_view

        toolbar.title = DateUtil.getViewableTime(transaction.date)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        val drawable: Drawable?
        val color: Int

        if (transaction.type) {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_charge_off)
            color = ContextCompat.getColor(context!!, R.color.colorChargeOff)
        } else {
            drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_refill)
            color = ContextCompat.getColor(context!!, R.color.colorRefill)
        }

        drawable?.setBounds(0, 0, 50, 50)
        placeTextView.text = transaction.place
        methodTextView.text = transaction.method.name
        categoryTextView.text = transaction.category.name
        sumTextView.setCompoundDrawables(drawable, null, null, null)
        sumTextView.setTextColor(color)
        sumTextView.text = transaction.sum.toString()
        currencyTextView.text = transaction.currency

        if (transaction.description == "") {
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.text = transaction.description
            descriptionTextView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuRes = if (fromArchive) {
            R.menu.archived_transaction_context_menu
        } else {
            R.menu.transaction_context_menu
        }

        inflater.inflate(menuRes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_trans_edit -> editTransaction()
            R.id.menu_trans_archive -> archiveTransaction()
            R.id.menu_arch_trans_unarchive -> unarchiveTransaction()
            R.id.menu_arch_trans_delete -> showConfirmationDialog()
        }

        return true
    }

    private fun editTransaction() {
        val fragment = AddTransactionFragment.newInstance(transaction, true)
        val trans = activity?.supportFragmentManager?.beginTransaction()
        trans?.replace(R.id.fragment_container, fragment, fragment.javaClass.name)
        trans?.addToBackStack(fragment.javaClass.name)
        trans?.commit()
    }

    private fun archiveTransaction() {
        listener?.onArchiveTrans(transaction.id)
        closeFragment()
    }

    private fun unarchiveTransaction() {
        listener?.onUnarchiveTrans(transaction.id)
        closeFragment()
    }

    private fun showConfirmationDialog() {
        val fragment =
            ConfirmationDialogFragment.newInstance("Are you sure you want to delete this transaction? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deleteTransaction() }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deleteTransaction() {
        listener?.onDeleteTrans(transaction.id)
        closeFragment()
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        fromArchive = arguments?.getBoolean("fromArchive") ?: false

        val tag = if (fromArchive) {
            ArchivedTransactionListFragment::class.java.name
        } else {
            TransactionListFragment::class.java.name
        }

        val fragment = (context as MainActivity).supportFragmentManager.findFragmentByTag(tag)

        if (fragment is OnBrowseTransactionListener) {
            listener = fragment
        } else {
            throw RuntimeException(fragment.toString() + " must implement OnBrowseTransactionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnBrowseTransactionListener {
        fun onArchiveTrans(transId: Int) {}
        fun onUnarchiveTrans(transId: Int) {}
        fun onDeleteTrans(transId: Int) {}
    }

    companion object {
        @JvmStatic
        fun newInstance(transaction: Transaction, fromArchive: Boolean) =
            BrowseTransactionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("trans", transaction)
                    putBoolean("fromArchive", fromArchive)
                }
            }

        fun newInstance(transaction: Transaction) =
            BrowseTransactionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("trans", transaction)
                }
            }
    }
}
