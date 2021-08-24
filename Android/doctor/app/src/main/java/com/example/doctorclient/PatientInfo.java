package com.example.doctorclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

//The controller for the patient_info activity.
public class PatientInfo extends AppCompatActivity {
    private String doctorID;
    private Patient patient;
    private TextView nameTV, idTV, heartrateTV, spo2TV;
    private BroadcastReceiver updatePatientReciever, errorReciever;
    private SocketBackgroundService socketBackgroundService;
    private boolean bounded = false;
    private ArrayList<Message> msgArrayList;
    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private SharedPreferences sharedPreferences;
    private RecyclerView patientRV;
    private TextView chatInputTextTV;
    private BroadcastReceiver confirmMsgReceiver, chatUpdateReceiver;
    private Button sendBT;
    private boolean isSenior;
    private String senior;
    private Button callBT;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);
//        Get the patient and doctor information from the intent.
        Intent intent = getIntent();
        patient = intent.getParcelableExtra("patient");
        doctorID = intent.getStringExtra("doctorID");
        isSenior = intent.getBooleanExtra("isSenior", true);
        senior = intent.getStringExtra("senior");
//        Connect the variables to the views
        callBT = findViewById(R.id.callBT);
        nameTV = findViewById(R.id.nameTV);
        idTV = findViewById(R.id.idTV);
        heartrateTV = findViewById(R.id.heartrateTV);
        spo2TV = findViewById(R.id.spo2TV);
        patientRV = findViewById(R.id.patientRV);
        sendBT = findViewById(R.id.sendBT);
        nameTV.setText(patient.name);
        idTV.setText(patient.id);
        chatInputTextTV = findViewById(R.id.chatInputTextTV);
//        Log.d("doctorID", doctorID);
//        Show the patient data
        if (patient.color.equals("Black")) {
            heartrateTV.setText("Offline");
            spo2TV.setText("Offline");
            sendBT.setEnabled(false);
            callBT.setEnabled(false);
        } else {
            heartrateTV.setText(patient.heartrate);
            spo2TV.setText(patient.spo2);
            sendBT.setEnabled(true);
            callBT.setEnabled(true);
        }
        if (isSenior){
            sendBT.setEnabled(false);
            callBT.setEnabled(false);
        }
//        Set the broadcast receiver for receiving patient information
        updatePatientReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Patient p = intent.getParcelableExtra("patient");
                Log.d("patientID", p.id);
//                Set the text views using the Patient object from the intent
                if (p.color.equals("Black")) {
                    heartrateTV.setText("Offline");
                    spo2TV.setText("Offline");
                    sendBT.setEnabled(false);
                    callBT.setEnabled(false);

                } else {
                    if (p.id.equals(patient.id)) {
                        Log.d("allowReply", String.valueOf(p.allowReply));
                        heartrateTV.setText(p.heartrate);
                        spo2TV.setText(p.spo2);
                        sendBT.setEnabled(p.allowReply);
                        callBT.setEnabled(true);
                        phone = p.phone;
                    }
                }

            }
        };
//        Set the error broadcast receiver
        errorReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Patient update error.\nContact IT.", "OK");
                dialogFrag1option.show(getSupportFragmentManager(), "patientUpdateErrorDialog");
            }
        };
//        Bind to the background service
        Intent intent1 = new Intent(this, SocketBackgroundService.class);
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);

//        Set the chat recycler view adapter and layout
        msgArrayList = new ArrayList<>();
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this, msgArrayList, isSenior);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        patientRV.setLayoutManager(layoutManager);
        patientRV.setAdapter(chatRecyclerViewAdapter);

//        Set the comfirm message broadcast receiver
//        This receiver is invoked when the message is confirmed by the server
        confirmMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message messageGet = intent.getParcelableExtra("msg");
                msgArrayList.add(messageGet);
                chatRecyclerViewAdapter.insertAtBottom(msgArrayList);
                patientRV.smoothScrollToPosition(msgArrayList.size() - 1);

            }
        };

//        Set the broadcast receiver for receiving a message
        chatUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = intent.getParcelableExtra("msg");
                Log.d("msg", message.toString());
//                Append the message to the dataset and
//                ask the adapter to update the recycler view.
                if (message.patientID.equals(patient.id)){
                    msgArrayList.add(message);
                    chatRecyclerViewAdapter.insertAtBottom(msgArrayList);
                    patientRV.smoothScrollToPosition(msgArrayList.size() - 1);
                }
            }
        };

