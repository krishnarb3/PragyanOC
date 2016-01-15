package com.delta.pragyanoc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rb on 13/1/16.
 */
public class User implements Parcelable {
    public String user_name;
    public String user_roll;
    public String user_phone;
    public String user_type;

    public User() {

    }
    protected User(Parcel in) {
        user_name = in.readString();
        user_roll = in.readString();
        user_phone = in.readString();
        user_type = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(user_name);
        dest.writeString(user_roll);
        dest.writeString(user_phone);
        dest.writeString(user_type);
    }
}
