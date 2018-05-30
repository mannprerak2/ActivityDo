package com.pkmnapps.activitydo.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.pkmnapps.activitydo.MConstants;
import com.pkmnapps.activitydo.R;
import com.pkmnapps.activitydo.custominterfaces.HomeFragInterace;

public class ColorThemeAdapter extends RecyclerView.Adapter<ColorThemeAdapter.MyViewHolder> {

    HomeFragInterace homeFragInterace;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }
    }

    public ColorThemeAdapter(HomeFragInterace homeFragInterace) {
        this.homeFragInterace = homeFragInterace;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_circle, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.itemView.setBackgroundColor(Color.parseColor(MConstants.colors[holder.getAdapterPosition()]));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeFragInterace.changeColorTheme(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return MConstants.colors.length;
    }


}

