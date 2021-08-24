package com.example.doctorclient;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//The Message class
public class Message implements Parcelable {
    public String msg;
    public String time;
    public String from;
    public String patientID;
    public String docID;
    public String seniorID;

//    Suppress the default constructor
    private Message(){}

    public  Message(String msg, String time, String from, String patientID, String docID, String seniorID){
        this.msg = msg;
        this.time = time;
        this.from = from;
        this.patientID = patientID;
        this.docID = docID;
        this.seniorID = seniorID;
    }

//    Util function for converting the Message object to json object.
    public JSONObject toJSONObject(){
        Map<String, String> param = new HashMap<>();
        param.put("msg", this.msg);
        param.put("time", this.time);
        param.put("from", this.from);
        param.put("patientID", this.patientID);
        param.put("doctorID", this.docID);
        param.put("seniorID", this.seniorID);
        JSONObject ret = new JSONObject(param);
        return  ret;
    }

//    Util function for converting the Message object to String.
    @Override
    public String toString(){
        return "{\"msg\":"+this.msg + ", \"time\":" + this.time + ", \"from\":" + this.from + ", \"patientID\":" + this.patientID + ", \"doctorID\":" + this.docID + ", \"seniorID\":" + this.seniorID+"}";

    }

//    Constructor
    public Message(@NonNull Parcel p){
        this.msg = p.readString();
        this.time = p.readString();
        this.from = p.readString();
        this.patientID = p.readString();
        this.docID = p.readString();
        this.seniorID = p.readString();
    }

//    Util functions for implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msg);
        dest.writeString(this.time);
        dest.writeString(this.from);
        dest.writeString(this.patientID);
        dest.writeString(this.docID);
        dest.writeString(this.seniorID);
    }

    public  static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

}

