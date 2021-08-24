package com.example.doctorclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

//The background service that handles the WS connection.
public class SocketBackgroundService extends Service {
    private MBinder binder = new MBinder();
    public class MBinder extends Binder {
        public SocketBackgroundService getServiceBinder() {
            return SocketBackgroundService.this;
        }
    }

    private AppConfig appConfig;
    private Socket mSocket;
    private Context context;
    private String doctorID = "";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("serviceThing", "onBind");
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("serviceThing", "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("serviceThing", "onCreate");
        appConfig = new AppConfig();
        Log.d("serviceThing", doctorID);
        context = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("serviceThing", "onDestory");
    }

//    The debug function
    public void hello() {
        Log.d("serviceThing", "hello");
    }

//    Set the doctor ID.
    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

//  Function that establish the WS connection to the server
    public void connectSocket() {
//        Identify the client when connecting
        IO.Options opts = new IO.Options();
        opts.query = "type=" + "doc" + "&docID=" + doctorID;
        try {
            mSocket = IO.socket(appConfig.getServerUrl(), opts);
        } catch (URISyntaxException e) {
            Log.d("serviceThing", e.toString());
        }

//        Set the listener for receiving vital data
        mSocket.on("patientMonitorUpdate", args -> {
            try {
                Log.d("service", Arrays.toString(args));
//                Put the vital sign data to an intent
//                Broadcast the intent throughout the application.
                JSONObject params = (JSONObject) args[0];
                Patient patient = new Patient(params.getString("name"), params.getString("id"), params.getString("tag"), params.getString("color"), params.getString("heartrate"), params.getString("spo2"), params.getString("doctorID"), params.getString("phone"), Boolean.parseBoolean(params.getString("allowReply")));
                Intent intent = new Intent("updatePatient");
                intent.putExtra("patient", patient);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            } catch (Exception e) {
                Log.d("exception", e.toString());
                Intent intent = new Intent("error");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

//        Set the listener for receiving chat message
        mSocket.on("chatUpdate", args -> {
            try {
                JSONObject messageJSON = (JSONObject) args[0];
//                Create a Message object and store the message in the shared preference.
                Message message =
                        new Message(
                                messageJSON.getString("msg"),
                                messageJSON.getString("time"),
                                messageJSON.getString("from"),
                                messageJSON.getString("patientID"),
                                messageJSON.getString("doctorID"),
                                messageJSON.getString("seniorID"));
//                Log.d("msg", message.toString());
                SharedPreferences sharedPreferences = getSharedPreferences(doctorID + "_" +messageJSON.getString("patientID"), MODE_PRIVATE );
                String msgString = sharedPreferences.getString("msg", "");
                JSONObject msgJson = message.toJSONObject();
                if (msgString.equals("")){
                    JSONArray msgJsonArray = new JSONArray();
                    msgJsonArray.put(msgJson);
                    sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
                }else{
                    JSONArray msgJsonArray = new JSONArray(msgString);
                    msgJsonArray.put(msgJson);
                    sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
                }
//                Put the Message object in an intent and broadcast throughout the application.
                Intent intent = new Intent("chatUpdate");
                intent.putExtra("msg", message);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }catch (Exception e){
                Log.d("exception", e.toString());
            }
        });
//        Connect to the server using the Socket
        if(mSocket.connected()){
            mSocket.disconnect();
            mSocket.connect();
        }else{
            mSocket.connect();
        }
    }

//    Function that disconnect the WS connection.
    public void disconnectSocket(){
        if (mSocket.connected()){
        mSocket.disconnect();}
    }

//    Function that send the message to the server
    public void sendMessage(Patient patient, Message message) {
        if (mSocket.connected()) {
//            Register the listener for confirming event
            mSocket.on("confirm", args -> {
//                Fetch the message from the event.
//                Store the message in the shared preference.
                JSONObject msgJsonArg = (JSONObject) args[0];
                Log.d("msg", msgJsonArg.toString());
                try {
                    Message message1 =
                            new Message(
                                    msgJsonArg.getString("msg"),
                                    msgJsonArg.getString("time"),
                                    msgJsonArg.getString("from"),
                                    msgJsonArg.getString("patientID"),
                                    msgJsonArg.getString("doctorID"),
                                    msgJsonArg.getString("seniorID"));
                    SharedPreferences sharedPreferences = getSharedPreferences(doctorID + "_" + patient.id, MODE_PRIVATE);
                    String msgString = sharedPreferences.getString("msg", "");
                    JSONObject msgJson = message1.toJSONObject();
                    if (msgString.equals("")){
                        JSONArray msgJsonArray = new JSONArray();
                        msgJsonArray.put(msgJson);
                        sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();
                    }else{
                        JSONArray msgJsonArray = new JSONArray(msgString);
                        msgJsonArray.put(msgJson);
                        sharedPreferences.edit().putString("msg", msgJsonArray.toString()).apply();

                    }
                    Log.d("msg", message1.toString());
//                   Put the message in the intent and broadcast it.
                    Intent intent = new Intent("confirmMsg");
                    intent.putExtra("msg", message1);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } catch (Exception e) {
                    Log.d("exception", e.toString());
                }
//                Unregister the confirm listener.
                mSocket.off("confirm");
            });
//            Send the message to the server.
            mSocket.emit("uploadChatMsg", message.toJSONObject());

        }
    }


}
