package com.pkmnapps.activitydo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.pkmnapps.activitydo.adapters.ListAdapter
import com.pkmnapps.activitydo.custominterfaces.ListActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelperList
import com.pkmnapps.activitydo.databasehelpers.DBHelperListItems
import com.pkmnapps.activitydo.dataclasses.ListItem
import org.junit.runner.RunWith
import java.util.*

class ListActivity : AppCompatActivity(), ListActivityInterface {
    var lid: String? = null
    var head //lid is recieved
            : String? = null
    var headE: EditText? = null
    var data: Intent? = null
    var listItems: MutableList<ListItem?>? = null
    var listAdapter: ListAdapter? = null
    var recyclerView: RecyclerView? = null
    var firebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        val addItem = findViewById<TextView?>(R.id.addItemTextView)
        lid = intent.getStringExtra("lid")
        head = intent.getStringExtra("head")
        data = intent
        headE = findViewById(R.id.head_editText)
        headE.setText(head)
        addItem.setOnClickListener { //add list
            newListItem()
        }
        recyclerView = findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@ListActivity)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setNestedScrollingEnabled(false)
        recyclerView.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> false })
        loadData()
        listAdapter = ListAdapter(listItems, this@ListActivity)
        recyclerView.setAdapter(listAdapter)
        setResult(RESULT_OK, data)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onBackPressed() {
        clearFocus()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // app icon in action bar clicked; goto parent activity.
                onBackPressed()
                true
            }
            R.id.action_share -> {
                clearFocus()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                val stringBuilder = StringBuilder()
                stringBuilder.append(headE.getText().toString())
                stringBuilder.append("\n\n")
                for (l in listItems) {
                    stringBuilder.append("  - ")
                    stringBuilder.append(l.getContent())
                    stringBuilder.append("\n")
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "Share via"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() { //will run always no-matter how activity is closed
        clearFocus()
        //save data on pause activity
        head = headE.getText().toString()
        val dbHelperList = DBHelperList(this@ListActivity)
        dbHelperList.updateHead(lid, head)
        val dbHelperListItems = DBHelperListItems(this@ListActivity)
        //save listitems
        if (listItems.size > 0) {
            for (listItem in listItems) {
                if (dbHelperListItems.updateActivity(listItem) == 0) dbHelperListItems.insertListItem(listItem)
            }
        } else {
            if (head == "") dbHelperList.deleteList(lid)
        }
        super.onPause()
    }

    override fun deleteListItem(listItem: ListItem?) {
        listItems.remove(listItem)
        listAdapter.notifyDataSetChanged()
    }

    override fun newListItem() {
        //save previous list first
        listItems.add(ListItem(System.currentTimeMillis().toString(), lid, "", false))
        listAdapter.notifyItemInserted(listItems.size - 1)
    }

    fun loadData() {
        listItems = ArrayList()
        val dbHelperListItems = DBHelperListItems(this@ListActivity)
        listItems = dbHelperListItems.getAllListItemsAsList(lid)
    }

    fun clearFocus() {
        val current = currentFocus
        current?.clearFocus()
    }
}