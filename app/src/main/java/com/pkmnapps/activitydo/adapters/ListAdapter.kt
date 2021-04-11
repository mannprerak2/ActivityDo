package com.pkmnapps.activitydo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.custominterfaces.ListActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems
import com.pkmnapps.activitydo.dataclasses.ListItem
import org.junit.runner.RunWith

class ListAdapter(private val listItems: MutableList<ListItem?>?, val listActivityInterface: ListActivityInterface?) : RecyclerView.Adapter<ListAdapter.MyViewHolder?>() {
    class MyViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val content: EditText?
        val checked: CheckBox?
        val delete: ImageButton?

        init {
            content = view.findViewById(R.id.contentEditText)
            checked = view.findViewById(R.id.checkbox)
            delete = view.findViewById(R.id.deleteButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_listitem_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val listItem = listItems.get(position)
        holder.content.setText(listItem.getContent())
        holder.checked.setChecked(listItem.getChecked())
        holder.checked.setOnClickListener(View.OnClickListener { listItem.setChecked(holder.checked.isChecked()) })
        holder.delete.setOnClickListener(View.OnClickListener { //remove from database if exists
            DBHelperListItems(holder.itemView.context).deleteListItem(listItem.getUid())
            listItems.remove(listItem)
            listActivityInterface.deleteListItem(listItem)
        })
        holder.content.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                listItem.setContent(holder.content.getText().toString())
                holder.delete.setVisibility(View.INVISIBLE)
            } else { //gained focus
                holder.delete.setVisibility(View.VISIBLE)
            }
        })
        holder.content.requestFocus()
    }

    override fun getItemCount(): Int {
        return listItems.size
    }
}