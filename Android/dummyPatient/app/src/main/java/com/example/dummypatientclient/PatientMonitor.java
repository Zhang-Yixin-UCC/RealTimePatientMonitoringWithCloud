package com.example.dummypatientclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;

//The Patient Monitor activity.
public class PatientMonitor extends AppCompatActivity {
  private String patientID = "";
  private String patientName = "";
  private String patientTag = "";
  private String docID = "";
  private TextView nameTV;
  private TextView idTV;
  private Timer dummyDataGenTimer;
  private TimerTask dummyDataGen;
  private AppConfig appConfig;
  private int heartrateGlobal;
  private int spo2Global;
  private boolean serviceBound = false;
  private Socket mSocket;
  private TextView inputTextTV, docStatusTV;
  private boolean docOnline = false;
  private SharedPreferences sharedPreferences;
  private String msgString;
  private ArrayList<Message> msgArrayList;
  private RecyclerView chatRV;
  private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
  private Button sendBT, callBT;
  private String senior;
  private String phone, docPhone;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_patient_monitor);
//    Connect the variables to views.
    nameTV = findViewById(R.id.nameTV);
    idTV = findViewById(R.id.idTV);
    callBT = findViewById(R.id.callBT);
    inputTextTV = findViewById(R.id.inputTextTV);
    chatRV = findViewById(R.id.chatRV);
    docStatusTV = findViewById(R.id.docStatusTV);
    sendBT = findViewById(R.id.sendBT);

//    Get the patient information from the intent.
    Intent intent = getIntent();
    Bundle bundle = intent.getExtras();
    patientID = bundle.getString("id");
    patientName = bundle.getString("name");
    patientTag = bundle.getString("tag");
    docID = bundle.getString("docID");
    senior = bundle.getString("senior");
    phone = bundle.getString("phone");
    Log.d("senior", senior);
    nameTV.setText(patientName);
    idTV.setText(patientID);
    docStatusTV.setText("Offline");
    sendBT.setEnabled(false);
    callBT.setEnabled(false);

    appConfig = new AppConfig();
//Identify the client by setting arguments in the connect event.
    IO.Options opts = new IO.Options();
    opts.query = "type=" + "patient" + "&patID=" + patientID;
    try {
      mSocket = IO.socket(appConfig.getServerUrl(), opts);
    } catch (URISyntaxException e) {
      DialogFrag1option dialogFrag1option =
          DialogFrag1option.newInstance("ERROR", "Socket connection failed.\nContact Doctor", "OK");
      dialogFrag1option.show(getSupportFragmentManager(), "SocketFailedDialog");
    }
//    Set the listener for the doctor online event.
    mSocket.on(
        "docOnline",
        args -> {
          Log.d("docOnline", Arrays.toString(args));

          try {
            JSONObject phoneJson;
//            The first event contains two arguments.
            if (args.length == 2) {
              phoneJson = new JSONObject(args[1].toString());
              docPhone = phoneJson.getString("phone");
            } else if (args.length == 1) {
//              Normal events contain one argument.
              phoneJson = new JSONObject(args[0].toString());
              docPhone = phoneJson.getString("phone");
            }

            docOnline = true;
            Log.d("docOnline", docPhone);

            runOnUiThread(
                () -> {
                  docStatusTV.setText("Online");
                  sendBT.setEnabled(true);
                  callBT.setEnabled(true);
                });

          } catch (Exception e) {
            Log.d("exception", e.toString());
          }
        });

//    Set the listener for doctor offline event.
    mSocket.on(
        "docOffline",
        args -> {
          docOnline = false;
          runOnUiThread(
              () -> {
                docStatusTV.setText("Offline");
                sendBT.setEnabled(false);
                callBT.setEnabled(false);
              });
        });
//    Set the listener for receiving new chat message.
//    Fetch the message from the event, append in the dataset and ask the adapter
//    to update the chat recycler view and store the new message to the shared preference.
    mSocket.on(
        "chatUpdate",
        args -> {
          try {
            JSONObject messageJSON = (JSONObject) args[0];
            Message message =
                new Message(
                    messageJSON.getString("msg"),
                    messageJSON.getString("time"),
                    messageJSON.getString("from"),
                    messageJSON.getString("patientID"),
                    messageJSON.getString("doctorID"),
                    messageJSON.getString("seniorID"));
            msgArrayList.add(message);
            runOnUiThread(() -> chatRecyclerViewAdapter.insertAtBottom(msgArrayList));
            chatRV.smoothScrollToPosition(msgArrayList.size() - 1);

            String msgString = sharedPreferences.getString("msg", "");

            if (msgString.equals("")) {
              JSONArray msgJsonArray = new JSONArray();
              msgJsonArray.put(messageJSON);
              sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
            } else {
              JSONArray msgJsonArray = new JSONArray(msgString);
              msgJsonArray.put(messageJSON);
              sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
            }

          } catch (Exception e) {
            Log.d("exception", e.toString());
          }
        });
//    Connect the WS to the server.
    mSocket.connect();

