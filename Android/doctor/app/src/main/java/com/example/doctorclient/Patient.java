package com.example.doctorclient;

import android.os.Parcel;
import android.os.Parcelable;

public class Patient implements Parcelable {
    public String name;
    public String id;
    public String color;
    public String heartrate;
    public String spo2;
    public String tag;
    public String docID;
    public String phone;
    public Boolean allowReply;

    private Patient(){}

    public Patient(String name, String id, String tag, String docID, String phone, Boolean allowReply){
        this.name = name;
        this.id = id;
        this.tag = tag;
        this.color = "Black";
        this.heartrate = "";
        this.spo2 = "";
        this.docID = docID;
        this.phone = phone;
        this.allowReply = allowReply;
    };
    public  Patient(String name, String id, String tag, String color, String heartrate, String spo2,String docID, String phone, Boolean allowReply){
        this.name = name;
        this.id = id;
        this.tag = tag;
        this.color = color;
        this.heartrate = heartrate;
        this.spo2 = spo2;
        this.docID = docID;
        this.phone = phone;
        this.allowReply = allowReply;
    }




    public Patient(Parcel p){
        this.name = p.readString();
        this.id = p.readString();
        this.tag=p.readString();
        this.color = p.readString();
        this.heartrate = p.readString();
        this.spo2 = p.readString();
        this.docID = p.readString();
        this.phone = p.readString();
        this.allowReply = p.readBoolean();
    }

    public static final Creator<Patient> CREATOR = new Creator<Patient>() {
        @Override
        public Patient createFromParcel(Parcel in) {
            return new Patient(in);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
        dest.writeString(this.tag);
        dest.writeString(this.color);
        dest.writeString(this.heartrate);
        dest.writeString(this.spo2);
        dest.writeString(this.docID);
        dest.writeString(this.phone);
        dest.writeBoolean(this.allowReply);
    }
}
