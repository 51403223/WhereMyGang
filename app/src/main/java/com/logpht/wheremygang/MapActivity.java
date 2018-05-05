package com.logpht.wheremygang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {

    // Pha o day nhe
    // hjrehehehe
    private User user;
    private GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(10.8691456, 106.7386724);
        mMap.addMarker(new MarkerOptions().position(sydney).title("My location"));
        CameraUpdate cameraUpdate =CameraUpdateFactory.newLatLngZoom(sydney,15);
        mMap.moveCamera(cameraUpdate);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        user = new User("1", "1", "1", 0, 0, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.content_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            final Dialog createRoom = new Dialog(MapActivity.this);

            createRoom.setTitle("Create Your Room");
            createRoom.setContentView(R.layout.create_room);
            final EditText nameRoom = createRoom.findViewById(R.id.nameRoom);
            final EditText passRoom = createRoom.findViewById(R.id.passRoom);
            Button btn_create = createRoom.findViewById(R.id.btn_createRoom);
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MapActivity.this, nameRoom.getText().toString()+"-" +
                            passRoom.getText().toString(), Toast.LENGTH_SHORT).show();
                    createRoom.dismiss();
                }
            });
            createRoom.show();
            Window window = createRoom.getWindow();
            window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        } else if (id == R.id.nav_gallery) {
            final Dialog joinRoom = new Dialog(MapActivity.this);
            joinRoom.setTitle("Join Room");
            joinRoom.setContentView(R.layout.join_room);
            final EditText idRoom = joinRoom.findViewById(R.id.id_room);
            final EditText passRoom = joinRoom.findViewById(R.id.pass_join_room);
            Button btn_join = joinRoom.findViewById(R.id.btn_joinRoom);
            btn_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MapActivity.this, idRoom.getText().toString() + "-"
                            + passRoom.getText().toString(), Toast.LENGTH_SHORT).show();
                    joinRoom.dismiss();
                }
            });
            joinRoom.show();
            Window window = joinRoom.getWindow();
            window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        } else if (id == R.id.nav_slideshow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("Leave Room");
            builder.setMessage("Do you want to leave this Room ");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // neu no la chu phong ...........

                    // neu no khong la chu phong .............
                    Toast.makeText(MapActivity.this, "You left this Room", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MapActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();


        } else if (id == R.id.nav_manage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("Delte Room");
            builder.setMessage("If you delete this Room, all member in Room will be dismissed. Are you sure to Delete Room?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Toast.makeText(MapActivity.this, "Your Room is deleted", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MapActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();



        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResumeFragments() {

    }
}
