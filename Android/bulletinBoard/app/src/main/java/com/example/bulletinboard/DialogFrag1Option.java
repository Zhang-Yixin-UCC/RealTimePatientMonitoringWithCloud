package com.example.bulletinboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;



//Customizable Dialog box fragment
//Use newInstance to create a new dialog box

public class DialogFrag1Option extends DialogFragment {
    private DialogFrag1Option(){}

    public static DialogFrag1Option newInstance(String title, String msg, String option) {
        DialogFrag1Option frag = new DialogFrag1Option();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        bundle.putString("option", option);
        frag.setArguments(bundle);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
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
