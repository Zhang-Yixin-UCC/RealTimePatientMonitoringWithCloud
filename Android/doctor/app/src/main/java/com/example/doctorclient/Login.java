package com.example.doctorclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.Hashing;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import android.util.Base64;

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Login extends AppCompatActivity {
    private TextView doctoridTV, passwordTV, phoneNumberTV;
    private Button loginBT;
    private RequestQueue requestQueue;
    private AppConfig appConfig;
    private String pbk = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        doctoridTV = findViewById(R.id.doctorIDTV);
        passwordTV = findViewById(R.id.passwordTV);
        loginBT = findViewById(R.id.loginBT);
        phoneNumberTV = findViewById(R.id.phoneNumberTV);
        requestQueue = RequestQueueSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        appConfig = new AppConfig();
        String getPbkUrl = appConfig.getServerUrl() + "getPBK";
        getPBK(getPbkUrl);
    }

    public void getPBK(String url) {
        StringRequest stringRequest = new StringRequest(url,
                response -> {
                    pbk = response.substring(0);
                    pbk = pbk.replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replace("\n", "")
                            .trim();
                },
                error -> {

                    if (error.networkResponse == null) {
                        runOnUiThread(() -> {
                            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Server not reachable.", "OK");
                            dialogFrag1option.show(getSupportFragmentManager(), "serviceNotReachableDialog");
                        });
                    } else {
                        DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Unknown Error.<PBK get failed>\nContact IT staff.", "OK");
                        dialogFrag1option.show(getSupportFragmentManager(), "unknownErrorDialog");
                    }
                }
        );
        RequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    public void loginDoc(String url, String params) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Log.d("data", params);
        Log.d("pbk", pbk);
        if (pbk != null) {
            byte[] pbkBytes = Base64.decode(pbk, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pbkBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] encrypted = cipher.doFinal(params.getBytes());
            String encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);
            Log.d("encrypted", encoded);
            Map<String, String> param = new HashMap<>();
            param.put("value", encoded);
            JSONObject jsonParam = new JSONObject(param);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    url,
                    jsonParam,
                    response -> {
                        try {
                            String seniorID = response.getString("senior");
                            String doctorID = doctoridTV.getText().toString();
                            Intent intent = new Intent(Login.this, PatientList.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("doctorID", doctorID);
                            if (!seniorID.equals("")){
                                bundle.putBoolean("isSenior", false);
                                bundle.putString("senior", seniorID);
                            }else{
                                bundle.putBoolean("isSenior", true);
                                bundle.putString("senior",doctorID);
                            }
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Log.d("exception", e.toString());
                        }

                    }, error -> {
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
            }
            );
            jsonObjectRequest.setShouldCache(false);
            RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
        else{
            DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Connection Failed.<Get PBK failed>\nRestart app. If this problem reoccurs contact IT staff", "OK");
            dialogFrag1option.show(getSupportFragmentManager(), "connectionFailedDialog");
        }


    }


    public void loginEventHandler(View v) {
        String doctorid = doctoridTV.getText().toString();
        String password = passwordTV.getText().toString();
        String phone = phoneNumberTV.getText().toString();
        if (doctorid.equals("") || password.equals("")||phone.equals("")) {
            Toast.makeText(this, "Must fill in ID and password and phone.", Toast.LENGTH_SHORT).show();
        } else {
            String passwordHash = Hashing.sha256().hashString(password, StandardCharsets.UTF_16).toString();
            Log.d("passwordhash", password);
            Log.d("passwordhash", passwordHash);
            Map<String, String> params = new HashMap<>();
            params.put("doctorID", doctorid);
            params.put("passwordHash", passwordHash);
            params.put("phone",phone);
            String loginDetails = String.valueOf(new JSONObject(params));
            String loginUrl = appConfig.getServerUrl() + "docLogin";

            try {
                loginDoc(loginUrl, loginDetails);
            } catch (Exception e) {
                Log.d("ex", e.toString());
            }
        }
    }
}