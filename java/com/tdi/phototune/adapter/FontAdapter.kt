package com.tdi.phototune.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tdi.phototune.R
import java.lang.StringBuilder

/**
 * Created by Armen Mkhitaryan on 24.10.2019.
 */
class FontAdapter(internal var context: Context,
                  internal var listener: FontAdapterClickListener
):
    RecyclerView.Adapter<FontAdapter.FontViewHolder>() {

    var rowSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.font_item, parent, false)
        return FontViewHolder(itemView = itemView)
    }

    override fun getItemCount(): Int {
        return fontList.size
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {

        if (rowSelected == position)
            holder.imgCheck.visibility = View.VISIBLE
        else
            holder.imgCheck.visibility = View.INVISIBLE

        val typeFace = Typeface.createFromAsset(context.assets, StringBuilder("fonts/")
            .append(fontList.get(position)).toString())
            holder.txtFontName.text = fontList.get(position)
            holder.txtFontDemo.typeface = typeFace
    }

    internal var fontList: List<String>

    init {
        this.fontList = loadFontList()!!
    }

    private fun loadFontList(): List<String>? {
        val result = ArrayList<String>()
        result.add("Cheque-Black.otf")
        result.add("Cheque-Regular.otf")
        return result
    }

    interface FontAdapterClickListener{

        fun onFontSelected(fontName: String)
    }

    inner class FontViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        internal var txtFontDemo: TextView = itemView.findViewById(R.id.txt_font_demo)
        internal var txtFontName: TextView = itemView.findViewById(R.id.txt_font_name)
        internal var imgCheck: ImageView = itemView.findViewById(R.id.img_check)

        init {
            itemView.setOnClickListener{
                listener.onFontSelected(fontName = fontList[adapterPosition])
                rowSelected = adapterPosition
                notifyDataSetChanged()
            }
        }
    }
}