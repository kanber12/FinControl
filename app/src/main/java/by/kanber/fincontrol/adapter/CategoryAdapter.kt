package by.kanber.fincontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import by.kanber.fincontrol.model.Category
import kotlinx.android.synthetic.main.category_list_item.view.*

class CategoryAdapter(private val categories: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private var listener: ((Int) -> Unit)? = null

    fun setOnCategoryClickListener(listener: (Int) -> Unit) {
        this.listener = listener
    }

    class CategoryViewHolder(v: View, listener: ((Int) -> Unit)?) :
        RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.category_name_text_view

        init {
            v.category_card_view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.invoke(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_list_item, parent, false)

        return CategoryViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.nameTextView.text = category.name
    }

    override fun getItemCount() = categories.size
}