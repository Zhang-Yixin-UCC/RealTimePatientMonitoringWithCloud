package com.example.doctorclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Map;

//Adapter for the patient list recycler view.
//Takes in an array list of Patient object and new message mapping.
//The new message mapping maps if there is new message to patient ID.
public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.ViewHolder> {
//    private JSONObject dataset;
    private Map<String, Boolean> newMsgMap;
    private ArrayList<Patient> data;
    private Context context;
    private String docID;
    private boolean isSenior;
    private String senior;

//    Create the view holders
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView recyclerviewRowNameTV;
        private final LinearLayout recyclerviewRowBG;
        private final  TextView newMsgTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerviewRowNameTV = itemView.findViewById(R.id.recyclerviewRowNameTV);
            recyclerviewRowBG = itemView.findViewById(R.id.recyclerviewRowBG);
            newMsgTV = itemView.findViewById(R.id.newMsgTV);

        }
//        Util functions for getting text views and linear layout.
        public TextView getTextView(){
            return recyclerviewRowNameTV;
        }
        public LinearLayout getLinearLayout(){return recyclerviewRowBG;}
        public TextView getNewMsgTV() {return newMsgTV;}
    }

//    Constructor
    public PatientListAdapter(Context context, ArrayList<Patient> params, String docID, Map<String,Boolean> newMsgMap, boolean isSenior, String senior){
        this.context = context;
        this.data = params;
        this.docID = docID;
        this.newMsgMap = newMsgMap;
        this.isSenior = isSenior;
        this.senior = senior;
//        Log.d("dataset is null", String.valueOf(dataset == null));
    }

//    Inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view) ;
    }

//    Put the data into the view holders
//    Set the OnClick listeners to the layout.
//    Clicking the layout will redirect to the patient information activity.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (data != null) {
            try {
                Patient patient = data.get(position);
                holder.getTextView().setText(patient.name);

                if (patient.color != null){
                    switch (patient.color) {
                        case "Orange":
                            holder.getTextView().setTextColor(Color.BLACK);
                            holder.getLinearLayout().setBackgroundColor(Color.parseColor("#FFA500"));
                            break;
                        case "Red":
                            holder.getTextView().setTextColor(Color.BLACK);
                            holder.getLinearLayout().setBackgroundColor(Color.RED);
                            break;
                        case "Normal":
                            holder.getTextView().setTextColor(Color.BLACK);
                            holder.getLinearLayout().setBackgroundColor(Color.WHITE);
                            break;
                        case "Black":
                            holder.getTextView().setTextColor(Color.WHITE);
                            holder.getLinearLayout().setBackgroundColor(Color.BLACK);
                            break;
                    }
                }
//                Log.d("patientAdapter", String.valueOf(patient == null));
                if (newMsgMap.containsKey(patient.id)) {
                    if (newMsgMap.get(patient.id)) {
                        holder.getNewMsgTV().setText("New Message");
                    } else {
                        holder.getNewMsgTV().setText("");
                    }
                }

                holder.getLinearLayout().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent("readMsg");
                        intent1.putExtra("patientID", patient.id);
                        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent1);

                        Intent intent = new Intent(v.getContext(), PatientInfo.class);
                        intent.putExtra("patient", data.get(position));
                        intent.putExtra("doctorID", docID);
                        intent.putExtra("isSenior", isSenior);
                        intent.putExtra("senior", senior);
                        v.getContext().startActivity(intent);
                    }
                });


            } catch (Exception e) {
                Log.d("exceptionAdapter", e.toString());
                DialogFrag1option dialogFrag1option = DialogFrag1option.newInstance("Error", "Adapter bind failed.\nContact IT.>", "OK");
                dialogFrag1option.show(((AppCompatActivity) context).getSupportFragmentManager(), "jsonParseErrorDialog");

            }
        }
    }

//    Util function for getting the count of patients.
    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
            return 0;


    }

//    Util function for updating the whole patient list recycler view.
    public  void updateAdapter(ArrayList<Patient> newData){
        data = newData;
        notifyDataSetChanged();
    }

//    Util function for updating a specific patient.
    public void updateAdapterPosition(ArrayList<Patient> newData, int position){
        data = newData;
        notifyItemChanged(position);
    }

//    Util function for updating the "New message" text.
    public void readMsgUpdateAdaptorOnPosition(ArrayList<Patient> newData, Map<String, Boolean> newNewMsgMap, int position){
        data = newData;
        newMsgMap = newNewMsgMap;
        notifyItemChanged(position);
        }

}