//        Send the broadcast that the doctor has read the message of this patient.
        Intent intent2 = new Intent("readMsg");
        intent2.putExtra("patientID", patient.id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("boundedResume", String.valueOf(bounded));

//        Bind the service when the activity is resumed.
        if (!bounded) {
            bindService(new Intent(this, SocketBackgroundService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            bounded = true;
        }
//        Load the history message from the shared preference.
        sharedPreferences = getSharedPreferences(doctorID + "_" + patient.id, MODE_PRIVATE);
        String msgString = sharedPreferences.getString("msg", "");
        msgArrayList = new ArrayList<>();
        try {
            if (!msgString.equals("")) {
                JSONArray msgJsonArray = new JSONArray(msgString);
                for (int i = 0; i < msgJsonArray.length(); i++) {
                    JSONObject msgJson = msgJsonArray.getJSONObject(i);
                    Message message =
                            new Message(
                                    msgJson.getString("msg"),
                                    msgJson.getString("time"),
                                    msgJson.getString("from"),
                                    msgJson.getString("patientID"),
                                    msgJson.getString("doctorID"),
                                    msgJson.getString("seniorID"));
                    msgArrayList.add(message);
                }
            }
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
//        Ask the adapter to update the recycler view and scroll the recycler view to the button.
        chatRecyclerViewAdapter.updateAdapter(msgArrayList);
        if (msgArrayList.size() != 0) {
            patientRV.smoothScrollToPosition(msgArrayList.size() - 1);
        }

//        Register all broadcast receivers.
        LocalBroadcastManager.getInstance(this).registerReceiver(updatePatientReciever, new IntentFilter("updatePatient"));
        LocalBroadcastManager.getInstance(this).registerReceiver(errorReciever, new IntentFilter("error"));
        LocalBroadcastManager.getInstance(this).registerReceiver(confirmMsgReceiver, new IntentFilter("confirmMsg"));
        LocalBroadcastManager.getInstance(this).registerReceiver(chatUpdateReceiver, new IntentFilter("chatUpdate"));

    }

//    Set the service connection used to bind the background service.
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketBackgroundService.MBinder mBinder = (SocketBackgroundService.MBinder) service;
            socketBackgroundService = mBinder.getServiceBinder();
            bounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bounded = false;
        }
    };

//    OnClick handler for the "send" button.
    public void sendMsgOnClick(View v) {
        String msg = chatInputTextTV.getText().toString();
        if (!msg.equals("")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//            Make a new Message object using the input text.
//            Call the background function to send the message
            if (!isSenior) {
                Message message =
                        new Message(
                                msg, dateTimeFormatter.format(LocalDateTime.now()), "doctor", patient.id, patient.docID, senior);
                if (bounded) {
                    socketBackgroundService.sendMessage(patient, message);
                }
            } else {
                Message message;
                if (patient.docID.equals(senior)){
                    message =
                            new Message(
                                    msg, dateTimeFormatter.format(LocalDateTime.now()), "doctor", patient.id, patient.docID, senior);
                }else{
                    message =
                            new Message(
                                    msg, dateTimeFormatter.format(LocalDateTime.now()), "senior", patient.id, patient.docID, senior);

                }
//                Message message =
//                        new Message(
//                                msg, dateTimeFormatter.format(LocalDateTime.now()), "senior", patient.id, patient.docID, senior);
                Log.d("MSG", String.valueOf(patient.docID.equals(senior)));
                if (bounded) {
                    socketBackgroundService.sendMessage(patient, message);
                }
            }
        }
    }
//    OnClick handler for the "call" button
    public void callBTOnClick(View v){
//        Call the system service to dial the patient's number.
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri uri = Uri.parse("tel:"+phone);
        intent.setData(uri);
        startActivity(intent);
    }

//    Onclick handler for the "statistic" button
    public void statisticOnClick(View v){
//        Log.d("patientID", patient.id);
//        Redirect to the statistic activity

        Intent intent = new Intent(this, Statistics.class);
        intent.putExtra("patientID", patient.id);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
//        When the activity is paused,
//        unbind the background service and unregister all broadcast receivers.
        Log.d("boundedPause", String.valueOf(bounded));
        if (bounded){
            unbindService(serviceConnection);
            bounded = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updatePatientReciever);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(chatUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorReciever);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(confirmMsgReceiver);
    }

}