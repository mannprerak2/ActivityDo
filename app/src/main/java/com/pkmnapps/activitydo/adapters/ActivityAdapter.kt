package com.pkmnapps.activitydo.adapters

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.TaskActivity
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace
import com.pkmnapps.activitydo.databasehelpers.DBHelper
import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith

class ActivityAdapter(private val activityDataList: MutableList<ActivityData?>?, private val homeFrag: HomeFragInterace?) : RecyclerView.Adapter<ActivityAdapter.MyViewHolder?>() {
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
        holder.pinned.setOnClickListener(View.OnClickListener {
            val dbHelper = DBHelper(holder.itemView.context)
            if (holder.pinned.isChecked()) {
                dbHelper.pinActivity(activityData.getId(), 1)
            } else {
                dbHelper.pinActivity(activityData.getId(), 0)
            }
            homeFrag.updatePinnedMenu()
        })
        holder.more.setOnClickListener(View.OnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.more)
            popupMenu.menuInflater.inflate(R.menu.activity_data_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_edit) homeFrag.displayEditDialog(activityData) else if (item.itemId == R.id.action_delete) homeFrag.displayDeleteDialog(activityData)
                true
            }
            popupMenu.show()
        })
        holder.itemView.setOnClickListener { //open openActivity and send this data to it
            val bundle = Bundle()
            bundle.putString("name", activityData.getName())
            bundle.putString("id", activityData.getId())
            bundle.putString("color", activityData.getColor())
            bundle.putBoolean("pinned", activityData.getPinned())
            val i = Intent(holder.itemView.context, TaskActivity::class.java)
            i.putExtra("activityData", bundle)
            holder.itemView.context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return activityDataList.size
    }
}