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

public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.ViewHolder> {
//    private JSONObject dataset;
    private Map<String, Boolean> newMsgMap;
    private ArrayList<Patient> data;
    private Context context;
    private String docID;
    private boolean isSenior;
    private String senior;

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
        public TextView getTextView(){
            return recyclerviewRowNameTV;
        }
        public LinearLayout getLinearLayout(){return recyclerviewRowBG;}
        public TextView getNewMsgTV() {return newMsgTV;}
    }

    public PatientListAdapter(Context context, ArrayList<Patient> params, String docID, Map<String,Boolean> newMsgMap, boolean isSenior, String senior){
        this.context = context;
        this.data = params;
        this.docID = docID;
        this.newMsgMap = newMsgMap;
        this.isSenior = isSenior;
        this.senior = senior;
//        Log.d("dataset is null", String.valueOf(dataset == null));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view) ;
    }

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

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
            return 0;


    }
    public  void updateAdapter(ArrayList<Patient> newData){
        data = newData;

        notifyDataSetChanged();
    }
    public void updateAdapterPosition(ArrayList<Patient> newData, int position){
        data = newData;
        notifyItemChanged(position);
    }

    public void readMsgUpdateAdaptorOnPosition(ArrayList<Patient> newData, Map<String, Boolean> newNewMsgMap, int position){
        data = newData;
        newMsgMap = newNewMsgMap;
        notifyItemChanged(position);
        }

}