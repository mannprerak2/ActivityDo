package com.pkmnapps.activitydo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.pkmnapps.activitydo.MainActivity
import com.pkmnapps.activitydo.databasehelpers.DBHelper
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage
import com.pkmnapps.activitydo.databasehelpers.DBHelperText
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TabFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, QuickNotesFragment.OnFragmentInteractionListener {
    var home = true
    var navigationView: NavigationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        receiveIntentForAction()
        val drawer = findViewById<DrawerLayout?>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        menuAddPinned()
        navigationView.setCheckedItem(R.id.nav_home)
        //Set home fragment to load first here
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_parent_main, TabFragment(), "back").commit()
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun receiveIntentForAction() {
        // Get intent, action and MIME type
        val intent = intent
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleRecievedText(intent)
            } else if (type.startsWith("image/")) {
                handleRecievedImage(intent) // Handle single image being sent
            } else {
                Toast.makeText(this, "Sorry, An Error Occured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleRecievedText(intent: Intent?) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val titleText = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        if (sharedText != null) {
            //add to database
            val uid = System.currentTimeMillis().toString()
            DBHelperText(this).insertText(uid, "0", titleText, sharedText)
            DBHelperWidgets(this).insertWidget(uid, 0)
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleRecievedImage(intent: Intent?) {
        val imageUri = intent.getParcelableExtra<Uri?>(Intent.EXTRA_STREAM)
        if (imageUri != null) {
            val uid = System.currentTimeMillis().toString()
            try {
                val file = createImageFile(uid)
                val input = contentResolver.openInputStream(imageUri)
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
                DBHelperImage(this).insertImage(uid, "0", Uri.fromFile(file).toString())
                DBHelperWidgets(this).insertWidget(uid, 0)
                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
            } catch (ignored: Exception) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun createImageFile(tempUid: String?): File? {
        // Create an image file name
        val imageFileName = "$tempUid.jpg"
        return File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName)
    }

    fun menuAddPinned() {
        //add items to nav menu
        val menu = navigationView.getMenu()
        val subMenu = menu.findItem(R.id.nav_pinned).subMenu
        subMenu.clear()
        val d = resources.getDrawable(R.drawable.ic_label_white_24dp)
        for (a in DBHelper(this@MainActivity).allActivitiesAsList) {
            if (a.pinned) { //only pinned
                subMenu.add(a.name).setIcon(d).setOnMenuItemClickListener { //open that activity
                    //open openActivity and send this data to it
                    val bundle = Bundle()
                    bundle.putString("name", a.name)
                    bundle.putString("id", a.id)
                    bundle.putString("color", a.color)
                    bundle.putBoolean("pinned", a.pinned)
                    val i = Intent(this@MainActivity, TaskActivity::class.java)
                    i.putExtra("activityData", bundle)
                    startActivity(i)
                    false
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout?>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (home) super.onBackPressed() else {
                //load home frag here
                //Set home fragment to load  here
                navigationView.setCheckedItem(R.id.nav_home)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem?): Boolean {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Handle navigation view item clicks here.
        val id = item.getItemId()
        home = false
        if (id == R.id.nav_home) {
            home = true
            fragmentTransaction.replace(R.id.fragment_parent_main, TabFragment()).commit()
        } else if (id == R.id.nav_share) {
            try {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "text/plain"
                i.putExtra(Intent.EXTRA_SUBJECT, "Activity Do")
                var sAux = "\nTry this app 'Activity Do' to note down stuff, organise, plan stuff in your life as Activities\n\n"
                sAux = """
                    ${sAux}https://play.google.com/store/apps/details?id=com.pkmnapps.activitydo
                    
                    
                    """.trimIndent()
                i.putExtra(Intent.EXTRA_TEXT, sAux)
                startActivity(Intent.createChooser(i, "choose one"))
            } catch (ignored: Exception) {
            }
        } else if (id == R.id.nav_rate) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.pkmnapps.activitydo")
            startActivity(intent)
        }
        val drawer = findViewById<DrawerLayout?>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onFragmentInteraction(uri: Uri?) {}
}