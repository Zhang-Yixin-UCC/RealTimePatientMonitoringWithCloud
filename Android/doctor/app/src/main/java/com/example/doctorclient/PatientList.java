package com.example.doctorclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//The controller for the pateint list activity.
public class PatientList extends AppCompatActivity {
    private String doctorID;
    private String patientsStr;
    private JSONObject patients;
    private RecyclerView patientListRV;
    private RecyclerView.LayoutManager layoutManager;
    private PatientListAdapter patientListAdapter;
    private AppConfig appConfig;
    private SocketBackgroundService socketBackgroundService;
    private ArrayList<Patient> patientArrayList;
    private BroadcastReceiver updateColorReceiver, errorReceiver, chatUpdateReceiver, readMsgReceiver;
    private Map<String, Boolean> newMsgMap;
    private boolean bounded = false;
    private boolean isSenior;
    private String senior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
//        Connect the variables to the views
        patientListRV = findViewById(R.id.patientListRV);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        patientListRV.setLayoutManager(layoutManager);
//        Get the doctor information from the intent.
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        doctorID = bundle.getString("doctorID");
        isSenior = bundle.getBoolean("isSenior");
        senior = bundle.getString("senior");
        newMsgMap = new HashMap<>();
//        Set the adapter for the patient list recycler view.
        patientListAdapter = new PatientListAdapter(this, patientArrayList, doctorID, newMsgMap, isSenior, senior);
        patientListRV.setAdapter(patientListAdapter);

        appConfig = new AppConfig();
        patientArrayList = new ArrayList<>();

//        Bind the activity to the background service
        Intent intent1 = new Intent(this, SocketBackgroundService.class);
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);

//        Broadcast listener for updating the category
//        Modify the dataset and ask the adapter to update.
        updateColorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Patient p = intent.getParcelableExtra("patient");
                for (int i = 0; i < patientArrayList.size(); i++) {
                    if (patientArrayList.get(i).id.equals(p.id)) {
                        if (!patientArrayList.get(i).color.equals(p.color)) {
                            patientArrayList.get(i).color = p.color;
                            patientListAdapter.updateAdapterPosition(patientArrayList, i);
                        }
                    }
                }

            }
        };
//        Broadcast receiver that handles error.
        errorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "JSON format error.<Service update>\nContact IT", "OK");
                dialogFrag1option.show(getSupportFragmentManager(), "serviceUpdateErrorDialog");

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(updateColorReceiver, new IntentFilter("updatePatient"));
//      Broadcast receiver that handles receiving new message.
        chatUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = intent.getParcelableExtra("msg");
                String patientID = message.patientID;
                Log.d("chatUpdate", patientID);
                if (newMsgMap.containsKey(message.patientID)) {
                    newMsgMap.put(message.patientID, true);
                    for (int i = 0; i < patientArrayList.size(); i++) {
                        if (patientArrayList.get(i).id.equals(message.patientID)) {
                            patientListAdapter.readMsgUpdateAdaptorOnPosition(patientArrayList, newMsgMap, i);
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(chatUpdateReceiver, new IntentFilter("chatUpdate"));

//        Broadcast Receiver that handles message get read
//        Calling this receiver will make the "new message" text disappear on specific patient.
        readMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String patientID = intent.getStringExtra("patientID");
                newMsgMap.put(patientID, false);
                for (int i = 0; i < patientArrayList.size(); i++) {
                    if (patientArrayList.get(i).id == patientID) {
                        patientListAdapter.readMsgUpdateAdaptorOnPosition(patientArrayList, newMsgMap, i);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(readMsgReceiver, new IntentFilter("readMsg"));
    }


    @Override
    protected void onResume() {
        super.onResume();
//        Re-request the patient list when the activity resume
        getPatientList(appConfig);
//        Bind the service if it is not bounded
        LocalBroadcastManager.getInstance(this).registerReceiver(errorReceiver, new IntentFilter("error"));
        if (!bounded) {
            bindService(new Intent(this, SocketBackgroundService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }
//        Ask the adapter to update.
        patientListAdapter.updateAdapter(patientArrayList);
    }

//    OnClick listener for the "Add" button.
//    Redirect to the "add_patient" activity.
    public void addPatientOnClick(View v) {
        Log.d("docID", doctorID);
        Intent intent = new Intent(PatientList.this, AddPatient.class);
        Bundle bundle = new Bundle();
        bundle.putString("doctorID", doctorID);
        bundle.putBoolean("isSenior", false);
        bundle.putString("senior", senior);
        intent.putExtras(bundle);
        startActivity(intent);
    }

//    Util function for requesting the patient list
    private void getPatientList(AppConfig appConfig) {
        Map<String, String> doctorIDMap = new HashMap<>();
        doctorIDMap.put("doctorID", doctorID);
        JSONObject doctorIDJson = new JSONObject(doctorIDMap);
        String url = appConfig.getServerUrl() + "reqPatientList";
//        Make a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url,
                doctorIDJson,
//                Set the listener for putting received patient list to the dataset
                response -> {
                    patientArrayList = new ArrayList<>();
                    try {
                        JSONArray patientIdJsonArray = response.getJSONArray("patientIDList");
                        Log.d("json", response.toString());
                        ArrayList<String> patientIdArrayList = new ArrayList<>();

                        for (int i = 0; i < patientIdJsonArray.length(); i++) {
                            patientIdArrayList.add(patientIdJsonArray.getString(i));
                        }

                        for (String i : patientIdArrayList) {
                            JSONObject patientJson = response.getJSONObject(i);
                            Log.d("patientjson", patientJson.toString());
                            Patient patient = new Patient(patientJson.getString("patientName"), i, patientJson.getString("patientTag"), patientJson.getString("doctorID"), "",false);
                            patientArrayList.add(patient);
                            if (!newMsgMap.containsKey(i)) {
                                newMsgMap.put(i, false);
                            }
                        }
                        Log.d("arraylist", patientArrayList.toString());
                        Log.d("arraylist", newMsgMap.toString());
//                        Ask the adapter to update the recyler view
                        patients = new JSONObject(response.toString());
                        patientListAdapter.updateAdapter(patientArrayList);

                    } catch (JSONException e) {
                        DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "JSON parse error.<adapter>\nContact IT", "OK");
                        dialogFrag1option.show(getSupportFragmentManager(), "jsonParseErrorAdapterDialog");
                    }
                },
//                Set th error listener.
                error -> {
                    if (error.networkResponse == null) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Server not reachable.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "serviceNotReachableDialog");
                        });
                    } else if (error.networkResponse.statusCode == 500) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Database error.\nContact IT staff.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "dbErrorDialog");
                        });
                    } else {
                        DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Unknown Error.<DB query failed>\nContact IT staff.", "OK");
                        dialogFrag1option.show(getSupportFragmentManager(), "unknownErrorDialog");
                    }

                });
        jsonObjectRequest.setShouldCache(false);
        RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

//    Set the service connection used to connect the background service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketBackgroundService.MBinder mBinder = (SocketBackgroundService.MBinder) service;
            socketBackgroundService = mBinder.getServiceBinder();
            socketBackgroundService.setDoctorID(doctorID);
            socketBackgroundService.connectSocket();
            bounded = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bounded = false;

        }
    };

//    Unregister the error broadcast receiver when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorReceiver);
    }

//    Unbind the background service when the activity is destroyed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bounded) {
            socketBackgroundService.disconnectSocket();
            unbindService(serviceConnection);
            stopService(new Intent(this, SocketBackgroundService.class));
        }
    }
}