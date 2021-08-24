package com.example.dummypatientclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

//Customizable Dialog box fragment
//Use newInstance to create a new dialog box
public class DialogFrag1option extends DialogFragment {

  public DialogFrag1option() {}

  public static DialogFrag1option newInstance(String title, String msg, String option) {

    DialogFrag1option frag = new DialogFrag1option();
    Bundle bundle = new Bundle();
    bundle.putString("title", title);
    bundle.putString("msg", msg);
    bundle.putString("option", option);
    frag.setArguments(bundle);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getArguments().getString("title"));
    builder.setMessage(getArguments().getString("msg"));
    builder.setPositiveButton(
        getArguments().getString("option"),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {}
        });
    return builder.create();
  }
}
