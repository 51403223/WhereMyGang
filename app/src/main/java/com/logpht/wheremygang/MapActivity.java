package com.logpht.wheremygang;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, ILocationObserver {
    private User user;
    private GoogleMap mMap;
    private LocationServices locationService;
    private ServiceConnection locationServiceConnection; // connection for location sender
    private LocationManager manager;
    private FloatingActionButton fab;
    private final float zoomLevel = 15f;
    private boolean loopSendLocationFail = false; // sendLocationLoopInterval fail
    private static final long SEND_LOCATION_MIN_TIME = 0; // in miliseconds
    private static final float SEND_LOCATION_MIN_DISTANCE = 5; // in meters
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
    private ReceiveLocationTask receiveLocationTask;
    private ArrayList<User> receivedUsers;
    private ArrayList<Marker> markerList;
    private Response.Listener receiveLocResponse;
    private Response.ErrorListener receiveLocError;
    private static final int RECEIVE_LOCATION_INTERVAL = 5000; // amount of time to request locations, in miliseconds

    public MapActivity () {
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

        // receive user from login page
        this.user = getIntent().getParcelableExtra("user");
        Log.d("map", "received user: " + user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

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
        mapFragment.onResume(); // add this for android 5.0 can work

        //set leave-room menu option enable
        if (user.getJoiningRoomID() == 0) {
            setLeaveRoomEnable(false);
        }

        // start location service
        startLocationService();
        // prepare receive location
        initializeReceiveLocation();
        // hide sms button
        disableSendRoomInfo();
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
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SEND_LOCATION_MIN_TIME,
                    SEND_LOCATION_MIN_DISTANCE, locationService);
            loopSendLocationFail = false;
        } catch (SecurityException e) {
            loopSendLocationFail = true;
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loopSendLocationFail) {
            startSendingLocationLoop();
        }
    }

    @Override
    protected void onStart() {
        Log.d("map", "onstart");
        super.onStart();
        if (mMap != null) {
            startReceiveLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopReceiveLocation();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("map", "on map ready");
        this.mMap = googleMap;
        mMap.setMyLocationEnabled(true); // show user position
        // move camera to user location
        Location location = mMap.getMyLocation();

        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                    location.getLongitude()), zoomLevel));
        }


        if (this.user.getJoiningRoomID() != 0) {
            startReceiveLocation();
        }
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
            handleCreateRoom();
        } else if (id == R.id.nav_gallery) {
            handleJoinRoom();
        } else if (id == R.id.nav_slideshow) {
            handleLeaveRoom();
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
        Toast.makeText(this, "---------------", Toast.LENGTH_LONG).show();
        Location newLocation = (Location) data;
        updateUserLocation(newLocation);
    }

    @Override
    public void handleLocationConnectionLost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle()
    }

    private Marker drawUserLocation(User user, float color) {
        Marker marker = this.mMap.addMarker(createMarker(new LatLng(user.getLatitude(), user.getLongitude()),
                color, user.getName()));
        return marker;
    }

    private void setLeaveRoomEnable(boolean enable) {
        // get leave-room item
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem leaveRoomItem = menu.findItem(R.id.nav_slideshow);
        leaveRoomItem.setEnabled(enable);
    }

    private void handleCreateRoom() {
        final Dialog createRoom = new Dialog(this);
        createRoom.setContentView(R.layout.create_room);
        final EditText nameRoom = createRoom.findViewById(R.id.nameRoom);
        final EditText passRoom = createRoom.findViewById(R.id.passRoom);
        final Button btn_create = createRoom.findViewById(R.id.btn_createRoom);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomName = nameRoom.getText().toString().trim();
                final String roomPassword = passRoom.getText().toString();
                if (roomName.equals("") || roomPassword.equals("")) {
                    Toast.makeText(MapActivity.this, getResources().getString(R.string.missing_input),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    btn_create.setEnabled(false);
                    btn_create.setText(getResources().getString(R.string.requesting_text));
                    btn_create.setBackgroundColor(getResources().getColor(R.color.gray));
                    createRoom.setCanceledOnTouchOutside(false);
                    Response.Listener<String> roomResponseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            // response variable contains id of newly created room
                            if (response.equals("0")) {
                                // create room fail
                                createRoom.setCanceledOnTouchOutside(true);
                                btn_create.setBackgroundColor(getResources().getColor(R.color.orange));
                                btn_create.setText(getResources().getString(R.string.create_text));
                                btn_create.setEnabled(true);
                                Toast.makeText(MapActivity.this,
                                        getResources().getString(R.string.create_room_fail),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                int createRoomId = Integer.parseInt(response);
                                user.setJoiningRoomID(createRoomId);
                                setLeaveRoomEnable(true);
                                // close create-room dialog
                                createRoom.dismiss();
                                // remove old markers and receive new
                                removeMarkers();
                                startReceiveLocation();
                                // ask if user wants to send id & password to friends
                                sendRoomInfoToFriends(response, roomPassword);
                                // add send info to button
                                fab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendRoomInfoToFriends(response, roomPassword);
                                    }
                                });
                                fab.setVisibility(View.VISIBLE);
                            }
                        }
                    };
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            createRoom.setCanceledOnTouchOutside(true);
                            btn_create.setBackgroundColor(getResources().getColor(R.color.orange));
                            btn_create.setText(getResources().getString(R.string.create_text));
                            btn_create.setEnabled(true);
                            Toast.makeText(MapActivity.this, getResources().getString(R.string.error_request_server),
                                    Toast.LENGTH_LONG).show();
                        }
                    };
                    RoomServices roomServices = new RoomServices();
                    roomServices.createRoom(user.getPhone(), roomName, roomPassword, roomResponseListener, errorListener);
                }
            }
        });
        createRoom.show();
        Window window = createRoom.getWindow();
        window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
    }

    private void handleJoinRoom() {
        final Dialog joinRoom = new Dialog(this);
        joinRoom.setContentView(R.layout.join_room);
        final EditText idRoom = joinRoom.findViewById(R.id.id_room);
        final EditText passRoom = joinRoom.findViewById(R.id.pass_join_room);
        final Button btn_join = joinRoom.findViewById(R.id.btn_joinRoom);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String roomId = idRoom.getText().toString().trim();
                String roomPassword = passRoom.getText().toString();
                if (roomId.equals("") || roomPassword.equals("")) {
                    Toast.makeText(MapActivity.this, getResources().getString(R.string.missing_input),
                            Toast.LENGTH_SHORT).show();
                } else {
                    btn_join.setText(getResources().getString(R.string.joining_text));
                    btn_join.setEnabled(false);
                    btn_join.setBackgroundColor(getResources().getColor(R.color.gray));
                    joinRoom.setCanceledOnTouchOutside(false);
                    Response.Listener<String> roomResponseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals(RoomServices.RESULT_SUCCESS)) {
                                // join room success
                                // close join-room dialog
                                user.setJoiningRoomID(Integer.parseInt(roomId));
                                setLeaveRoomEnable(true);
                                disableSendRoomInfo();
                                joinRoom.dismiss();
                                Toast.makeText(MapActivity.this,
                                        getResources().getString(R.string.join_room_success),
                                        Toast.LENGTH_LONG).show();
                                removeMarkers();
                                startReceiveLocation();
                            } else {
                                joinRoom.setCanceledOnTouchOutside(true);
                                btn_join.setBackgroundColor(getResources().getColor(R.color.orange));
                                btn_join.setEnabled(true);
                                btn_join.setText(getResources().getString(R.string.join_text));
                                Toast.makeText(MapActivity.this,
                                        getResources().getString(R.string.join_room_fail),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                    Response.ErrorListener errorListener = new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            joinRoom.setCanceledOnTouchOutside(true);
                            btn_join.setBackgroundColor(getResources().getColor(R.color.orange));
                            btn_join.setEnabled(true);
                            btn_join.setText(getResources().getString(R.string.join_text));
                            Toast.makeText(MapActivity.this, getResources().getString(R.string.error_request_server),
                                    Toast.LENGTH_LONG).show();
                        }
                    };
                    RoomServices roomServices = new RoomServices();
                    roomServices.joinRoom(user.getPhone(), roomId, roomPassword, roomResponseListener, errorListener);
                }
            }
        });
        joinRoom.show();
        Window window = joinRoom.getWindow();
        window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
    }

    private void handleLeaveRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.leave_room_message), "Missing-value"));
        builder.setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MapActivity.this, getString(R.string.leaving_text), Toast.LENGTH_LONG).show();
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals(RoomServices.RESULT_SUCCESS)) {
                            user.setJoiningRoomID(0);
                            setLeaveRoomEnable(false);
                            disableSendRoomInfo();
                            stopReceiveLocation();
                            removeMarkers();
                            Toast.makeText(MapActivity.this, getString(R.string.leave_room_success),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MapActivity.this, getString(R.string.leave_room_fail),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapActivity.this, getResources().getString(R.string.error_request_server),
                                Toast.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                };
                RoomServices roomServices = new RoomServices();
                roomServices.leaveRoom(user.getPhone(), String.valueOf(user.getJoiningRoomID()), responseListener, errorListener);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_text), null);
        builder.show();
    }

    private void sendRoomInfoToFriends(final String roomId, final String roomPassword) {
        String message = String.format(getString(R.string.send_room_info_message),
                roomId, roomPassword);
        AlertDialog.Builder sendSMSDialog = new AlertDialog.Builder(MapActivity.this);
        sendSMSDialog.setTitle(R.string.create_room_success)
                .setMessage(message)
                .setPositiveButton(R.string.send_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String body = String.format(getString(R.string.room_info), roomId, roomPassword);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.putExtra("sms_body", body);
                        intent.setData(Uri.parse("smsto:"));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel_text, null);
        sendSMSDialog.show();
    }


    private void disableSendRoomInfo() {
        fab.setVisibility(View.INVISIBLE);
    }

    /**
     * initialize components that used for receive locations
     */
    private void initializeReceiveLocation() {
        this.receiveLocResponse = new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                try {
                    Log.d("map", "received locations\nresponse: " + response);
                    // get users info
                    String result = (String) response;
                    JSONArray arrayUser = new JSONArray(result);
                    Log.d("map", "num of received locations = " + arrayUser.length());
                    receivedUsers = new ArrayList<>(arrayUser.length());
                    JSONObject jsonObject;
                    User u;
                    for (int i = 0; i < arrayUser.length(); i++) {
                        jsonObject = arrayUser.getJSONObject(i);
                        u = new User();
                        u.setPhone(jsonObject.getString("id"));
                        u.setName(jsonObject.getString("name"));
                        u.setLatitude(jsonObject.getDouble("latitude"));
                        u.setLongitude(jsonObject.getDouble("longitude"));
                        receivedUsers.add(u);
                    }
                    // draw locations
                    drawMarkers(receivedUsers);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        this.receiveLocError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("map", "error on receive locations");
                error.printStackTrace();
            }
        };
    }

    private void startReceiveLocation() {
        this.receiveLocationTask = new ReceiveLocationTask(RECEIVE_LOCATION_INTERVAL, receiveLocResponse, receiveLocError);
        this.receiveLocationTask.execute(user.getJoiningRoomID());
    }

    private void stopReceiveLocation() {
        if (this.receiveLocationTask != null) {
            this.receiveLocationTask.stopTask();
        }
    }

    /**
     * draw users' locations that received from server
     * @param arrUsers - list of users, i use this.receivedUsers
     */
    private void drawMarkers(ArrayList<User> arrUsers) {
        // remove old markers
        removeMarkers();
        // draw new markers
        markerList = new ArrayList<>(arrUsers.size());
        Marker marker;
        for (User u : arrUsers) {
            marker = drawUserLocation(u, colors[1]);
            markerList.add(marker);
        }
    }

    private void removeMarkers() {
        if (this.markerList != null) {
            for (Marker marker : markerList) {
                marker.remove();
            }
        }
    }

}
