package com.example.doctorclient;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

//Singleton for the request queue used in Volley
//This make sure that there is only one request queue in the application.
public class RequestQueueSingleton {
    private static RequestQueueSingleton instance;
    private static Context ctx;
    private RequestQueue requestQueue;

    private RequestQueueSingleton(Context context){
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSingleton getInstance(Context context){
        if (instance == null){
            instance = new RequestQueueSingleton(context);
        }
        return instance;
    }
    public RequestQueue getRequestQueue(){
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }
}
