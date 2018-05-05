package com.logpht.wheremygang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ILocationObserver {
    private User user;
    private GoogleMap mMap;
    private LocationServices locationService;
    private ServiceConnection locationServiceConnection;
    private LocationManager manager;
    private final float userMarkerColor = BitmapDescriptorFactory.HUE_RED;
    private Marker userMarker;
    private final float zoomLevel = 15f;
    private boolean loopSendLocationFail = false; // sendLocationLoopInterval fail
    private static final long SEND_LOCATION_MIN_TIME = 3000; // in miliseconds
    private static final float SEND_LOCATION_MIN_DISTANCE = 50; // in meters
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
        this.user = new User("1", "1", "1", 0, 10.732583049437197, 106.69998977812224);
        this.locationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationServices.LocationServiceBinder locationBinder = (LocationServices.LocationServiceBinder) service;
                locationService = locationBinder.getLocationService();
                locationService.setUserID(user.getPhone());
                locationService.registerObserver(MapActivity.this);
                Log.d("map", "on service connected");
                startSendingLocationLoop();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("map", "on service disconnected");
            }
        };
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
                user.setLatitude(user.getLatitude() + 0.01);
                drawUserLocation();
                moveCameraToMarker(userMarker);
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

    private boolean checkGPSEnabled() {
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void startSendingLocationLoop() {
        if (!checkGPSEnabled()) {
            loopSendLocationFail = true;
            return;
        }
        // start sending and updating location loop
        Log.d("map", "start locationService loop");
        try{
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SEND_LOCATION_MIN_TIME, SEND_LOCATION_MIN_DISTANCE, locationService);
            loopSendLocationFail = false;
        } catch (SecurityException e) {
            loopSendLocationFail = true;
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.d("map", "onstart");
        super.onStart();
        if (loopSendLocationFail) {
            startSendingLocationLoop();
        }
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

    private void updateUserLocation(Location newLocation) {
        Log.d("map", "new location: " + newLocation.getLatitude() + " - " + newLocation.getLongitude());
        this.user.setLatitude(newLocation.getLatitude());
        this.user.setLongitude(newLocation.getLongitude());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("map", "on map ready");
        this.mMap = googleMap;
        drawUserLocation();
        moveCameraToMarker(userMarker);
    }

    private MarkerOptions createMarker(LatLng location, float markerColorFactory, String title) {
        return new MarkerOptions().position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColorFactory))
                .title(title);
    }

    private void moveCameraToMarker(Marker marker) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoomLevel);
        mMap.moveCamera(cameraUpdate);
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
    protected void onResumeFragments() {}

    @Override
    public void handleDataChange(Object data) {
        Location newLocation = (Location) data;
        updateUserLocation(newLocation);
        if (mMap != null) {
            drawUserLocation();
            moveCameraToMarker(userMarker);
        }
    }

    private void drawUserLocation() {
        // remove old marker
        if (userMarker != null) {
            userMarker.remove();
        }
        userMarker = this.mMap.addMarker(createMarker(new LatLng(user.getLatitude(), user.getLongitude()),
                userMarkerColor, user.getName()));
    }
}
