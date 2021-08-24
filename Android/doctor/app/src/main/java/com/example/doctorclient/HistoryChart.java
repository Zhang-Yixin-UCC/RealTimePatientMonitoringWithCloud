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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                webView.loadData(response, "text/html", "UTF-8");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
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
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);






    }
}