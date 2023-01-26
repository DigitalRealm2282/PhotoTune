package com.tdi.phototune.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.infernal93.photofilter.view.interfaces.FilterListFragmentListener
import com.tdi.phototune.R
import com.tdi.phototune.utils.ThumbnailItem
import kotlinx.android.synthetic.main.thumbnail_list_item.view.*

class ThumbnailAdapter (private val context: Context,
                        private val thumbnailItemList: List<ThumbnailItem>,
                        private val listener: FilterListFragmentListener): RecyclerView.Adapter<ThumbnailAdapter.MyViewHolder>() {


    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return thumbnailItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, pos: Int) {
        val thumbNailItem = thumbnailItemList[pos]
        holder.thumbNail.setImageBitmap(thumbNailItem.image)
        holder.thumbNail.setOnClickListener {
            listener.onFilterSelected(thumbNailItem.filter)
            selectedIndex = holder.adapterPosition
            notifyDataSetChanged()
        }

        holder.filterName.text = thumbNailItem.filterName

        if (selectedIndex == holder.adapterPosition)
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_selected))
        else
            holder.filterName.setTextColor(ContextCompat.getColor(context, R.color.filter_label_normal))
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var thumbNail: ImageView = itemView.thumbnail
        var filterName: TextView = itemView.filter_name
    }
}