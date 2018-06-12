package com.pkmnapps.activitydo.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pkmnapps.activitydo.ImageViewFullscreen;
import com.pkmnapps.activitydo.MConstants;
import com.pkmnapps.activitydo.R;
import com.pkmnapps.activitydo.custominterfaces.TaskActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems;
import com.pkmnapps.activitydo.dataclasses.ImageWidget;
import com.pkmnapps.activitydo.dataclasses.ListWidget;
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;
import com.pkmnapps.activitydo.dataclasses.Widget;

import java.util.List;

public class ActivityContentAdapter extends RecyclerView.Adapter{

    List<Widget> widgets;
    TaskActivityInterface taskActivityInterface;
    public class MyTextViewHolder extends RecyclerView.ViewHolder {
        public TextView head,body;
        public ImageButton more;
        public MyTextViewHolder(View view) {
            super(view);
            head = (TextView)view.findViewById(R.id.head_textView);
            body = (TextView)view.findViewById(R.id.body_textView);
            more = (ImageButton)view.findViewById(R.id.more_button);
        }
    }
    public class MyListViewHolder extends RecyclerView.ViewHolder {
        public TextView head;
        public RecyclerView recyclerView;
        public ImageButton more;

        public MyListViewHolder(View view) {
            super(view);
            head = (TextView)view.findViewById(R.id.head_textView);
            recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
            more = (ImageButton)view.findViewById(R.id.more_button);

        }
    }
    public class MyImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageButton more;

        public MyImageViewHolder(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.widget_imageView);
            more = (ImageButton)view.findViewById(R.id.more_button);

        }
    }
    public class MyAudioViewHolder extends RecyclerView.ViewHolder {

        public MyAudioViewHolder(View view) {
            super(view);

        }
    }

    public ActivityContentAdapter(List<Widget> widgets, TaskActivityInterface taskActivityInterface) {
        this.widgets = widgets;
        this.taskActivityInterface = taskActivityInterface;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case MConstants.textW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_note_view, parent, false);
                return new MyTextViewHolder(v);
            case MConstants.listW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_in_task_view, parent, false);
                return new MyListViewHolder(v);
            case MConstants.imageW:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_widget_view, parent, false);
                return new MyImageViewHolder(v);
            case MConstants.audioW:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        final Object widget = widgets.get(position).getObject();
        if(widget!=null && type!=0){
            switch (type){
                case MConstants.textW:
                    final SimpleTextWidget t = (SimpleTextWidget)widget;
                    ((MyTextViewHolder)holder).head.setText(t.getHead());
                    ((MyTextViewHolder)holder).body.setText(t.getBody());
                    ((MyTextViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskActivityInterface.editWidget(widgets.get(holder.getAdapterPosition()));
                        }
                    });
                    ((MyTextViewHolder)holder).more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),((MyTextViewHolder)holder).more);

                            popupMenu.getMenuInflater().inflate(R.menu.recycler_task_menu,popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId()==R.id.action_delete)
                                        taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition()));
                                    else if(item.getItemId()==R.id.action_change_activity){
                                        taskActivityInterface.changeActivtyOfWidget(MConstants.textW,t.getUid(),holder.getAdapterPosition());
                                    }
                                    return true;
                                }
                            });
                            popupMenu.show();
                        }
                    });
                    break;
                case MConstants.listW:
                    final ListWidget l = (ListWidget)widget;
                    ((MyListViewHolder)holder).head.setText(l.getHead());
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(((MyListViewHolder)holder).itemView.getContext(),LinearLayoutManager.VERTICAL,false);
                    ((MyListViewHolder)holder).recyclerView.setLayoutManager(mLayoutManager);
                    ((MyListViewHolder)holder).recyclerView.setItemAnimator(new DefaultItemAnimator());
                    ((MyListViewHolder)holder).recyclerView.setNestedScrollingEnabled(false);
                            ((MyListViewHolder)holder).recyclerView
                            .setAdapter(new InViewListAdapter(new DBHelperListItems(((MyListViewHolder)holder).itemView.getContext())
                                    .get8ListItemsAsList(l.getUid())));
                    ((MyListViewHolder)holder).recyclerView.setLayoutFrozen(true);

                    ((MyListViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskActivityInterface.editWidget(widgets.get(holder.getAdapterPosition()));
                        }
                    });
                    ((MyListViewHolder)holder).more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),((MyListViewHolder)holder).more);

                            popupMenu.getMenuInflater().inflate(R.menu.recycler_task_menu,popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId()==R.id.action_delete)
                                        taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition()));
                                    else if(item.getItemId()==R.id.action_change_activity){
                                        taskActivityInterface.changeActivtyOfWidget(MConstants.listW,l.getUid(),holder.getAdapterPosition());
                                    }
                                    return true;
                                }
                            });
                            popupMenu.show();
                        }
                    });
                    break;
                case MConstants.imageW:
                    final ImageWidget i = (ImageWidget)widget;

                    Glide.with(((MyImageViewHolder)holder).imageView.getContext())
                            .load(Uri.parse(i.getImageUri()))
                            .into(((MyImageViewHolder)holder).imageView);
                    ((MyImageViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //open it in fullscreen
                            Intent intent = new Intent(((MyImageViewHolder)holder).itemView.getContext(), ImageViewFullscreen.class);
                            intent.putExtra("image", i.getImageUri());
                            ((MyImageViewHolder)holder).itemView.getContext().startActivity(intent);
                        }
                    });
                    ((MyImageViewHolder)holder).more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),((MyImageViewHolder)holder).more);

                            popupMenu.getMenuInflater().inflate(R.menu.recycler_task_menu,popupMenu.getMenu());
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId()==R.id.action_delete)
                                        taskActivityInterface.deleteWidget(widgets.get(holder.getAdapterPosition()));
                                    else if(item.getItemId()==R.id.action_change_activity){
                                        taskActivityInterface.changeActivtyOfWidget(MConstants.imageW,i.getUid(),holder.getAdapterPosition());
                                    }
                                    return true;
                                }
                            });
                            popupMenu.show();
                        }
                    });
                    break;
                case MConstants.audioW:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return widgets.size();
    }

    @Override
    public int getItemViewType(int position) {
        return widgets.get(position).getType();
    }


}
