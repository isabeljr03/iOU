package com.ijruiz.cs126.iouapplication;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by isabelruiz on 5/1/17.
 * Class that represents a user of the application.
 */
public class User implements Parcelable {

    private String uid;
    private String displayName;
    private String email;

    //constructors

    public User() {}

    public User(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
    }

    //getters

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    //setters

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //parcelable methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.displayName);
        dest.writeString(this.email);
    }

    protected User(Parcel in) {
        this.uid = in.readString();
        this.displayName = in.readString();
        this.email = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
