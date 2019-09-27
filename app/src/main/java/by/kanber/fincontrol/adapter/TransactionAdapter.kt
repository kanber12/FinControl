package by.kanber.fincontrol.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import by.kanber.fincontrol.model.Transaction
import by.kanber.fincontrol.util.DateUtil
import by.kanber.fincontrol.util.TextUtil
import kotlinx.android.synthetic.main.transaction_list_item.view.*

class TransactionAdapter(private val context: Context, private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    var listener: OnTransactionClickListener? = null

    fun setOnTransactionClickListener(listener: OnTransactionClickListener) {
        this.listener = listener
    }

    class TransactionViewHolder(v: View, listener: OnTransactionClickListener?) :
        RecyclerView.ViewHolder(v) {
        val sumTextView: TextView = v.trans_sum_text_view
        val currencyTextView: TextView = v.trans_currency_text_view
        val placeTextView: TextView = v.trans_place_text_view
        val categoryTextView: TextView = v.trans_category_text_view
        val methodTextView: TextView = v.trans_method_text_view
        val noteTextView: TextView = v.trans_note_text_view
        val dateTextView: TextView = v.trans_date_text_view
        val typeFrameLayout: FrameLayout = v.trans_type_frame_layout

        init {
            v.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onClick(adapterPosition)
                }
            }

            v.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onLongClick(adapterPosition)
                }

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_item, parent, false)
        return TransactionViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val trans = transactions[position]
        holder.sumTextView.text = TextUtil.getViewableSum(trans.sum)
        holder.currencyTextView.text = trans.currency
        holder.placeTextView.text = trans.place
        holder.dateTextView.text = DateUtil.getViewableTime(trans.date)
        val drawable: Drawable?
        val color: Int
        val text: String

        if (trans.type) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_charge_off)
            color = ContextCompat.getColor(context, R.color.colorChargeOff)
            text = TextUtil.getFromMethodName(trans.method.name)
        } else {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_refill)
            color = ContextCompat.getColor(context, R.color.colorRefill)
            text = TextUtil.getToMethodName(trans.method.name)
        }

        if (trans.category.id == 1) {
            holder.categoryTextView.visibility = View.GONE
        } else {
            holder.categoryTextView.text = trans.category.name
            holder.categoryTextView.visibility = View.VISIBLE
        }

        holder.methodTextView.text = text
        drawable?.setBounds(0, 0, 50, 50)
        holder.sumTextView.setCompoundDrawables(drawable, null, null, null)
        holder.sumTextView.setTextColor(color)
        holder.typeFrameLayout.setBackgroundColor(color)


            if (trans.description.isEmpty()) {
            holder.noteTextView.visibility = View.GONE
        } else {
            holder.noteTextView.visibility = View.VISIBLE
            holder.noteTextView.text = trans.description
        }
    }

    override fun getItemCount(): Int = transactions.size

    interface OnTransactionClickListener {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }
}