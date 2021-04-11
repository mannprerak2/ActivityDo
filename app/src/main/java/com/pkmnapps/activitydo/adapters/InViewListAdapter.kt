package com.pkmnapps.activitydo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.dataclasses.ListItem
import org.junit.runner.RunWith

class InViewListAdapter(private val listItems: MutableList<ListItem?>?) : RecyclerView.Adapter<InViewListAdapter.MyViewHolder?>() {
    class MyViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox?
        val textView: TextView?

        init {
            checkBox = view.findViewById(R.id.checkBox)
            textView = view.findViewById(R.id.contentTextView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_in_view_list_view, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val listItem = listItems.get(position)
        holder.checkBox.setChecked(listItem.getChecked())
        holder.textView.setText(listItem.getContent())
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}