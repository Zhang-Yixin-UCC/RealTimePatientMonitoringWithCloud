package com.example.bulletinboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

//The adapter for the patient list.
//takes in patient ID array list and a patient dataset: {patientID:[patientName, patientCategory]}
public class PatientRVAdapter extends RecyclerView.Adapter<PatientRVAdapter.ViewHolder> {
  private Map<String, String[]> dataset;
  private ArrayList<String> patientIDArrayList;
  private Context context;

//  Inflate the layout
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);
    return new ViewHolder(view);
  }

//  Put the data into the layout
  @Override
  public void onBindViewHolder(@NonNull PatientRVAdapter.ViewHolder holder, int position) {
    String patientID = patientIDArrayList.get(position);
    String[] patient = dataset.get(patientID);
    if (patient != null) {
      if (patient.length == 2) {
          holder.getPatientNameTV().setText(patient[0]);
          switch (patient[1]) {
              case "Black":
                  holder.getPatientNameTV().setTextColor(Color.WHITE);
                  holder.getPatientLL().setBackgroundColor(Color.BLACK);
                  break;
              case "Red":
                  holder.getPatientNameTV().setTextColor(Color.BLACK);
                  holder.getPatientLL().setBackgroundColor(Color.RED);
                  break;
              case "Orange":
                  holder.getPatientNameTV().setTextColor(Color.BLACK);
                  holder.getPatientLL().setBackgroundColor(Color.parseColor("#FFA500"));
                  break;
              case "Normal":
                  holder.getPatientNameTV().setTextColor(Color.BLACK);
                  holder.getPatientLL().setBackgroundColor(Color.WHITE);
                  break;
          }
      }
    }
  }

//  Get the count of patients
  @Override
  public int getItemCount() {
    return patientIDArrayList.size();
  }

//  Create View holder

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView patientNameTV;
    private final LinearLayout patientLL;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      patientNameTV = itemView.findViewById(R.id.patientNameTV);
      patientLL = itemView.findViewById(R.id.patientLL);
    }
    //  Util function that return the textView and the linear layout
    public TextView getPatientNameTV() {
      return patientNameTV;
    }

    public LinearLayout getPatientLL() {
      return patientLL;
    }
  }

//  Constructor
  public PatientRVAdapter(
      Context context, Map<String, String[]> dataset, ArrayList<String> patientIDArrayList) {
    this.context = context;
    this.dataset = dataset;
    this.patientIDArrayList = patientIDArrayList;
  }

//  Util function for update all patients
  public void updateAdapter(
      Map<String, String[]> newDataSet, ArrayList<String> newPatientIDArrayList) {
    dataset = newDataSet;
    patientIDArrayList = newPatientIDArrayList;
    notifyDataSetChanged();
  }

//  Util function for updating only one patient
  public void updateAdapterOnPosition(
      Map<String, String[]> newDataSet, ArrayList<String> newPatientIDArrayList, int position) {
    dataset = newDataSet;
    patientIDArrayList = newPatientIDArrayList;
    notifyItemChanged(position);
  }
}
