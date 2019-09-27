package by.kanber.fincontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import kotlinx.android.synthetic.main.currency_list_item.view.*

class CurrencyAdapter(private val currencies: List<String>) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {
    private var listener: OnCurrencyClickListener? = null

    fun setOnCurrencyClickListener(listener: OnCurrencyClickListener) {
        this.listener = listener
    }

    class CurrencyViewHolder(v: View, listener: OnCurrencyClickListener?) : RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.currency_name_text_view

        init {
            v.currency_card_view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onCurrencyClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CurrencyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.currency_list_item, parent, false)

        return CurrencyViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        val currency = currencies[position]

        holder.nameTextView.text = currency
    }

    override fun getItemCount() = currencies.size

    interface OnCurrencyClickListener {
        fun onCurrencyClick(position: Int)
    }
}