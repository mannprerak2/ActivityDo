package com.pkmnapps.activitydo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pkmnapps.activitydo.databasehelpers.DBHelper;
import com.pkmnapps.activitydo.dataclasses.ActivityData;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TabFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        QuickNotesFragment.OnFragmentInteractionListener
{   private FirebaseAnalytics mFirebaseAnalytics;
    boolean home = true;
    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menuAddPinned();

        navigationView.setCheckedItem(R.id.nav_home);
        //Set home fragment to load first here
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_parent_main,new TabFragment(),"back").commit();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    @SuppressWarnings("StatementWithEmptyBody")
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
