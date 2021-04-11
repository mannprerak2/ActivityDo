package com.pkmnapps.activitydo.adapters;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.R;
import com.pkmnapps.activitydo.TaskActivity;
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace;
import com.pkmnapps.activitydo.dataclasses.ActivityData;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.MyViewHolder> {

    private final List<ActivityData> activityDataList;
    private final HomeFragInterace homeFrag;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final CheckBox pinned;
        public final ImageButton more;
        public final ImageView label;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.nameTextView);
            pinned = view.findViewById(R.id.pinCheckBox);
            label = view.findViewById(R.id.colorView);
            more = view.findViewById(R.id.more_button);
        }
    }

    public ActivityAdapter(List<ActivityData> activityDataList,HomeFragInterace homeFrag) {
        this.activityDataList = activityDataList;
        this.homeFrag = homeFrag;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_activity_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final ActivityData activityData = activityDataList.get(position);
        holder.pinned.setChecked(activityData.getPinned());
        holder.name.setText(activityData.getName());
        holder.label.setColorFilter(Color.parseColor(activityData.getColor()), PorterDuff.Mode.SRC_ATOP);
        holder.pinned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper dbHelper = new DBHelper(holder.itemView.getContext());
                if(holder.pinned.isChecked()){
                    dbHelper.pinActivity(activityData.getId(),1);
                }
                else{
                    dbHelper.pinActivity(activityData.getId(),0);
                }
                homeFrag.updatePinnedMenu();
            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),holder.more);

                popupMenu.getMenuInflater().inflate(R.menu.activity_data_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.action_edit)
                            homeFrag.displayEditDialog(activityData);
                        else if(item.getItemId()==R.id.action_delete)
                            homeFrag.displayDeleteDialog(activityData);
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open openActivity and send this data to it
                Bundle bundle = new Bundle();
                bundle.putString("name",activityData.getName());
                bundle.putString("id",activityData.getId());
                bundle.putString("color",activityData.getColor());
                bundle.putBoolean("pinned",activityData.getPinned());

                Intent i = new Intent(holder.itemView.getContext(), TaskActivity.class);
                i.putExtra("activityData",bundle);
                holder.itemView.getContext().startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return activityDataList.size();
    }


}
