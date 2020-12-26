package com.pkmnapps.activitydo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.Toast;

import com.pkmnapps.activitydo.adapters.SimpleActivityAdapter;
import com.pkmnapps.activitydo.custominterfaces.ChangeActivityInterface;
import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage;
import com.pkmnapps.activitydo.databasehelpers.DBHelperList;
import com.pkmnapps.activitydo.databasehelpers.DBHelperText;
import com.pkmnapps.activitydo.dataclasses.ActivityData;

import java.util.ArrayList;
import java.util.List;

public class ActivityChoser extends AppCompatActivity implements ChangeActivityInterface{
    RecyclerView recyclerView;
    SimpleActivityAdapter simpleActivityAdapter;
    List<ActivityData> activityDataList;
    int type;
    String uid,aid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        int action = getIntent().getIntExtra("action",-1);

        if(action==MConstants.ACTION_MOVE_WIDGET){
            uid = getIntent().getStringExtra("uid");
            aid = getIntent().getStringExtra("aid");
            type = getIntent().getIntExtra("type",-1);
            if(uid==null|| uid.equals(""))
                errorQuit();
            else {
                initialiseRecyclerview();
            }
        }
        else {
            errorQuit();
        }


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void initialiseRecyclerview(){
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        activityDataList = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(this);
        activityDataList = dbHelper.getAllActivitiesAsList();
        activityDataList.add(0,new ActivityData("0","Quick Notes","#38444b"));
        simpleActivityAdapter = new SimpleActivityAdapter(activityDataList,this);
        recyclerView.setAdapter(simpleActivityAdapter);
    }

    private void errorQuit() {
        setResult(RESULT_CANCELED);
        Toast.makeText(this,"Action not supported",Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void changeActivity(String aid) {
        if(!this.aid.equals(aid)) {
            setResult(RESULT_OK, getIntent());
            switch (type) {
                case MConstants.textW:
                    new DBHelperText(this).updateAid(uid, aid);
                    break;
                case MConstants.imageW:
                    new DBHelperImage(this).updateAid(uid, aid);
                    break;
                case MConstants.listW:
                    new DBHelperList(this).updateAid(uid, aid);
                    break;
            }
            if(aid.equals("0")){//used to update quicknotesfragment only if it was transferred there
                QuickNotesFragment.activityMovedHere = true;
            }
        }
        finish();
    }
}
