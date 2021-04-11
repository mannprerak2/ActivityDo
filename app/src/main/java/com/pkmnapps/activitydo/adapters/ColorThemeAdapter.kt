package com.pkmnapps.activitydo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.MConstants
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace
import org.junit.runner.RunWith

class ColorThemeAdapter(val homeFragInterace: HomeFragInterace?) : RecyclerView.Adapter<ColorThemeAdapter.MyViewHolder?>() {
    class MyViewHolder(view: View?) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.color_circle, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(Color.parseColor(MConstants.colors[holder.adapterPosition]))
        holder.itemView.setOnClickListener { homeFragInterace.changeColorTheme(holder.adapterPosition) }
    }

    override fun getItemCount(): Int {
        return MConstants.colors.size
    }
}