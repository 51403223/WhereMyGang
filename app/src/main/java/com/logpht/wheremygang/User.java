package com.logpht.wheremygang;

/**
 * Created by Long on 28/04/2018.
 */
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String phone;
    private String name;
    private String password;
    private int joiningRoomID;
    private String joiningRoomName;
    private double longitude;
    private double latitude;

    public User(String phone, String name, String password, int joiningRoomID, String joiningRoomName, double longitude, double latitude) {
        this.phone = phone;
        this.name = name;
        this.password = password;
        this.joiningRoomID = joiningRoomID;
        this.joiningRoomName = joiningRoomName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public User() {
        this.joiningRoomID = 0;
        this.phone = "";
        this.name = "";
        this.password = "";
        this.joiningRoomName = "";
    }

    protected User(Parcel in) {
        phone = in.readString();
        name = in.readString();
        password = in.readString();
        joiningRoomID = in.readInt();
        longitude = in.readDouble();
        latitude = in.readDouble();
        joiningRoomName = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getJoiningRoomName() {
        return joiningRoomName;
    }

    public void setJoiningRoomName(String joiningRoomName) {
        this.joiningRoomName = joiningRoomName;
    }

    @Override
    public String toString() {
        return "User{" +
                "phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", joiningRoomID=" + joiningRoomID +
                ", joiningRoomName='" + joiningRoomName + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phone);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeInt(joiningRoomID);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(joiningRoomName);
    }
}
