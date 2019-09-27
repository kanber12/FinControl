package by.kanber.fincontrol.transactions.places


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager

import by.kanber.fincontrol.R
import by.kanber.fincontrol.activity.MainActivity
import by.kanber.fincontrol.adapter.PlaceAdapter
import by.kanber.fincontrol.dialog.ConfirmationDialogFragment
import by.kanber.fincontrol.dialog.ContextMenuDialogFragment
import by.kanber.fincontrol.dialog.InputTextDialogFragment
import by.kanber.fincontrol.model.Place
import kotlinx.android.synthetic.main.simple_list_layout.view.*
import kotlinx.android.synthetic.main.toolbar_scrollable.view.*
import java.util.*

class PlaceListFragment : Fragment() {
    private lateinit var places: MutableList<String>
    private lateinit var adapter: PlaceAdapter
    private lateinit var emptyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        places = Place.getAllPlaces((activity as MainActivity).helper)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.simple_list_layout, container, false)

        emptyTextView = view.simple_list_empty_text_view
        emptyTextView.text = getString(R.string.no_places)

        val toolbar = view.scrollable_toolbar
        toolbar.title = getString(R.string.places)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            closeFragment()
        }

        val recyclerView = view.simple_list_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = PlaceAdapter(places)
        adapter.setOnPlaceClickListener(object : PlaceAdapter.OnPlaceClickListener {
            override fun onPlaceClick(position: Int) {
                showContextMenu(position)
            }
        })

        recyclerView.adapter = adapter

        if (places.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showAddPlaceDialog()

        return true
    }

    private fun showContextMenu(position: Int) {
        val fragment = ContextMenuDialogFragment.newInstance(R.menu.delete_edit_menu)

        fragment.setOnItemSelectedListener(object :
            ContextMenuDialogFragment.OnItemSelectedListener {
            override fun itemSelected(itemId: Int) {
                when (itemId) {
                    R.id.menu_edit -> showEditPlaceDialog(position)
                    R.id.menu_delete -> showConfirmationDialog(position)
                }
            }
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun showAddPlaceDialog() {
        val fragment = InputTextDialogFragment.newInstance(getString(R.string.place))

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                addPlace(text)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun addPlace(place: String) {
        Place.insertPlace((activity as MainActivity).helper, place)

        places.add(place)
        adapter.notifyItemInserted(places.size - 1)

        if (places.size == 1) {
            emptyTextView.visibility = View.GONE
        }
    }

    private fun showEditPlaceDialog(position: Int) {
        val fragment = InputTextDialogFragment.newInstance(getString(R.string.place), places[position])

        fragment.setOnInputTextDialogInteractionListener(object :
            InputTextDialogFragment.OnInputTextDialogInteractionListener {
            override fun onPositiveButtonClick(text: String) {
                editPlace(text, position)
            }

            override fun isUnique(text: String) = checkIsUnique(text)
        })

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun editPlace(name: String, position: Int) {
        val old = places[position]
        places[position] = name
        adapter.notifyItemChanged(position)

        Place.updatePlace((activity as MainActivity).helper, old, name)
    }

    private fun showConfirmationDialog(position: Int) {
        val fragment =
            ConfirmationDialogFragment.newInstance("Are you sure you want to delete this place? This action cannot be undone.")
        fragment.setOnConfirmClickListener { deletePlace(position) }

        fragment.show(activity?.supportFragmentManager!!, fragment.javaClass.name)
    }

    private fun deletePlace(position: Int) {
        val place = places.removeAt(position)
        adapter.notifyItemRemoved(position)

        Place.deletePlace((activity as MainActivity).helper, place)

        if (places.size == 0) {
            emptyTextView.visibility = View.VISIBLE
        }
    }

    private fun checkIsUnique(name: String): Boolean {
        for (place in places) {
            if (place.toLowerCase(Locale.getDefault()) == name.toLowerCase(Locale.getDefault())) {
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
