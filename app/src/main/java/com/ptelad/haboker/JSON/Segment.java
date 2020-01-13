package com.ptelad.haboker.JSON;

import android.os.Parcel;
import android.os.Parcelable;

public class Segment implements Parcelable {
    public String name;
    public String description;
    public String image_url;
    public String download_url;
    public long progress;
    public long duration;

    public Segment() {
        progress = 0;
        duration = 0;
    }

    protected Segment(Parcel in) {
        name = in.readString();
        description = in.readString();
        image_url = in.readString();
        download_url = in.readString();
        progress = in.readLong();
        duration = in.readLong();
    }

    public static final Creator<Segment> CREATOR = new Creator<Segment>() {
        @Override
        public Segment createFromParcel(Parcel in) {
            return new Segment(in);
        }

        @Override
        public Segment[] newArray(int size) {
            return new Segment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(image_url);
        parcel.writeString(download_url);
        parcel.writeLong(progress);
        parcel.writeLong(duration);
    }
}
