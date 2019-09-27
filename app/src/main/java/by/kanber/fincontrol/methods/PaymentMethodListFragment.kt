package by.kanber.fincontrol.methods


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.PaymentMethodAdapter
import by.kanber.fincontrol.api.model.PaymentMethodApiModel
import by.kanber.fincontrol.base.BaseView
import by.kanber.fincontrol.base.PaymentMethodListItem
import by.kanber.fincontrol.dialog.AddMethodDialogFragment
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.model.PaymentMethod
import by.kanber.fincontrol.model.PaymentMethodHeader
import by.kanber.fincontrol.transactions.TransactionListFragment
import kotlinx.android.synthetic.main.fragment_payment_method_list.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*
import retrofit2.HttpException
import java.util.*

class PaymentMethodListFragment : Fragment(),
    BaseView<PaymentMethodApiModel> {
    private lateinit var adapter: PaymentMethodAdapter
    private lateinit var methods: MutableList<PaymentMethodListItem>
    private var listener: OnPaymentMethodEditedListener? = null
    private var idDefault: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initMethods()
        idDefault = PaymentMethod.getDefaultMethodId((activity as MainActivity).helper)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_method_list, container, false)
        val toolbar = view.scrollable_toolbar

        toolbar.title = getString(R.string.payment_methods)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        val recyclerView = view.method_list_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = PaymentMethodAdapter(methods)
        adapter.setOnMethodClickListener(object : PaymentMethodAdapter.OnMethodClickListener {
            override fun onHeaderClick() {
                showAddMethodDialog()
            }

            override fun onItemClick(position: Int) {
                showContextMenu(position)
            }
        })

        recyclerView.adapter = adapter

        return view
    }

    private fun showContextMenu(position: Int) {
        val method = methods[position] as PaymentMethod
        val fragment = ContextMenuDialogFragment.newInstance(
            R.menu.payment_method_context_menu,
            method.isDefault
        )

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_method_set_default -> setDefault(method, position)
                    R.id.menu_method_edit -> showEditMethodDialog(method, position)
                    R.id.menu_method_delete -> showConfirmationDialog(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showConfirmationDialog(position: Int) {
        val fragment = ConfirmationDialogFragment.newInstance("Are you sure you want to delete this method? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deleteMethod(position) }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deleteMethod(index: Int) {
        val method = methods.removeAt(index) as PaymentMethod
        adapter.notifyItemRemoved(index)

        if (method.isDefault) {
            idDefault = -1
            PaymentMethod.updateDefaultMethod((activity as MainActivity).helper, -1)
        }

        PaymentMethod.deleteMethod((activity as MainActivity).helper, method.id)
    }

    private fun showEditMethodDialog(method: PaymentMethod, position: Int) {
        val fragment = AddMethodDialogFragment.newInstance(method.name)
        fragment.setOnAddMethodListener(object : AddMethodDialogFragment.OnAddMethodFragmentInteractionListener {
            override fun editButtonClicked(name: String) {
                editMethod(method, name, position)
            }

            override fun isNameUnique(name: String) = isUnique(name)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun editMethod(method: PaymentMethod, name: String, position: Int) {
        method.name = name
        adapter.notifyItemChanged(position)

        PaymentMethod.updateMethod((activity as MainActivity).helper, method)

        listener?.methodEdited(method)
    }

    private fun setDefault(method: PaymentMethod, position: Int) {
        if (idDefault == -1) {
            method.isDefault = true
            idDefault = method.id
        } else {
            if (method.id == idDefault) {
                method.isDefault = false
                idDefault = -1
            } else {
                val oldPosition = getMethodIndexById(idDefault)
                val oldDefault = methods[oldPosition] as PaymentMethod
                oldDefault.isDefault = false
                method.isDefault = true
                idDefault = method.id
                adapter.notifyItemChanged(oldPosition)
            }
        }

        PaymentMethod.updateDefaultMethod((activity as MainActivity).helper, idDefault)

        adapter.notifyItemChanged(position)
    }

    private fun getMethodIndexById(id: Int): Int {
        for (i in 1..methods.size) {
            if ((methods[i] as PaymentMethod).id == id) {
                return i
            }
        }

        return -1
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun initMethods() {
        methods = mutableListOf()
        methods.add(PaymentMethodHeader())
        methods.addAll(PaymentMethod.getAllMethods((activity as MainActivity).helper))
    }

    private fun showAddMethodDialog() {
        val fragment = AddMethodDialogFragment()
        fragment.setOnAddMethodListener(object :
            AddMethodDialogFragment.OnAddMethodFragmentInteractionListener {
            override fun addButtonClicked(method: PaymentMethod) {
                addMethod(method)
            }

            override fun isNameUnique(name: String) = isUnique(name)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun addMethod(method: PaymentMethod) {
        val insertedMethod =
            PaymentMethod.insertMethod((activity as MainActivity).helper, method)
        methods.add(1, insertedMethod)
        adapter.notifyItemInserted(1)

        if (insertedMethod.isDefault) {
            setDefault(insertedMethod, 1)
        }
    }

    private fun isUnique(name: String): Boolean {
        for (method in methods) {
            if (method is PaymentMethod) {
                if (method.name.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())) {
                    return false
                }
            }
        }

        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val fragment = (context as MainActivity).supportFragmentManager.findFragmentByTag(TransactionListFragment::class.java.name)

        if (fragment is OnPaymentMethodEditedListener) {
            listener = fragment
        } else {
            throw RuntimeException(fragment.toString() + " must implement OnPaymentMethodEditedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDeleteSuccess(code: Int, id: Int) {

    }

    override fun onSuccess(resp: PaymentMethodApiModel) {

    }

    override fun onSuccessList(list: List<PaymentMethodApiModel>) {

    }

    override fun onError(error: HttpException) {

    }

    interface OnPaymentMethodEditedListener {
        fun methodEdited(method: PaymentMethod)
    }
}
