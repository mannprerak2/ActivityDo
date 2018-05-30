package com.pkmnapps.activitydo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.pkmnapps.activitydo.databasehelpers.DBHelperText;
import com.pkmnapps.activitydo.dataclasses.SimpleTextWidget;

public class NoteActivity extends AppCompatActivity {
    String uid,head,body;
    EditText headE,bodyE;
    Intent data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uid = getIntent().getStringExtra("uid");
        head = getIntent().getStringExtra("head");
        body = getIntent().getStringExtra("body");
        data = getIntent();

        headE = (EditText)findViewById(R.id.note_head_editText);
        bodyE = (EditText)findViewById(R.id.note_body_editText);
        headE.setText(head);
        bodyE.setText(body);

        setResult(Activity.RESULT_OK,data);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_activity, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    protected void onPause() {
        head = headE.getText().toString();
        body = bodyE.getText().toString();
        DBHelperText dbHelperText = new DBHelperText(NoteActivity.this);
         //save to database
            dbHelperText.updateHeadBody(uid, head, body);

        super.onPause();
    }


}
