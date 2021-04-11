package com.pkmnapps.activitydo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.pkmnapps.activitydo.NoteActivity
import com.pkmnapps.activitydo.databasehelpers.DBHelperText
import org.junit.runner.RunWith
import java.util.*

class NoteActivity : AppCompatActivity() {
    var uid: String? = null
    var head: String? = null
    var body: String? = null
    var headE: EditText? = null
    var bodyE: EditText? = null
    var data: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar).setDisplayHomeAsUpEnabled(true)
        uid = intent.getStringExtra("uid")
        head = intent.getStringExtra("head")
        body = intent.getStringExtra("body")
        data = intent
        headE = findViewById(R.id.note_head_editText)
        bodyE = findViewById(R.id.note_body_editText)
        headE.setText(head)
        bodyE.setText(body)
        setResult(RESULT_OK, data)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                // app icon in action bar clicked; goto parent activity.
                finish()
                true
            }
            R.id.action_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                head = headE.getText().toString()
                body = bodyE.getText().toString()
                sendIntent.putExtra(Intent.EXTRA_TEXT, """
     $head
     $body
     """.trimIndent())
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "Share via"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        head = headE.getText().toString()
        body = bodyE.getText().toString()
        val dbHelperText = DBHelperText(this@NoteActivity)
        //save to database
        if (head == "" && body == "") {
            dbHelperText.deleteText(uid)
        } else {
            dbHelperText.updateHeadBody(uid, head, body)
        }
        super.onPause()
    }
}