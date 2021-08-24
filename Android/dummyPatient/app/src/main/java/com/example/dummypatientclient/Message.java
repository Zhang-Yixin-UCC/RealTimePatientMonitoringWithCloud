package com.example.dummypatientclient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//The class for the chat message.
public class Message {
  public String msg;
  public String time;
  public String from;
  public String patientID;
  public String docID;
  public String seniorID;

  private Message() {}

  public Message(String msg, String time, String from, String patientID, String docID,String seniorID) {
    this.msg = msg;
    this.time = time;
    this.from = from;
    this.patientID = patientID;
    this.docID = docID;
    this.seniorID = seniorID;
  }

//  Convert the message object to the JSON object
  public JSONObject toJSONObject() {
    Map<String, String> param = new HashMap<>();
    param.put("msg", this.msg);
    param.put("time", this.time);
    param.put("from", this.from);
    param.put("patientID", this.patientID);
    param.put("doctorID", this.docID);
    param.put("seniorID", this.seniorID);
    JSONObject ret = new JSONObject(param);
    return ret;
  }

//  Convert the message object to string
  @Override
  public @NotNull String toString() {
    return "{\"msg\":"
        + this.msg
        + ", \"time\":"
        + this.time
        + ", \"from\":"
        + this.from
        + ", \"patientID\":"
        + this.patientID
        + ", \"doctorID\":"
        + this.docID
        + ", \"seniorID\":"
            + this.seniorID
            +"}";
  }
}
