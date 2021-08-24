package com.example.doctorclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class DialogFrag1option extends DialogFragment {

    public DialogFrag1option(){}

    public static DialogFrag1option newInstance(String title, String msg, String option){

        DialogFrag1option frag = new DialogFrag1option();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        bundle.putString("option", option);
        frag.setArguments(bundle);
        return frag;
    }
    public  static DialogFrag1option newInstance(String title, String msg, String option, int mode){
        DialogFrag1option frag = new DialogFrag1option();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        bundle.putString("option", option);
        bundle.putInt("mode", mode);
        frag.setArguments(bundle);
        return frag;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Log.d("bundlesize", String.valueOf(getArguments().size()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("msg"));
        if (getArguments().size() == 3){
            builder.setPositiveButton(getArguments().getString("option"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }else{
            int mode = getArguments().getInt("mode");
            if (mode == 1){
                builder.setPositiveButton(getArguments().getString("option"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();

                 }
                });
            }else if (mode == 2){
                builder.setPositiveButton(getArguments().getString("option"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(),Login.class);
                        startActivity(intent);
                    }
                });

            }

        }





        return builder.create();
    }

}
