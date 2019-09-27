package by.kanber.fincontrol.transactions


import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.TransactionAdapter
import by.kanber.fincontrol.api.model.TransactionApiModel
import by.kanber.fincontrol.base.BaseView
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.dialog.NavigationDrawerDialogFragment
import by.kanber.fincontrol.methods.PaymentMethodListFragment
import by.kanber.fincontrol.model.Category
import by.kanber.fincontrol.model.PaymentMethod
import by.kanber.fincontrol.model.Transaction
import by.kanber.fincontrol.summary.SimpleSummaryFragment
import by.kanber.fincontrol.transactions.categories.CategoryListFragment
import by.kanber.fincontrol.transactions.currencies.CurrencyListFragment
import by.kanber.fincontrol.transactions.places.PlaceListFragment
import by.kanber.fincontrol.util.CustomItemDivider
import kotlinx.android.synthetic.main.fragment_transaction_list.view.*
import retrofit2.HttpException

class TransactionListFragment : Fragment(), BaseView<TransactionApiModel>,
    AddTransactionFragment.OnAddTransactionListener,
    ArchivedTransactionListFragment.OnArchivedListListener,
    BrowseTransactionFragment.OnBrowseTransactionListener,
    CategoryListFragment.OnCategoryListInteractionListener,
    PaymentMethodListFragment.OnPaymentMethodEditedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter
    private lateinit var emptyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactions = Transaction.getAllTransactions((activity as MainActivity).helper, false)
        sortByTime()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)
        val manager = LinearLayoutManager(context)
        val divider = ContextCompat.getDrawable(context!!, R.drawable.list_item_divider)

        (activity as MainActivity).setSupportActionBar(view.trans_list_bap)

        emptyTextView = view.trans_list_empty_text_view
        recyclerView = view.trans_list_recycler_view
        view.trans_list_add_fab.setOnClickListener {
            showAddTransactionFragment()
        }
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
        } else {
            emptyTextView.visibility = View.GONE
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.transaction_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_trans_list_show_methods -> showPaymentMethods()
            R.id.menu_trans_list_show_summary -> showSimpleSummary()
            android.R.id.home -> showNavigationDrawer()
        }

        return true
    }

    private fun showNavigationDrawer() {
        val fragment = NavigationDrawerDialogFragment()
        fragment.setOnNavigationItemClickListener(object :
            NavigationDrawerDialogFragment.OnNavigationDrawerItemClickListener {
            override fun onArchiveTransactionsClick() {
                showArchivedTransactionListFragment()
            }

            override fun onCategoriesClick() {
                showCategoryListFragment()
            }

            override fun onPlacesClick() {
                showPlaceListFragment()
            }

            override fun onCurrenciesClick() {
                showCurrencyListFragment()
            }

            override fun onItem3Click() {
                Toast.makeText(context, "Item 3", Toast.LENGTH_SHORT).show()
            }

            override fun onItem4Click() {
                Toast.makeText(context, "Item 4", Toast.LENGTH_SHORT).show()
            }

            override fun onItem5Click() {
                Toast.makeText(context, "Item 5", Toast.LENGTH_SHORT).show()
            }

            override fun onItem6Click() {
                Toast.makeText(context, "Item 6", Toast.LENGTH_SHORT).show()
            }

            override fun onItem7Click() {
                Toast.makeText(context, "Item 7", Toast.LENGTH_SHORT).show()
            }

            override fun onItem8Click() {
                Toast.makeText(context, "Item 8", Toast.LENGTH_SHORT).show()
            }

            override fun onItem9Click() {
                Toast.makeText(context, "Item 9", Toast.LENGTH_SHORT).show()
            }

            override fun onUserClick() {
                Toast.makeText(context, "User click", Toast.LENGTH_SHORT).show()
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showArchivedTransactionListFragment() {
        val fragment = ArchivedTransactionListFragment()
        showFragment(fragment)
    }

    private fun showCategoryListFragment() {
        val fragment = CategoryListFragment()
        showFragment(fragment)
    }

    private fun showPlaceListFragment() {
        val fragment = PlaceListFragment()
        showFragment(fragment)
    }

    private fun showCurrencyListFragment() {
        val fragment = CurrencyListFragment()
        showFragment(fragment)
    }

    private fun browseTransaction(position: Int) {
        val fragment = BrowseTransactionFragment.newInstance(transactions[position])
        showFragment(fragment)
    }

    private fun showAddTransactionFragment() {
        if (PaymentMethod.getMethodSize((activity as MainActivity).helper) == 0) {
            Toast.makeText(activity, "Add at least one payment method", Toast.LENGTH_SHORT).show()
        } else {
            val fragment = AddTransactionFragment()
            showFragment(fragment)
        }
    }

    private fun showContextMenu(position: Int) {
        val trans = transactions[position]

        val fragment = ContextMenuDialogFragment.newInstance(R.menu.transaction_context_menu)

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_trans_edit -> showEditTransactionFragment(trans)
                    R.id.menu_trans_archive -> archiveTransaction(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    override fun addTransaction(trans: Transaction, isCustomTime: Boolean) {
        val insertedTrans = Transaction.insertTransaction((activity as MainActivity).helper, trans)
        transactions.add(0, insertedTrans)

        if (isCustomTime) {
            sortByTime()
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyItemInserted(0)
        }
    }

    private fun sortByTime() {
        transactions.sortBy { it.date }
        transactions.reverse()
    }

    override fun onArchiveTrans(transId: Int) {
        val index = findTransactionById(transId)

        if (index != -1) {
            archiveTransaction(index)
        }
    }

    override fun onTransactionUnarchived(transaction: Transaction) {
        transactions.add(transaction)
        sortByTime()
    }

    override fun onCategoryDeleted(id: Int) {
        val defaultCategory = Category(1, "None")

        for (trans in transactions) {
            if (trans.category.id == id) {
                trans.category = defaultCategory
            }
        }
    }

    override fun onCategoryUpdated(category: Category) {
        for (trans in transactions) {
            if (trans.category.id == category.id) {
                trans.category = category
            }
        }
    }

    override fun methodEdited(method: PaymentMethod) {
        for (trans in transactions) {
            if (trans.method.id == method.id) {
                trans.method = method
            }
        }
    }

    private fun showEditTransactionFragment(trans: Transaction) {
        val fragment = AddTransactionFragment.newInstance(trans, false)
        showFragment(fragment)
    }

    override fun editTransaction(trans: Transaction, isCustomTime: Boolean) {
        val position = findTransactionById(trans.id)

        if (position != -1) {
            transactions[position] = trans
            Transaction.updateTransaction((activity as MainActivity).helper, trans)

            if (isCustomTime) {
                sortByTime()
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyItemChanged(position)
            }
        }
    }

    private fun archiveTransaction(index: Int) {
        val trans = transactions.removeAt(index)
        adapter.notifyItemRemoved(index)

        Transaction.archiveTransaction((activity as MainActivity).helper, trans.id)

        if (transactions.size == 0) {
            emptyTextView.visibility = View.VISIBLE
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

    private fun showPaymentMethods() {
        val fragment = PaymentMethodListFragment()
        showFragment(fragment)
    }

    private fun showSimpleSummary() {
        val fragment = SimpleSummaryFragment()
        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        val trans = activity?.supportFragmentManager?.beginTransaction()
        trans?.replace(R.id.fragment_container, fragment, fragment.javaClass.name)
        trans?.addToBackStack(fragment.javaClass.name)
        trans?.commit()
    }

    override fun onSuccessList(list: List<TransactionApiModel>) {

    }

    override fun onError(error: HttpException) {

    }
}
