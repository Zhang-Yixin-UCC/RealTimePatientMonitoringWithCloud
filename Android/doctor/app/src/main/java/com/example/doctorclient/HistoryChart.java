package com.example.doctorclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//The controller for the history_chat activity
public class HistoryChart extends AppCompatActivity {
    private String id;
    private WebView webView;
    private AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_chart);
        appConfig = new AppConfig();
        id = getIntent().getStringExtra("id");
        webView = findViewById(R.id.historyChartWV);
        String uri = appConfig.getServerUrl() + "getPatientStatisticPic";
//        Make a String request to request the html file.
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                uri,
                response -> {
                    Log.d("response", response);
    //                Ask the webview to load the HTML string.
                    webView.loadData(response, "text/html", "UTF-8");

                },
                error -> {
                    if (error.networkResponse == null) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Server not reachable.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "serviceNotReachableDialog");
                        });
                    } else if (error.networkResponse.statusCode == 404) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Doctor not found or password not correct.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "doctorNotFoundDialog");
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
                }){
//            Add the patient ID as a form parameter.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("id", id);
                return param;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("Content-Type","application/x-www-form-urlencoded");
                return param;
            }
        };
        stringRequest.setShouldCache(false);
//        Send the request.
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);






    }
}