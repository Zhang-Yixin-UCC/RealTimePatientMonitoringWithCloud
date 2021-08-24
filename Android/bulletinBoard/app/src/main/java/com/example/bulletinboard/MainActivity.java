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

public class MainActivity extends AppCompatActivity {
  private RecyclerView patientRV;
  private PatientRVAdapter patientRVAdapter;
  private AppConfig appConfig;
  private Map<String, String[]> patientMap;
  private ArrayList<String> patientIDArrayList;
  private RecyclerView.LayoutManager layoutManager;
  private Socket mSocket;

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
    IO.Options opts = new IO.Options();
    opts.query = "type=board";
    try {
      mSocket = IO.socket(appConfig.getServerUrl(), opts);

    } catch (URISyntaxException e) {
      DialogFrag1Option dialogFrag1Option =
          DialogFrag1Option.newInstance("Error", "Connection Failed\nRestart", "OK");
      dialogFrag1Option.show(getSupportFragmentManager(), "");
    }
    mSocket.on(
        "updatePatient",
        args -> {
          try {
            Log.d("args", String.valueOf(args.length));
            if (args.length == 2) {
              JSONObject argsJson = (JSONObject) args[1];
              JSONArray patientIDJsonArray = argsJson.getJSONArray("patientIDList");
              Log.d("patientIDJsonArray", String.valueOf(patientIDJsonArray.length()));
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

  @Override
  protected void onPause() {
    super.onPause();
    mSocket.disconnect();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSocket.connect();
  }
}
