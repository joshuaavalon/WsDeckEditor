package com.joshuaavalon.wsdeckeditor.sdk.util;

import android.os.Parcel;
import android.os.Parcelable;

public class Range implements Parcelable {
    public static final Creator<Range> CREATOR = new Creator<Range>() {
        @Override
        public Range createFromParcel(Parcel source) {
            return new Range(source);
        }

        @Override
        public Range[] newArray(int size) {
            return new Range[size];
        }
    };
    private int max;
    private int min;

    public Range() {
        max = -1;
        min = -1;
    }

    protected Range(Parcel in) {
        this.max = in.readInt();
        this.min = in.readInt();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.max);
        dest.writeInt(this.min);
    }
}
