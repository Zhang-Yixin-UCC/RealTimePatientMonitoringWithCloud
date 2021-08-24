package com.example.doctorclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Statistics extends AppCompatActivity {
    private String patientID;
    private RequestQueue requestQueue;
    private TextView normalTV, orangeTV, redTV, heartrateAverageTV, heartrateLowestTV, heartrateLowestTimeTV, heartrateHighestTV, heartrateHighestTimeTV, spo2AverageTV, spo2LowestTV, spo2LowestTimeTV, spo2HighestTV, spo2HighestTimeTV;
    private AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        patientID = getIntent().getStringExtra("patientID");

        Log.d("patientID", patientID);

        normalTV = findViewById(R.id.normalTV);
        orangeTV = findViewById(R.id.orangeTV);
        redTV = findViewById(R.id.redTV);

        heartrateAverageTV = findViewById(R.id.heartrateAverageTV);
        heartrateHighestTV = findViewById(R.id.heartrateHighestTV);
        heartrateHighestTimeTV = findViewById(R.id.heartrateHighestTimeTV);
        heartrateLowestTV = findViewById(R.id.heartrateLowestTV);
        heartrateLowestTimeTV = findViewById(R.id.heartrateLowestTimeTV);

        spo2AverageTV = findViewById(R.id.spo2AverageTV);
        spo2HighestTV = findViewById(R.id.spo2HighestTV);
        spo2HighestTimeTV = findViewById(R.id.spo2HighestTimeTV);
        spo2LowestTV = findViewById(R.id.spo2LowestTV);
        spo2LowestTimeTV = findViewById(R.id.spo2LowestTimeTV);

        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        appConfig = new AppConfig();
        String uri = appConfig.getServerUrl() + "getPatientStatistic";

        Map<String, String> param = new HashMap<>();
        param.put("id", patientID);
        JSONObject paramJson = new JSONObject(param);
        Log.d("paramJson", paramJson.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                uri,
                paramJson,
                response -> {
                    try{
                        normalTV.setText(response.getString("normal"));
                        orangeTV.setText(response.getString("orange"));
                        redTV.setText(response.getString("red"));

                        heartrateAverageTV.setText(response.getString("hrAverage"));
                        heartrateHighestTV.setText(response.getString("hrHighest"));
                        heartrateHighestTimeTV.setText(response.getString("hrHighestTime"));
                        heartrateLowestTV.setText(response.getString("hrLowest"));
                        heartrateLowestTimeTV.setText(response.getString("hrLowestTime"));

                        spo2AverageTV.setText(response.getString("spAverage"));
                        spo2HighestTV.setText(response.getString("spHighest"));
                        spo2HighestTimeTV.setText(response.getString("spHighestTime"));
                        spo2LowestTV.setText(response.getString("spLowest"));
                        spo2LowestTimeTV.setText(response.getString("spLowestTime"));



                    }catch(Exception e){
                        Log.d("exception", e.toString());
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("ERROR", e.toString(), "OK");
                            dialogFrag1option.show(getSupportFragmentManager(),"");
                        });
                    }
                },
                error -> {
                    if (error.networkResponse == null) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Server not reachable.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "serviceNotReachableDialog");
                        });
                    } else {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", String.valueOf(error.networkResponse.statusCode), "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "");
                        });
                    }
                }
        );

        jsonObjectRequest.setShouldCache(false);
        RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    public void historyChartOnClick(View v){
        Intent intent = new Intent(this, HistoryChart.class);
        intent.putExtra("id", patientID);
        startActivity(intent);
    }
}