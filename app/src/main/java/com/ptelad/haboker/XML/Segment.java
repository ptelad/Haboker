package com.ptelad.haboker.XML;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public @Root(strict = false)
class Segment implements Parcelable {
    @Element(data = true)
    public String RecordedProgramsName;

    @Element(data = true)
    public String RecordedProgramsDownloadFile;

    @Element(data = true)
    public String RecordedProgramsImg;

    public long progress = 0;

    public Segment() {
    }

    protected Segment(Parcel in) {
        RecordedProgramsName = in.readString();
        RecordedProgramsDownloadFile = in.readString();
        RecordedProgramsImg = in.readString();
        progress = in.readLong();
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
        parcel.writeString(RecordedProgramsName);
        parcel.writeString(RecordedProgramsDownloadFile);
        parcel.writeString(RecordedProgramsImg);
        parcel.writeLong(progress);
    }
}
