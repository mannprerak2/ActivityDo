package com.pkmnapps.activitydo.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pkmnapps.activitydo.R;

import com.pkmnapps.activitydo.custominterfaces.ListActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelperList;
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems;
import com.pkmnapps.activitydo.dataclasses.ListItem;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private List<ListItem> listItems;
    ListActivityInterface listActivityInterface;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public EditText content;
        public CheckBox checked;
        public ImageButton delete;
        public MyViewHolder(View view) {
            super(view);
            content = (EditText) view.findViewById(R.id.contentEditText);
            checked = (CheckBox)view.findViewById(R.id.checkbox);
            delete = (ImageButton)view.findViewById(R.id.deleteButton);
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

        return new ListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListAdapter.MyViewHolder holder, final int position) {
        final ListItem listItem = listItems.get(position);

        holder.content.setText(listItem.getContent());
        holder.checked.setChecked(listItem.getChecked());
        holder.checked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checked.isChecked())
                    listItem.setChecked(true);
                else
                    listItem.setChecked(false);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove from database if exists
                new DBHelperListItems(holder.itemView.getContext()).deleteListItem(listItem.getUid());
                listItems.remove(listItem);
                listActivityInterface.deleteListItem(listItem);
                notifyDataSetChanged();
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
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

}