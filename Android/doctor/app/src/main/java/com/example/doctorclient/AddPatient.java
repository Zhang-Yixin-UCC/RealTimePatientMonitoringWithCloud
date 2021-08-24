package com.example.doctorclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddPatient extends AppCompatActivity {
    private TextView patientNameTV, patientTagTV,
            heartrateNormalUpperTV, heartrateNormalLowerTV,
            heartrateOrangeUpperTV, heartrateOrangeLowerTV,
            spo2NormalUpperTV, spo2NormalLowerTV,
            spo2OrangeUpperTV, spo2OrangeLowerTV;
    private String doctorID;
    private AppConfig appConfig;
    private boolean isSenior;
    private String senior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        patientNameTV = findViewById(R.id.patientNameTV);
        patientTagTV = findViewById(R.id.patientTagTV);

        heartrateNormalUpperTV = findViewById(R.id.heartrateNormalUpperTV);
        heartrateNormalLowerTV = findViewById(R.id.heartrateNormalLowerTV);
        heartrateOrangeUpperTV = findViewById(R.id.heartrateOrangeUpperTV);
        heartrateOrangeLowerTV = findViewById(R.id.heartrateOrangeLowerTV);

        spo2NormalUpperTV = findViewById(R.id.spo2NormalUpperTV);
        spo2NormalLowerTV = findViewById(R.id.spo2NormalLowerTV);
        spo2OrangeUpperTV = findViewById(R.id.spo2OrangeUpperTV);
        spo2OrangeLowerTV = findViewById(R.id.spo2OrangeLowerTV);

        appConfig = new AppConfig();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        doctorID = bundle.getString("doctorID");
        isSenior = bundle.getBoolean("isSenior");
        if (!isSenior){
            senior = bundle.getString("senior");
        }

//        SharedPreferences sharedPreferences = getApplication().getSharedPreferences("login", Context.MODE_PRIVATE);
//        doctorID = sharedPreferences.getString("doctorID", "");
        if (doctorID.equals("")){
            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Something is wrong.<Patient list Doctor ID not found>\nContact IT staff.", "OK", 2);
            dialogFrag1option.show(getSupportFragmentManager(), "patientListDoctorIDNotFoundDialog");

        }
    }

    public void setDefaultOnClick(View v) {
        heartrateNormalUpperTV.setText("110");
        heartrateNormalLowerTV.setText("75");
        heartrateOrangeUpperTV.setText("130");
        heartrateOrangeLowerTV.setText("60");

        spo2NormalUpperTV.setText("100");
        spo2NormalLowerTV.setText("96");
        spo2OrangeUpperTV.setText("100");
        spo2OrangeLowerTV.setText("93");
    }

    public void addPatientOnClick(View v) {
        String patientName = patientNameTV.getText().toString();
        String patientTag = patientTagTV.getText().toString();

        String heartrateNormalUpper = heartrateNormalUpperTV.getText().toString();
        String heartrateNormalLower = heartrateNormalLowerTV.getText().toString();
        String heartrateOrangeUpper = heartrateOrangeUpperTV.getText().toString();
        String heartrateOrangeLower = heartrateOrangeLowerTV.getText().toString();

        String spo2NormalUpper = spo2NormalUpperTV.getText().toString();
        String spo2NormalLower = spo2NormalLowerTV.getText().toString();
        String spo2OrangeUpper = spo2OrangeUpperTV.getText().toString();
        String spo2OrangeLower = spo2OrangeLowerTV.getText().toString();
        if (patientName.equals("") || patientTag.equals("")
                || heartrateNormalUpper.equals("") || heartrateNormalLower.equals("")
                || heartrateOrangeUpper.equals("") || heartrateOrangeLower.equals("")
                || spo2NormalUpper.equals("") || spo2NormalLower.equals("")
                || spo2OrangeUpper.equals("") || spo2OrangeLower.equals("")) {
            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Some fields are empty.", "OK");
            dialogFrag1option.show(getSupportFragmentManager(), "addPatientFieldEmptyDialog");
        } else {
            Map<String, String> params = new HashMap<>();
            params.put("doctorID", doctorID);
            params.put("patientName", patientName);
            params.put("patientTag", patientTag);
            params.put("heartrateNormalUpper", heartrateNormalUpper);
            params.put("heartrateNormalLower", heartrateNormalLower);
            params.put("heartrateOrangeUpper", heartrateOrangeUpper);
            params.put("heartrateOrangeLower", heartrateOrangeLower);

            params.put("spo2NormalUpper", spo2NormalUpper);
            params.put("spo2NormalLower", spo2NormalLower);
            params.put("spo2OrangeUpper", spo2OrangeUpper);
            params.put("spo2OrangeLower", spo2OrangeLower);

            params.put("isSenior", String.valueOf(isSenior));
            if (!isSenior){
                params.put("senior", senior);
            }
            JSONObject paramsJson = new JSONObject(params);
            Log.d("patientParams", params.toString());
            String url = appConfig.getServerUrl() + "regPatient";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url,
                    paramsJson,
                    response -> {
                        try {
                            String patientID = response.getString("patientID");
                            String msg = "The ID of this patient is: " + patientID + ".\nMake sure you tell the patient.";
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Patient ID", msg, "OK", 1);
                            dialogFrag1option.show(getSupportFragmentManager(), "patientIDDialog");

                        } catch (JSONException e) {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "JSON parsing error.\nContact IT staff.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "jsonParsingErrorDialog");

                        }

                    },
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
                    }
            );
            jsonObjectRequest.setShouldCache(false);
            RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        }

    }

}