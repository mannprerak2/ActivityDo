package com.pkmnapps.activitydo.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.pkmnapps.activitydo.R;
import com.pkmnapps.activitydo.dataclasses.ListItem;

import java.util.List;

public class InViewListAdapter extends RecyclerView.Adapter<InViewListAdapter.MyViewHolder> {

    private final List<ListItem> listItems;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final CheckBox checkBox;
        public final TextView textView;
        public MyViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkBox);
            textView = view.findViewById(R.id.contentTextView);
        }
    }

    public InViewListAdapter(List<ListItem> listItems) {
        this.listItems = listItems;
    }

    @NonNull
    @Override
    public InViewListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_in_view_list_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final InViewListAdapter.MyViewHolder holder, final int position) {
        final ListItem listItem = listItems.get(position);

        holder.checkBox.setChecked(listItem.getChecked());
        holder.textView.setText(listItem.getContent());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

}
