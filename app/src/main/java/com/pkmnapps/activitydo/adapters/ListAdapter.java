package com.pkmnapps.activitydo.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pkmnapps.activitydo.R;

import com.pkmnapps.activitydo.custominterfaces.ListActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems;
import com.pkmnapps.activitydo.dataclasses.ListItem;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private final List<ListItem> listItems;
    final ListActivityInterface listActivityInterface;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final EditText content;
        public final CheckBox checked;
        public final ImageButton delete;
        public MyViewHolder(View view) {
            super(view);
            content = view.findViewById(R.id.contentEditText);
            checked = view.findViewById(R.id.checkbox);
            delete = view.findViewById(R.id.deleteButton);
        }
    }

    public ListAdapter(List<ListItem> listItems, ListActivityInterface listActivityInterface) {
        this.listItems = listItems;
        this.listActivityInterface = listActivityInterface;
    }

    @NonNull
    @Override
    public ListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_listitem_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListAdapter.MyViewHolder holder, final int position) {
        final ListItem listItem = listItems.get(position);

        holder.content.setText(listItem.getContent());
        holder.checked.setChecked(listItem.getChecked());
        holder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listItem.setChecked(holder.checked.isChecked());
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove from database if exists
                new DBHelperListItems(holder.itemView.getContext()).deleteListItem(listItem.getUid());
                listItems.remove(listItem);
                listActivityInterface.deleteListItem(listItem);
            }
        });
        holder.content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    listItem.setContent(holder.content.getText().toString());
                    holder.delete.setVisibility(View.INVISIBLE);
                }
                else {//gained focus
                    holder.delete.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.content.requestFocus();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

}