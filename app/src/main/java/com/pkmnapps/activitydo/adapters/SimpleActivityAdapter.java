package com.pkmnapps.activitydo.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmnapps.activitydo.R;
import com.pkmnapps.activitydo.custominterfaces.ChangeActivityInterface;
import com.pkmnapps.activitydo.dataclasses.ActivityData;

import java.util.List;

public class SimpleActivityAdapter extends RecyclerView.Adapter<SimpleActivityAdapter.MyViewHolder> {

    private List<ActivityData> activityDataList;
    private ChangeActivityInterface changeActivityInterface;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CheckBox pinned;
        public ImageButton more;
        public ImageView label;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView)view.findViewById(R.id.nameTextView);
            pinned = (CheckBox)view.findViewById(R.id.pinCheckBox);
            label = (ImageView) view.findViewById(R.id.colorView);
            more = (ImageButton)view.findViewById(R.id.more_button);
        }
    }

    public SimpleActivityAdapter(List<ActivityData> activityDataList, ChangeActivityInterface changeActivityInterface) {
        this.activityDataList = activityDataList;
        this.changeActivityInterface = changeActivityInterface;
    }

    @NonNull
    @Override
    public SimpleActivityAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_activity_layout, parent, false);

        return new SimpleActivityAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SimpleActivityAdapter.MyViewHolder holder, int position) {
        final ActivityData activityData = activityDataList.get(position);
        holder.pinned.setChecked(activityData.getPinned());
        holder.name.setText(activityData.getName());
        holder.label.setColorFilter(Color.parseColor(activityData.getColor()), PorterDuff.Mode.SRC_ATOP);

        holder.pinned.setEnabled(false);
        holder.more.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changeActivityInterface!=null)
                    changeActivityInterface.changeActivity(activityData.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityDataList.size();
    }


}
