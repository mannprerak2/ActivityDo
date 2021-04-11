package com.pkmnapps.activitydo.adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.custominterfaces.ChangeActivityInterface
import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith

class SimpleActivityAdapter(private val activityDataList: MutableList<ActivityData?>?, private val changeActivityInterface: ChangeActivityInterface?) : RecyclerView.Adapter<SimpleActivityAdapter.MyViewHolder?>() {
    class MyViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val name: TextView?
        val pinned: CheckBox?
        val more: ImageButton?
        val label: ImageView?

        init {
            name = view.findViewById(R.id.nameTextView)
            pinned = view.findViewById(R.id.pinCheckBox)
            label = view.findViewById(R.id.colorView)
            more = view.findViewById(R.id.more_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_activity_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val activityData = activityDataList.get(position)
        holder.pinned.setChecked(activityData.getPinned())
        holder.name.setText(activityData.getName())
        holder.label.setColorFilter(Color.parseColor(activityData.getColor()), PorterDuff.Mode.SRC_ATOP)
        holder.pinned.setEnabled(false)
        holder.more.setVisibility(View.GONE)
        holder.itemView.setOnClickListener { changeActivityInterface?.changeActivity(activityData.getId()) }
    }

    override fun getItemCount(): Int {
        return activityDataList.size
    }
}