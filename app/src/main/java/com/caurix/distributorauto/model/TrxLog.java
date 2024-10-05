package com.caurix.distributorauto.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class TrxLog implements Parcelable {
    private String trxTargetNumber;
    private String trxStatus;
    private String trxDateTime;
    private String trxAmount;
    private String trxNotes;
    private String trxType;
    private String trxSDNumber;
    private String sdName;

    public TrxLog() {
    }

    public TrxLog(Parcel in) {
        trxTargetNumber = in.readString();
        trxStatus = in.readString();
        trxDateTime = in.readString();
        trxAmount = in.readString();
        trxNotes = in.readString();
        trxType = in.readString();
        trxSDNumber = in.readString();
        sdName = in.readString();
    }

    public static final Creator<TrxLog> CREATOR = new Creator<TrxLog>() {
        @Override
        public TrxLog createFromParcel(Parcel in) {
            return new TrxLog(in);
        }

        @Override
        public TrxLog[] newArray(int size) {
            return new TrxLog[size];
        }
    };

    public String getTrxTargetNumber() {
        return trxTargetNumber;
    }

    public void setTrxTargetNumber(String trxTargetNumber) {
        this.trxTargetNumber = trxTargetNumber;
    }

    public String getTrxStatus() {
        return trxStatus;
    }

    public void setTrxStatus(String trxStatus) {
        this.trxStatus = trxStatus;
    }

    public String getTrxDateTime() {
        return trxDateTime;
    }

    public void setTrxDateTime(String trxDateTime) {
        this.trxDateTime = trxDateTime;
    }

    public String getTrxAmount() {
        return trxAmount;
    }

    public void setTrxAmount(String trxAmount) {
        this.trxAmount = trxAmount;
    }

    public String getTrxNotes() {
        return trxNotes;
    }

    public void setTrxNotes(String trxNotes) {
        this.trxNotes = trxNotes;
    }

    public String getTrxType() {
        return trxType;
    }

    public void setTrxType(String trxType) {
        this.trxType = trxType;
    }

    public String getTrxSDNumber() {
        return trxSDNumber;
    }

    public void setTrxSDNumber(String trxSDNumber) {
        this.trxSDNumber = trxSDNumber;
    }

    public String getSdName() {
        return sdName;
    }

    public void setSdName(String sdName) {
        this.sdName = sdName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(trxTargetNumber);
        parcel.writeString(trxStatus);
        parcel.writeString(trxDateTime);
        parcel.writeString(trxAmount);
        parcel.writeString(trxNotes);
        parcel.writeString(trxType);
        parcel.writeString(trxSDNumber);
        parcel.writeString(sdName);
    }
}
