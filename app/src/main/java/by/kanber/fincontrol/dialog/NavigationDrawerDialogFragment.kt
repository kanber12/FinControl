package by.kanber.fincontrol.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import by.kanber.fincontrol.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_navigation_drawer.*
import kotlinx.android.synthetic.main.fragment_dialog_navigation_drawer.view.*

class NavigationDrawerDialogFragment : BottomSheetDialogFragment() {
    private var listener: OnNavigationDrawerItemClickListener? = null

    fun setOnNavigationItemClickListener(listener: OnNavigationDrawerItemClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_navigation_drawer, container, false)

        view.nav_drawer_username_text_view.text = "Kanber"
        view.nav_drawer_email_text_view.text = "kanber12@bk.ru"
        view.nav_drawer_avatar_image_view.setImageResource(R.mipmap.ic_launcher_round)

        view.nav_drawer_navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_menu_archived_trans -> listener?.onArchiveTransactionsClick()
                R.id.nav_menu_categories -> listener?.onCategoriesClick()
                R.id.nav_menu_places -> listener?.onPlacesClick()
                R.id.nav_menu_currencies -> listener?.onCurrenciesClick()
                R.id.nav_menu_item_4 -> listener?.onItem4Click()
                R.id.nav_menu_item_5 -> listener?.onItem5Click()
                R.id.nav_menu_item_6 -> listener?.onItem6Click()
                R.id.nav_menu_item_7 -> listener?.onItem7Click()
                R.id.nav_menu_item_8 -> listener?.onItem8Click()
                R.id.nav_menu_item_10 -> listener?.onItem8Click()
                R.id.nav_menu_item_13 -> listener?.onItem8Click()
            }

            this.dismiss()

            true
        }

        view.nav_drawer_avatar_image_view.setOnClickListener {
            listener?.onUserClick()
        }

        view.nav_drawer_email_text_view.setOnClickListener {
            listener?.onUserClick()
        }

        view.nav_drawer_username_text_view.setOnClickListener {
            listener?.onUserClick()
        }

        view.nav_drawer_close_image_view.setOnClickListener {
            this.dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog

            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            bottomSheetBehavior.setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > 0.5) {
                        nav_drawer_close_image_view.visibility = View.VISIBLE
                    } else {
                        nav_drawer_close_image_view.visibility = View.GONE
                    }
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        dismiss()
                }
            })
        }

        return bottomSheetDialog
    }

    interface OnNavigationDrawerItemClickListener {
        fun onArchiveTransactionsClick()
        fun onCategoriesClick()
        fun onPlacesClick()
        fun onCurrenciesClick()
        fun onItem3Click()
        fun onItem4Click()
        fun onItem5Click()
        fun onItem6Click()
        fun onItem7Click()
        fun onItem8Click()
        fun onItem9Click()
        fun onUserClick()
    }
}