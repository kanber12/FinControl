package by.kanber.fincontrol.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import by.kanber.fincontrol.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_context_menu_dialog.view.*

class ContextMenuDialogFragment : BottomSheetDialogFragment() {
    private lateinit var navigationView: NavigationView
    private var listener: OnItemSelectedListener? = null
    var resId: Int = 0
    var isChange: Boolean = false

    companion object {
        fun newInstance(resId: Int): ContextMenuDialogFragment {
            val args = Bundle()
            args.putInt("res", resId)

            return putArgs(args)
        }

        fun newInstance(resId: Int, isChange: Boolean): ContextMenuDialogFragment {
            val args = Bundle()
            args.putInt("res", resId)
            args.putBoolean("change", isChange)

            return putArgs(args)
        }

        private fun putArgs(args: Bundle): ContextMenuDialogFragment {
            val fragment = ContextMenuDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resId = arguments?.getInt("res") ?: 0
        isChange = arguments?.getBoolean("change") ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_context_menu_dialog, container, false)

        navigationView = view.context_menu_navigation_view

        navigationView.inflateMenu(resId)

        if (isChange) {
            changeMenuItem(navigationView.menu)
        }

        navigationView.setNavigationItemSelectedListener {
            listener?.itemSelected(it.itemId)
            dismiss()

            true
        }

        return view
    }

    private fun changeMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_method_set_default)
        item.title = "Unset as default"
    }

    interface OnItemSelectedListener {
        fun itemSelected(itemId: Int)
    }
}