package com.logpht.wheremygang;

/**
 * Created by Long on 28/04/2018.
 */
import com.google.android.gms.maps.model.LatLng;

public class User {
    private String phone;
    private String name;
    private String password;
    private int joiningRoomID;
    private LatLng location;

    public User(String phone, String name, String password, int joiningRoomID, LatLng location) {
        this.phone = phone;
        this.name = name;
        this.password = password;
        this.joiningRoomID = joiningRoomID;
        this.location = location;
    }

    public User() {}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getJoiningRoomID() {
        return joiningRoomID;
    }

    public void setJoiningRoomID(int joiningRoomID) {
        this.joiningRoomID = joiningRoomID;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "User{" +
                "phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", joiningRoomID=" + joiningRoomID +
                ", location=" + location +
                '}';
    }
}
