package com.example.dummypatientclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
  private TextView patientIDTV;
  private RequestQueue requestQueue;
  private AppConfig appConfig;
  private TextView phoneNumberTV;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    patientIDTV = findViewById(R.id.patientIDTV);
    phoneNumberTV = findViewById(R.id.phoneNumberTV);

    requestQueue = Volley.newRequestQueue(this);
    appConfig = new AppConfig();
  }

  public void loginEventHandler(View v) {
    String patientID = patientIDTV.getText().toString();
    String phoneNumber= phoneNumberTV.getText().toString();
    if (patientID.equals("")||phoneNumber.equals("")) {
      Toast.makeText(this, "Must input patient ID and Phone number.", Toast.LENGTH_SHORT).show();
    } else {
      Map<String, String> param = new HashMap<>();
      param.put("patientID", patientID);
      param.put("phone", phoneNumber);
      JSONObject jsonParam = new JSONObject(param);
      String url = appConfig.getServerUrl() + "patientLogin";
      JsonObjectRequest jsonObjectRequest =
          new JsonObjectRequest(
              Request.Method.POST,
              url,
              jsonParam,
                  response -> {
                    try {
                      Log.d("patientid", response.getString("id"));
                      Log.d("patientName", response.getString("name"));

                      String patientID1 = response.getString("id");
                      String patientName = response.getString("name");
                      String patientTag = response.getString("tag");
                      String doctorID = response.getString("docID");
                      String senior = response.getString("senior");
                      Intent intent = new Intent(Login.this, PatientMonitor.class);
                      Bundle bundle = new Bundle();
                      bundle.putString("id", patientID1);
                      bundle.putString("name", patientName);
                      bundle.putString("tag", patientTag);
                      bundle.putString("docID", doctorID);
                      bundle.putString("senior", senior);
                      intent.putExtras(bundle);
                      startActivity(intent);
                    } catch (Exception e) {
                      Log.d("exception", e.toString());
                      DialogFrag1option dialogFrag1option =
                          DialogFrag1option.newInstance(
                              "Error",
                              "Unknown Error. <Response Parse>\nPlease contact IT staff.",
                              "OK");
                      dialogFrag1option.show(
                          getSupportFragmentManager(), "UnknownErrorResponseParseDialog");
                    }
                  },
              new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                  if (error.networkResponse == null) {
                    runOnUiThread(
                        new Runnable() {
                          @Override
                          public void run() {
                            DialogFrag1option dialogFrag1option =
                                DialogFrag1option.newInstance(
                                    "Error", "Service unreachable.", "OK");
                            dialogFrag1option.show(
                                getSupportFragmentManager(), "ServiceUnreachableDialog");
                          }
                        });
                  } else if (error.networkResponse.statusCode == 404) {
                    DialogFrag1option dialogFrag1option =
                        DialogFrag1option.newInstance("Error", "Patient ID not found.", "OK");
                    dialogFrag1option.show(getSupportFragmentManager(), "patientidNotFoundDialog");

                  } else if (error.networkResponse.statusCode == 500) {
                    DialogFrag1option dialogFrag1option =
                        DialogFrag1option.newInstance(
                            "Error", "Database error.\nContact IT staff.", "OK");
                    dialogFrag1option.show(getSupportFragmentManager(), "databaseErrorDialog");
                  } else {
                    DialogFrag1option dialogFrag1option =
                        DialogFrag1option.newInstance(
                            "Error", "Unknown Error.<DB query failed>\nContact IT staff.", "OK");
                    dialogFrag1option.show(getSupportFragmentManager(), "unknownErrorDialog");
                  }
                }
              });
      jsonObjectRequest.setShouldCache(false);
      requestQueue.add(jsonObjectRequest);
    }
  }
}
