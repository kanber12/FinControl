package by.kanber.fincontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import by.kanber.fincontrol.base.PaymentMethodListItem
import by.kanber.fincontrol.model.PaymentMethod
import by.kanber.fincontrol.util.TextUtil
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.method_list_item.view.*

class PaymentMethodAdapter(private val methods: List<PaymentMethodListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_HEADER = R.layout.method_list_item_add
    val TYPE_ITEM = R.layout.method_list_item
    private var listener: OnMethodClickListener? = null

    fun setOnMethodClickListener(listener: OnMethodClickListener) {
        this.listener = listener
    }

    class HeaderViewHolder(v: View, listener: OnMethodClickListener?) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener {
                listener?.onHeaderClick()
            }
        }
    }

    class ItemViewHolder(v: View, listener: OnMethodClickListener?) : RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.method_name_text_view
        val defaultMarkHolder: FrameLayout = v.method_default_mark_holder
        val methodCardView: MaterialCardView = v.method_card_view

        init {
            methodCardView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            TYPE_ITEM -> ItemViewHolder(view, listener)
            else -> HeaderViewHolder(view, listener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val method = methods[position] as PaymentMethod

            holder.nameTextView.text = method.name
            holder.defaultMarkHolder.visibility = if (method.isDefault) View.VISIBLE else View.GONE
            holder.methodCardView.strokeWidth = if (method.isDefault) 7 else 0
        }
    }

    override fun getItemCount(): Int = methods.size

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_HEADER else TYPE_ITEM

    interface OnMethodClickListener {
        fun onItemClick(position: Int)
        fun onHeaderClick()
    }
}