package by.kanber.fincontrol.transactions.categories


import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.CategoryAdapter
import by.kanber.fincontrol.database.ID
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.dialog.InputTextDialogFragment
import by.kanber.fincontrol.model.Category
import by.kanber.fincontrol.transactions.TransactionListFragment
import kotlinx.android.synthetic.main.simple_list_layout.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*
import java.util.*

class CategoryListFragment : Fragment() {
    private lateinit var categories: MutableList<Category>
    private lateinit var adapter: CategoryAdapter
    private lateinit var emptyTextView: TextView
    private var listener: OnCategoryListInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categories = Category.getAllCategories((activity as MainActivity).helper, "where $ID > 1")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.simple_list_layout, container, false)

        emptyTextView = view.simple_list_empty_text_view
        emptyTextView.text = getString(R.string.no_categories)

        val toolbar = view.scrollable_toolbar
        toolbar.title = getString(R.string.categories)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        val recyclerView = view.simple_list_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = CategoryAdapter(categories)

        adapter.setOnCategoryClickListener { position ->
            showContextMenu(position)
        }

        recyclerView.adapter = adapter

        if (categories.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showAddCategoryDialog()

        return true
    }

    private fun showContextMenu(position: Int) {
        val fragment = ContextMenuDialogFragment.newInstance(R.menu.delete_edit_menu)

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_edit -> showEditCategoryDialog(position)
                    R.id.menu_delete -> showConfirmationDialog(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showAddCategoryDialog() {
        val fragment = InputTextDialogFragment.newInstance(getString(R.string.category))

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                addCategory(text)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun addCategory(name: String) {
        val category = Category(name)
        val insertedCategory = Category.insertCategory((activity as MainActivity).helper, category)

        categories.add(insertedCategory)
        adapter.notifyItemInserted(categories.size - 1)

        if (categories.size == 1) {
            emptyTextView.visibility = View.GONE
        }
    }

    private fun showEditCategoryDialog(position: Int) {
        val fragment =
            InputTextDialogFragment.newInstance(getString(R.string.category), categories[position].name)

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                editCategory(text, position)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun editCategory(name: String, position: Int) {
        val category = categories[position]
        category.name = name
        adapter.notifyItemChanged(position)

        Category.updateCategory((activity as MainActivity).helper, category)

        listener?.onCategoryUpdated(category)
    }

    private fun showConfirmationDialog(position: Int) {
        val fragment =
            ConfirmationDialogFragment.newInstance("Are you sure you want to delete this category? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deleteCategory(position) }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deleteCategory(position: Int) {
        val cat = categories.removeAt(position)
        adapter.notifyItemRemoved(position)

        Category.deleteCategory((activity as MainActivity).helper, cat.id)

        listener?.onCategoryDeleted(cat.id)

        if (categories.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }
    }

    private fun checkIsUnique(name: String): Boolean {
        for (category in categories) {
            if (category.name.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())) {
                return false
            }
        }

        return true
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

        if (fragment is OnCategoryListInteractionListener) {
            listener = fragment
        } else {
            throw RuntimeException(fragment.toString() + " must implement OnCategoryListInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnCategoryListInteractionListener {
        fun onCategoryDeleted(id: Int)
        fun onCategoryUpdated(category: Category)
    }
}
