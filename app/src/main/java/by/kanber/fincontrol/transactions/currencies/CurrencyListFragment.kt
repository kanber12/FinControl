package by.kanber.fincontrol.transactions.currencies


import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.CurrencyAdapter
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.dialog.InputTextDialogFragment
import by.kanber.fincontrol.model.Currency
import kotlinx.android.synthetic.main.simple_list_layout.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*
import java.util.*

class CurrencyListFragment : Fragment() {
    private lateinit var currencies: MutableList<String>
    private lateinit var adapter: CurrencyAdapter
    private lateinit var emptyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currencies = Currency.getAllCurrencies((activity as MainActivity).helper)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.simple_list_layout, container, false)

        emptyTextView = view.simple_list_empty_text_view
        emptyTextView.text = getString(R.string.no_currencies)

        val toolbar = view.scrollable_toolbar
        toolbar.title = getString(R.string.currency)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        val recyclerView = view.simple_list_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        adapter = CurrencyAdapter(currencies)
        adapter.setOnCurrencyClickListener(object : CurrencyAdapter.OnCurrencyClickListener {
            override fun onCurrencyClick(position: Int) {
                showContextMenu(position)
            }
        })

        recyclerView.adapter = adapter

        if (currencies.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showAddCurrencyDialog()

        return true
    }

    private fun showContextMenu(position: Int) {
        val fragment = ContextMenuDialogFragment.newInstance(R.menu.delete_edit_menu)

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_edit -> showEditCurrencyDialog(position)
                    R.id.menu_delete -> showConfirmationDialog(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showAddCurrencyDialog() {
        val fragment = InputTextDialogFragment.newInstance(getString(R.string.currency), true)

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                addCurrency(text)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun addCurrency(currency: String) {
        Currency.insertCurrency((activity as MainActivity).helper, currency)

        currencies.add(currency)
        adapter.notifyItemInserted(currencies.size - 1)

        if (currencies.size == 1) {
            emptyTextView.visibility = View.GONE
        }
    }

    private fun showEditCurrencyDialog(position: Int) {
        val fragment = InputTextDialogFragment.newInstance(getString(R.string.currency), currencies[position],true)

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                editCurrency(text, position)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun editCurrency(name: String, position: Int) {
        val old = currencies[position]
        currencies[position] = name
        adapter.notifyItemChanged(position)

        Currency.updateCurrency((activity as MainActivity).helper, old, name)
    }

    private fun showConfirmationDialog(position: Int) {
        val fragment =
            ConfirmationDialogFragment.newInstance("Are you sure you want to delete this currency? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deleteCurrency(position) }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deleteCurrency(position: Int) {
        val currency = currencies.removeAt(position)
        adapter.notifyItemRemoved(position)

        Currency.deleteCurrency((activity as MainActivity).helper, currency)

        if (currencies.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }
    }

    private fun checkIsUnique(name: String): Boolean {
        for (cur in currencies) {
            if (cur.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())) {
                return false
            }
        }

        return true
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }
}
