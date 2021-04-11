package com.pkmnapps.activitydo

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pkmnapps.activitydo.adapters.SimpleActivityAdapter
import com.pkmnapps.activitydo.custominterfaces.ChangeActivityInterface
import com.pkmnapps.activitydo.databasehelpers.DBHelper
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage
import com.pkmnapps.activitydo.databasehelpers.DBHelperList
import com.pkmnapps.activitydo.databasehelpers.DBHelperText
import com.pkmnapps.activitydo.dataclasses.ActivityData
import org.junit.runner.RunWith
import java.util.*

class ActivityChoser : AppCompatActivity(), ChangeActivityInterface {
    var recyclerView: RecyclerView? = null
    var simpleActivityAdapter: SimpleActivityAdapter? = null
    var activityDataList: MutableList<ActivityData?>? = null
    var type = 0
    var uid: String? = null
    var aid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choser)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true)
        val action = intent.getIntExtra("action", -1)
        if (action == MConstants.ACTION_MOVE_WIDGET) {
            uid = intent.getStringExtra("uid")
            aid = intent.getStringExtra("aid")
            type = intent.getIntExtra("type", -1)
            if (uid == null || uid == "") errorQuit() else {
                initialiseRecyclerview()
            }
        } else {
            errorQuit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // app icon in action bar clicked; goto parent activity.
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initialiseRecyclerview() {
        recyclerView = findViewById(R.id.recycler_view)
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setNestedScrollingEnabled(false)
        activityDataList = ArrayList()
        val dbHelper = DBHelper(this)
        activityDataList = dbHelper.allActivitiesAsList
        activityDataList.add(0, ActivityData("0", "Quick Notes", "#38444b"))
        simpleActivityAdapter = SimpleActivityAdapter(activityDataList, this)
        recyclerView.setAdapter(simpleActivityAdapter)
    }

    private fun errorQuit() {
        setResult(RESULT_CANCELED)
        Toast.makeText(this, "Action not supported", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun changeActivity(aid: String?) {
        if (this.aid != aid) {
            setResult(RESULT_OK, intent)
            when (type) {
                MConstants.textW -> DBHelperText(this).updateAid(uid, aid)
                MConstants.imageW -> DBHelperImage(this).updateAid(uid, aid)
                MConstants.listW -> DBHelperList(this).updateAid(uid, aid)
            }
            if (aid == "0") { //used to update quicknotesfragment only if it was transferred there
                QuickNotesFragment.Companion.activityMovedHere = true
            }
        }
        finish()
    }
}