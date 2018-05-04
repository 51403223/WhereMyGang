package com.logpht.wheremygang;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ILocationObserver {
    private User user;
    private GoogleMap mMap;
    private LocationServices locationService;
    private ServiceConnection locationServiceConnection;
    private static final float userMarkerColor = BitmapDescriptorFactory.HUE_RED;
    private MarkerOptions userMarker;
    private static final float zoomLevel = 15f;
    private static final float[] colors = {
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_YELLOW };

    public MapActivity () {
        this.user = new User("1", "1", "1", 0, 20, 15);
        this.locationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationServices.LocationServiceBinder locationBinder = (LocationServices.LocationServiceBinder) service;
                locationService = locationBinder.getLocationService();
                //locationService.setUser(user);
                locationService.setUserID(user.getPhone());
                locationService.registerObserver(MapActivity.this);
                Log.d("map", "on service connected");
                //sendUserLocation();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("map", "on service disconnected");
            }
        };
        Log.d("map", "constructor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("map", "oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //locationService.setContinueLoop(false);
                //locationService.stop();
                Snackbar.make(view, String.format("%s - %s", user.getLatitude(), user.getLongitude()), Snackbar.LENGTH_LONG)
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

        // start location service
        startLocationService();
    }

    @Override
    protected void onStart() {
        Log.d("map", "onstart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d("map", "ondestroy");
        super.onDestroy();
        unbindService(this.locationServiceConnection);
    }

    private void startLocationService() {
        Intent locationIntent = new Intent(this, LocationServices.class);
        bindService(locationIntent, this.locationServiceConnection, BIND_AUTO_CREATE);
    }

    private void sendUserLocation() {
        if (this.locationService != null) {
            Log.d("map", "sending user location");
            locationService.sendUserLocation();
        } else {
            Log.d("map", "can not send user location");
        }
    }

    private void updateUserLocation(Location newLocation) {
        this.user.setLatitude(newLocation.getLatitude());
        this.user.setLongitude(newLocation.getLongitude());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("map", "on map ready");
//        LatLng longan = new LatLng(10.5, 106.43);
//        mMap.addMarker(createMarker(longan, colors[0], "long an"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(longan, zoomLevel);
//        mMap.moveCamera(cameraUpdate);
    }

    private MarkerOptions createMarker(LatLng location, float markerColorFactory, String title) {
        MarkerOptions markerOptions = new MarkerOptions().position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColorFactory))
                .title(title);
        return markerOptions;
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
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResumeFragments() {

    }

    @Override
    public void handleDataChange(Object data) {
        Location newLocation = (Location) data;
        updateUserLocation(newLocation);
        drawUserLocation();
    }

    private void drawUserLocation() {
        userMarker = createMarker(new LatLng(user.getLatitude(), user.getLongitude()), userMarkerColor, user.getName());
        mMap.addMarker(userMarker);
    }
}
