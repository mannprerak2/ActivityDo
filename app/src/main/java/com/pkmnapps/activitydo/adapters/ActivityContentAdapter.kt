package com.pkmnapps.activitydo.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pkmnapps.activitydo.ImageViewFullscreen
import com.pkmnapps.activitydo.MConstants
import com.pkmnapps.activitydo.R
import com.pkmnapps.activitydo.custominterfaces.TaskActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems
import com.pkmnapps.activitydo.dataclasses.ImageWidget
import com.pkmnapps.activitydo.dataclasses.ListWidget
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget
import com.pkmnapps.activitydo.dataclasses.Widget
import org.junit.runner.RunWith

class ActivityContentAdapter(val widgets: MutableList<Widget?>?, val taskActivityInterface: TaskActivityInterface?) : RecyclerView.Adapter<Any?>() {
    class MyTextViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val head: TextView?
        val body: TextView?
        val more: ImageButton?

        init {
            head = view.findViewById(R.id.head_textView)
            body = view.findViewById(R.id.body_textView)
            more = view.findViewById(R.id.more_button)
        }
    }

    class MyListViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val head: TextView?
        val recyclerView: RecyclerView?
        val more: ImageButton?

        init {
            head = view.findViewById(R.id.head_textView)
            recyclerView = view.findViewById(R.id.recycler_view)
            more = view.findViewById(R.id.more_button)
        }
    }

    class MyImageViewHolder(view: View?) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView?
        val more: ImageButton?

        init {
            imageView = view.findViewById(R.id.widget_imageView)
            more = view.findViewById(R.id.more_button)
        }
    }

    class MyAudioViewHolder(view: View?) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View?
        return when (viewType) {
            MConstants.textW -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.simple_note_view, parent, false)
                MyTextViewHolder(v)
            }
            MConstants.listW -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_list_in_task_view, parent, false)
                MyListViewHolder(v)
            }
            MConstants.imageW -> {
                v = LayoutInflater.from(parent.context).inflate(R.layout.image_widget_view, parent, false)
                MyImageViewHolder(v)
            }
            MConstants.audioW -> null
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = getItemViewType(position)
        val widget = widgets.get(position).getObject()
        if (widget != null && type != 0) {
            when (type) {
                MConstants.textW -> {
                    val t = widget as SimpleTextWidget
                    (holder as MyTextViewHolder).head.setText(t.head)
                    (holder as MyTextViewHolder).body.setText(t.body)
                    (holder as MyTextViewHolder).itemView.setOnClickListener { taskActivityInterface.editWidget(widgets.get(holder.getAdapterPosition())) }
                    (holder as MyTextViewHolder).more.setOnClickListener(View.OnClickListener {
                        val popupMenu = PopupMenu(holder.itemView.context, (holder as MyTextViewHolder).more)
                        popupMenu.menuInflater.inflate(R.menu.recycler_task_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener { item ->
                            if (item.itemId == R.id.action_delete) taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition())) else if (item.itemId == R.id.action_change_activity) {
                                taskActivityInterface.changeActivtyOfWidget(MConstants.textW, t.uid, holder.getAdapterPosition())
                            }
                            true
                        }
                        popupMenu.show()
                    })
                }
                MConstants.listW -> {
                    val l = widget as ListWidget
                    (holder as MyListViewHolder).head.setText(l.head)
                    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager((holder as MyListViewHolder).itemView.context, LinearLayoutManager.VERTICAL, false)
                    (holder as MyListViewHolder).recyclerView.setLayoutManager(mLayoutManager)
                    (holder as MyListViewHolder).recyclerView.setItemAnimator(DefaultItemAnimator())
                    (holder as MyListViewHolder).recyclerView.setNestedScrollingEnabled(false)
                    (holder as MyListViewHolder).recyclerView
                            .setAdapter(InViewListAdapter(DBHelperListItems((holder as MyListViewHolder).itemView.context)
                                    .get8ListItemsAsList(l.uid)))
                    (holder as MyListViewHolder).recyclerView.suppressLayout(true)
                    (holder as MyListViewHolder).itemView.setOnClickListener { taskActivityInterface.editWidget(widgets.get(holder.getAdapterPosition())) }
                    (holder as MyListViewHolder).more.setOnClickListener(View.OnClickListener {
                        val popupMenu = PopupMenu(holder.itemView.context, (holder as MyListViewHolder).more)
                        popupMenu.menuInflater.inflate(R.menu.recycler_task_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener { item ->
                            if (item.itemId == R.id.action_delete) taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition())) else if (item.itemId == R.id.action_change_activity) {
                                taskActivityInterface.changeActivtyOfWidget(MConstants.listW, l.uid, holder.getAdapterPosition())
                            }
                            true
                        }
                        popupMenu.show()
                    })
                }
                MConstants.imageW -> {
                    val i = widget as ImageWidget
                    Glide.with((holder as MyImageViewHolder).imageView.getContext())
                            .load(Uri.parse(i.imageUri))
                            .into((holder as MyImageViewHolder).imageView)
                    (holder as MyImageViewHolder).itemView.setOnClickListener { //open it in fullscreen
                        val intent = Intent((holder as MyImageViewHolder).itemView.context, ImageViewFullscreen::class.java)
                        intent.putExtra("image", i.imageUri)
                        (holder as MyImageViewHolder).itemView.context.startActivity(intent)
                    }
                    (holder as MyImageViewHolder).more.setOnClickListener(View.OnClickListener {
                        val popupMenu = PopupMenu(holder.itemView.context, (holder as MyImageViewHolder).more)
                        popupMenu.menuInflater.inflate(R.menu.recycler_task_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener { item ->
                            if (item.itemId == R.id.action_delete) taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition())) else if (item.itemId == R.id.action_change_activity) {
                                taskActivityInterface.changeActivtyOfWidget(MConstants.imageW, i.uid, holder.getAdapterPosition())
                            }
                            true
                        }
                        popupMenu.show()
                    })
                }
                MConstants.audioW -> {
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return widgets.size
    }

    override fun getItemViewType(position: Int): Int {
        return widgets.get(position).getType()
    }
}