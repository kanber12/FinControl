package by.kanber.fincontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.kanber.fincontrol.R
import kotlinx.android.synthetic.main.place_list_item.view.*

class PlaceAdapter(private val places: List<String>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {
    private var listener: OnPlaceClickListener? = null

    fun setOnPlaceClickListener(listener: OnPlaceClickListener) {
        this.listener = listener
    }

    class PlaceViewHolder(v: View, listener: OnPlaceClickListener?) : RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.place_name_text_view

        init {
            v.place_card_view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onPlaceClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_list_item, parent, false)

        return PlaceViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]

        holder.nameTextView.text = place
    }

    override fun getItemCount() = places.size

    interface OnPlaceClickListener {
        fun onPlaceClick(position: Int)
    }
}