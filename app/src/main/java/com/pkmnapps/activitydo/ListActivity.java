package com.pkmnapps.activitydo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pkmnapps.activitydo.adapters.ListAdapter;
import com.pkmnapps.activitydo.custominterfaces.ListActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelperList;
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems;
import com.pkmnapps.activitydo.dataclasses.ListItem;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements ListActivityInterface {
    String lid,head;//lid is recieved
    EditText headE;
    Intent data;

    List<ListItem> listItems;
    ListAdapter listAdapter;
    RecyclerView recyclerView;
    FirebaseAnalytics firebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        final TextView addItem = (TextView)findViewById(R.id.addItemTextView);

        lid = getIntent().getStringExtra("lid");
        head = getIntent().getStringExtra("head");
        data = getIntent();

        headE = (EditText)findViewById(R.id.head_editText);
        headE.setText(head);

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add list
                newListItem();
            }
        });
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ListActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                return false;
            }
        });
        loadData();

        listAdapter = new ListAdapter(listItems, ListActivity.this);
        recyclerView.setAdapter(listAdapter);

        setResult(Activity.RESULT_OK,data);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onBackPressed() {
        clearFocus();
        super.onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                onBackPressed();
                return true;
            case R.id.action_share:
                clearFocus();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(headE.getText().toString());
                stringBuilder.append("\n\n");
                for(ListItem l: listItems){
                    stringBuilder.append("  - ");

                    stringBuilder.append(l.getContent());

                    stringBuilder.append("\n");
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {//will run always no-matter how activity is closed
        clearFocus();
        //save data on pause activity
        head = headE.getText().toString();

        DBHelperList dbHelperList = new DBHelperList(ListActivity.this);
        dbHelperList.updateHead(lid,head);
        DBHelperListItems dbHelperListItems = new DBHelperListItems(ListActivity.this);
        //save listitems
        if(listItems.size()>0) {
            for (ListItem listItem : listItems) {
                if (dbHelperListItems.updateActivity(listItem) == 0)
                    dbHelperListItems.insertListItem(listItem);
            }
        }else {
            if(head.equals(""))
                dbHelperList.deleteList(lid);
        }
        super.onPause();
    }

    @Override
    public void deleteListItem(ListItem listItem) {
        listItems.remove(listItem);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void newListItem() {
        //save previous list first

        listItems.add(new ListItem(String.valueOf(System.currentTimeMillis()), lid,"",false));
        listAdapter.notifyItemInserted(listItems.size() - 1);
    }

    public void loadData(){
        listItems = new ArrayList<>();
        DBHelperListItems dbHelperListItems = new DBHelperListItems(ListActivity.this);
        listItems = dbHelperListItems.getAllListItemsAsList(lid);
    }

    public void clearFocus(){
        View current = getCurrentFocus();
        if (current != null) {
            current.clearFocus();
        }
    }


}
