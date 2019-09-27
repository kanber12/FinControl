package by.kanber.fincontrol.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import by.kanber.fincontrol.base.SpinnerItem
import by.kanber.fincontrol.model.Category
import by.kanber.fincontrol.model.PaymentMethod
import kotlinx.android.synthetic.main.spinner_dropdown_item.view.*
import kotlinx.android.synthetic.main.spinner_item.view.*

class SpinnerAdapter(
    context: Context,
    private val itemResId: Int,
    private val dropdownItemResId: Int,
    private val itemList: MutableList<SpinnerItem>,
    private val isMethod: Boolean
) : ArrayAdapter<SpinnerItem>(context, itemResId, itemList) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(dropdownItemResId, parent, false)
        val nameTextView = view.spinner_dropdown_item_name_text_view
        val item = itemList[position]

        nameTextView.text = item.name

        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(itemResId, parent, false)
        val methodNameTextView = view.spinner_item_name_text_view
        val item = itemList[position]
        val text = if (isMethod) "Method: ${item.name}" else "Category: ${item.name}"

        methodNameTextView.text = text

        return view
    }
}