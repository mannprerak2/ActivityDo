package com.pkmnapps.activitydo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.databasehelpers.DBHelperImage;
import com.pkmnapps.activitydo.databasehelpers.DBHelperText;
import com.pkmnapps.activitydo.databasehelpers.DBHelperWidgets;
import com.pkmnapps.activitydo.dataclasses.ActivityData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TabFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        QuickNotesFragment.OnFragmentInteractionListener
{
    boolean home = true;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        receiveIntentForAction();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menuAddPinned();

        navigationView.setCheckedItem(R.id.nav_home);
        //Set home fragment to load first here
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_parent_main,new TabFragment(),"back").commit();

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
    private void receiveIntentForAction(){
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if("text/plain".equals(type)){
                handleRecievedText(intent);
            }else if (type.startsWith("image/")) {
                handleRecievedImage(intent); // Handle single image being sent
            }
            else{
                Toast.makeText(this,"Sorry, An Error Occured",Toast.LENGTH_SHORT).show();
            }
        }
    }
    void handleRecievedText(Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String titleText = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (sharedText != null) {
            //add to database
            String uid = String.valueOf(System.currentTimeMillis());
            new DBHelperText(this).insertText(uid,"0",titleText,sharedText);
            new DBHelperWidgets(this).insertWidget(uid,0);
            Toast.makeText(this,"Saved successfully",Toast.LENGTH_SHORT).show();
        }
    }
    void handleRecievedImage(Intent intent){
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            String uid = String.valueOf(System.currentTimeMillis());
            try {
                File file = createImageFile(uid);
                InputStream input = getContentResolver().openInputStream(imageUri);
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                }
                new DBHelperImage(this).insertImage(uid,"0",Uri.fromFile(file).toString());
                new DBHelperWidgets(this).insertWidget(uid,0);
                Toast.makeText(this,"Saved successfully",Toast.LENGTH_SHORT).show();
            }catch (Exception ignored){
                Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private File createImageFile(String tempUid) {
        // Create an image file name
        String imageFileName = tempUid + ".jpg";
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),imageFileName);
    }
    public void menuAddPinned(){
        //add items to nav menu
        Menu menu = navigationView.getMenu();
        SubMenu subMenu = menu.findItem(R.id.nav_pinned).getSubMenu();
        subMenu.clear();
        Drawable d = getResources().getDrawable(R.drawable.ic_label_white_24dp);
        for(final ActivityData a:new DBHelper(MainActivity.this).getAllActivitiesAsList()){
            if(a.getPinned()) {//only pinned
                subMenu.add(a.getName()).setIcon(d).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //open that activity
                        //open openActivity and send this data to it
                        Bundle bundle = new Bundle();
                        bundle.putString("name", a.getName());
                        bundle.putString("id", a.getId());
                        bundle.putString("color", a.getColor());
                        bundle.putBoolean("pinned", a.getPinned());

                        Intent i = new Intent(MainActivity.this, TaskActivity.class);
                        i.putExtra("activityData", bundle);
                        startActivity(i);
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(home)
                super.onBackPressed();
            else {
                //load home frag here
                //Set home fragment to load  here
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        home = false;
        if (id == R.id.nav_home) {
            home = true;
            fragmentTransaction.replace(R.id.fragment_parent_main,new TabFragment()).commit();
        }
        else if(id == R.id.nav_share){
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Activity Do");
                String sAux = "\nTry this app 'Activity Do' to note down stuff, organise, plan stuff in your life as Activities\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.pkmnapps.activitydo\n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch(Exception ignored) {
            }
        }
        else if(id==R.id.nav_rate){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.pkmnapps.activitydo"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
