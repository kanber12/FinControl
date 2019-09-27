package by.kanber.fincontrol.transaction

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.TransactionAdapter
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.model.Transaction
import by.kanber.fincontrol.util.CustomItemDivider
import kotlinx.android.synthetic.main.fragment_archived_transaction_list.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*

class ArchivedTransactionListFragment : Fragment(), BrowseTransactionFragment.OnBrowseTransactionListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var emptyTextView: TextView
    private lateinit var dividerView: View

    private var listener: OnArchivedListListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactions = Transaction.getAllTransactions((activity as MainActivity).helper, true)
        sortByTime()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_archived_transaction_list, container, false)
        val manager = LinearLayoutManager(context)
        val divider = ContextCompat.getDrawable(context!!, R.drawable.list_item_divider)

        val toolbar = view.scrollable_toolbar
        toolbar.title = getString(R.string.archived_transactions)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        dividerView = view.arch_trans_list_divider_view
        emptyTextView = view.arch_trans_list_empty_text_view
        recyclerView = view.arch_trans_list_recycler_view
        recyclerView.layoutManager = manager

        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            CustomItemDivider(
                context,
                manager.orientation
            ).setDrawable(divider!!)
        )

        adapter = TransactionAdapter(context!!, transactions)
        adapter.setOnTransactionClickListener(object :
            TransactionAdapter.OnTransactionClickListener {
            override fun onClick(position: Int) {
                browseTransaction(position)
            }

            override fun onLongClick(position: Int) {
                showContextMenu(position)
            }
        })

        recyclerView.adapter = adapter

        if (transactions.size == 0) {
            emptyTextView.visibility = View.VISIBLE
            dividerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            dividerView.visibility = View.VISIBLE
        }

        return view
    }

    private fun browseTransaction(position: Int) {
        val trans = activity?.supportFragmentManager?.beginTransaction()
        val fragment = BrowseTransactionFragment.newInstance(transactions[position], true)
        trans?.replace(R.id.fragment_container, fragment, fragment.javaClass.name)
        trans?.addToBackStack(fragment.javaClass.name)
        trans?.commit()
    }

    private fun showContextMenu(position: Int) {
        val fragment = ContextMenuDialogFragment.newInstance(R.menu.archived_transaction_context_menu)

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_arch_trans_unarchive -> unarchiveTransaction(position)
                    R.id.menu_arch_trans_delete -> showConfirmationDialog(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun unarchiveTransaction(index: Int) {
        val trans = transactions.removeAt(index)
        adapter.notifyItemRemoved(index)

        Transaction.unarchiveTransaction((activity as MainActivity).helper, trans.id)
        listener?.onTransactionUnarchived(trans)

        if (transactions.size == 0) {
            emptyTextView.visibility = View.VISIBLE
            dividerView.visibility = View.GONE
        }
    }

    private fun showConfirmationDialog(position: Int) {
        val fragment = ConfirmationDialogFragment.newInstance("Are you sure you want to delete this transaction? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deleteTransaction(position) }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deleteTransaction(index: Int) {
        val trans = transactions.removeAt(index)
        adapter.notifyItemRemoved(index)

        Transaction.deleteTransaction((activity as MainActivity).helper, trans.id)

        if (transactions.size == 0) {
            emptyTextView.visibility = View.VISIBLE
            dividerView.visibility = View.GONE
        }
    }

    override fun onUnarchiveTrans(transId: Int) {
        val index = findTransactionById(transId)

        if (index != -1) {
            unarchiveTransaction(index)
        }
    }

    override fun onDeleteTrans(transId: Int) {
        val index = findTransactionById(transId)

        if (index != -1) {
            deleteTransaction(index)
        }
    }

    private fun findTransactionById(id: Int): Int {
        var ind = -1

        for ((index, trans) in transactions.withIndex()) {
            if (trans.id == id) {
                ind = index
                break
            }
        }

        return ind
    }

    private fun sortByTime() {
        transactions.sortBy { it.date }
        transactions.reverse()
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

        if (fragment is OnArchivedListListener) {
            listener = fragment
        } else {
            throw RuntimeException(fragment.toString() + " must implement OnArchivedListListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnArchivedListListener {
        fun onTransactionUnarchived(transaction: Transaction)
    }

}
