package com.example.bulletinboard;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// The Main and only activity
public class MainActivity extends AppCompatActivity {
  private RecyclerView patientRV;
  private PatientRVAdapter patientRVAdapter;
  private AppConfig appConfig;
  private Map<String, String[]> patientMap;
  private ArrayList<String> patientIDArrayList;
  private RecyclerView.LayoutManager layoutManager;
  private Socket mSocket;


//  Load the application configuration
//  Create a socket connection
//  Register the WS listeners
//  Connect to the server
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    appConfig = new AppConfig();
    patientRV = findViewById(R.id.patientRV);
    patientMap = new HashMap<>();
    patientIDArrayList = new ArrayList<>();
    layoutManager = new GridLayoutManager(this, 3);
    patientRVAdapter = new PatientRVAdapter(this, patientMap, patientIDArrayList);

    patientRV.setLayoutManager(layoutManager);
    patientRV.setAdapter(patientRVAdapter);

//    Identify the client by adding args to the connect event
    IO.Options opts = new IO.Options();
    opts.query = "type=board";
    try {
      mSocket = IO.socket(appConfig.getServerUrl(), opts);

    } catch (URISyntaxException e) {
      DialogFrag1Option dialogFrag1Option =
          DialogFrag1Option.newInstance("Error", "Connection Failed\nRestart", "OK");
      dialogFrag1Option.show(getSupportFragmentManager(), "");
    }

//Register the listener for receiving vital sign update
    mSocket.on(
        "updatePatient",
        args -> {
          try {
            Log.d("args", String.valueOf(args.length));
//            For the first time the board connects to the server, the event contains
//            the data for all patients. This first event arrives with two arguments.
            if (args.length == 2) {
              Log.d("args", args[0].toString());
              Log.d("args", args[1].toString());

              JSONObject argsJson = (JSONObject) args[1];
              JSONArray patientIDJsonArray = argsJson.getJSONArray("patientIDList");
              if (patientIDJsonArray.length() > 1) {
                for (int i = 0; i < patientIDJsonArray.length(); i++) {
                  String patientID = patientIDJsonArray.getString(i);
                  JSONObject patient = argsJson.getJSONObject(patientID);
                  if (!patientIDArrayList.contains(patientID)){
                    patientIDArrayList.add(patientID);
                    String[] p = new String[2];
                    p[0] = patient.getString("patientName");
                    p[1] = patient.getString("patientColor");
                    patientMap.put(patientID, p);
                  }
                }
                runOnUiThread(() -> patientRVAdapter.updateAdapter(patientMap, patientIDArrayList));
              }
            } else if (args.length == 1) {
//              Normal update event arrives with one argument.
//              Update the dataset and notify the adapter to update the recycler view.
              JSONObject argsJson = (JSONObject) args[0];
              JSONArray patientIDJsonArray = argsJson.getJSONArray("patientIDList");
              String patientID = patientIDJsonArray.getString(0);
              JSONObject patient = argsJson.getJSONObject(patientID);
              if (patientIDArrayList.contains(patientID)) {
                String[] p = new String[2];
                p[0] = patient.getString("patientName");
                p[1] = patient.getString("patientColor");
                patientMap.put(patientID, p);
                runOnUiThread(
                    () ->
                        patientRVAdapter.updateAdapterOnPosition(
                            patientMap, patientIDArrayList, patientIDArrayList.indexOf(patientID)));

              } else {
                patientIDArrayList.add(patientID);
                String[] p = new String[2];
                p[0] = patient.getString("patientName");
                p[1] = patient.getString("patientColor");
                patientMap.put(patientID, p);
                runOnUiThread(
                    () ->
                        patientRVAdapter.updateAdapterOnPosition(
                            patientMap, patientIDArrayList, patientIDArrayList.size() - 1));
              }
            }
          } catch (Exception e) {
            Log.d("exception", e.toString());
          }
        });
//  Register the listener for patient client going offline
//    Update the dataset and notify the adapter to update the recycler view.
    mSocket.on(
        "patientOffline",
        new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            try {
              JSONObject argsJson = (JSONObject) args[0];
              String patientID = argsJson.getString("id");
              if (patientMap.containsKey(patientID)) {
                String[] p = new String[2];
                p[0] = argsJson.getString("name");
                p[1] = "Black";
                patientMap.put(patientID, p);
                runOnUiThread(
                    () ->
                        patientRVAdapter.updateAdapterOnPosition(
                            patientMap, patientIDArrayList, patientIDArrayList.indexOf(patientID)));
              }

            } catch (Exception e) {
              Log.d("exception", e.toString());
            }
          }
        });
  }

//  When the activity is paused, disconnect from the server
  @Override
  protected void onPause() {
    super.onPause();
    mSocket.disconnect();
  }

//  When the activity is resumed, connect the server
  @Override
  protected void onResume() {
    super.onResume();
    mSocket.connect();
  }
}