//    Create a background task to generate random vital signs
//    and send the data to the server.
    dummyDataGenTimer = new Timer();
    dummyDataGen =
        new TimerTask() {
          @Override
          public void run() {
            Log.d("dummyDataGen", "Gened");
            int heartrate = (int) (Math.random() * (150 - 70) + 70);
            int spo2 = (int) (Math.random() * (100 - 92) + 92);
            heartrateGlobal = heartrate;
            spo2Global = spo2;
            runOnUiThread(
                () -> {
                  TextView heartrateTV = findViewById(R.id.heartrateTV);
                  TextView spo2TV = findViewById(R.id.spo2TV);
                  heartrateTV.setText(String.valueOf(heartrate));
                  spo2TV.setText(String.valueOf(spo2));
                });
            if (mSocket != null) {
              Map<String, String> params = new HashMap<>();
              params.put("patientID", patientID);
              params.put("patientName", patientName);
              params.put("patientTag", patientTag);
              params.put("heartrate", String.valueOf(heartrate));
              params.put("spo2", String.valueOf(spo2));
              params.put("docID", docID);
              params.put("senior", senior);

              JSONObject paramsJson = new JSONObject(params);
              mSocket.emit("patientMonitorUpdate", paramsJson);
            }
          }
        };
    dummyDataGenTimer.scheduleAtFixedRate(dummyDataGen, 1000, 2000);

//    Read the history message in the shared preferences.
//    Load the history message in the dataset.
    sharedPreferences = getSharedPreferences(patientID, MODE_PRIVATE);
    msgString = sharedPreferences.getString("msg", "");
    Log.d("msgStr", msgString);
    msgArrayList = new ArrayList<>();
    if (!msgString.equals("")) {
      try {
        JSONArray msgJsonArray = new JSONArray(msgString);
        Log.d("jsonArray", msgJsonArray.toString());
        for (int i = 0; i < msgJsonArray.length(); i++) {
          JSONObject msgJson = msgJsonArray.getJSONObject(i);
          Log.d("msgJson", msgJson.toString());
          Message message =
              new Message(
                  msgJson.getString("msg"),
                  msgJson.getString("time"),
                  msgJson.getString("from"),
                  msgJson.getString("patientID"),
                  msgJson.getString("doctorID"),
                  msgJson.getString("seniorID"));
          Log.d("msg", message.toString());
          msgArrayList.add(message);
          Log.d("msg", msgArrayList.toString());
        }
      } catch (JSONException e) {
        Log.d("exception", e.toString());
      }
    }
//  Set the layout for the chat recycler view.
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    chatRV.setLayoutManager(layoutManager);
//Set the adapter and scroll to the bottom.
    chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, msgArrayList);
    chatRV.setAdapter(chatRecyclerViewAdapter);
    if (msgArrayList.size() != 0) {
      chatRV.smoothScrollToPosition(msgArrayList.size() - 1);
    }
  }

//  OnClick event listener for the send message button

  public void sendMsgOnClick(View v) {
//    Fetch the message and make a new Message object.
    String msg = inputTextTV.getText().toString();
    if (!msg.equals("")) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
      Message message =
          new Message(
              msg,
              dateTimeFormatter.format(LocalDateTime.now()),
              "patient",
              patientID,
              docID,
              senior);
      JSONObject paramJSON = message.toJSONObject();
//      Set the listener for the server confirming the message
      mSocket.on(
//              Fetch the message from the event.
          "confirm",
          args -> {
            JSONObject msgJsonArg = (JSONObject) args[0];
            try {
              Message message1 =
                  new Message(
                      msgJsonArg.getString("msg"),
                      msgJsonArg.getString("time"),
                      msgJsonArg.getString("from"),
                      msgJsonArg.getString("patientID"),
                      msgJsonArg.getString("doctorID"),
                      msgJsonArg.getString("seniorID"));
              String msgString = sharedPreferences.getString("msg", "");
              JSONObject msgJson = message1.toJSONObject();
//            Store the message to shared preference
//            Add the message to the dataset and ask the adapter to update the chat recycler view.
//            Scroll the chat view to the bottom.
              if (msgString.equals("")) {
                JSONArray msgJsonArray = new JSONArray();
                msgJsonArray.put(msgJson);
                sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
                msgArrayList.add(message1);
                runOnUiThread(() -> chatRecyclerViewAdapter.updateAdapter(msgArrayList));
                chatRV.smoothScrollToPosition(msgArrayList.size() - 1);

              } else {
                JSONArray msgJsonArray = new JSONArray(msgString);
                msgJsonArray.put(msgJson);
                sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
                msgArrayList.add(message1);
                runOnUiThread(() -> chatRecyclerViewAdapter.insertAtBottom(msgArrayList));
                chatRV.smoothScrollToPosition(msgArrayList.size() - 1);
              }
            } catch (JSONException e) {
              Log.d("exception", e.toString());
            }
//            Unregister the confirm event listener after everything is done.
            Log.d("confirm", "true");
            mSocket.off("confirm");
          });
//          Send the message to the server.
      mSocket.emit("uploadChatMsg", paramJSON);
    }
  }

//  OnClick event listener for the call doctor button
//  Call the system service to dial the doctor's number.
  public void callBTOnClick(View v){
    Intent intent = new Intent(Intent.ACTION_DIAL);
    Uri uri = Uri.parse("tel:"+docPhone);
    intent.setData(uri);
    startActivity(intent);
  }

//  Disconnect the socket when the activity is destroyed
//  The socket will disconnect anyway.
  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mSocket.connected()) {
      mSocket.disconnect();
    }
  }

}
