package com.example.doctorclient;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//The adapter for the chat recycler view.
//Takes in an array list of Message objects and if the user is senior doctor.
public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Message> data;
    private Context context;
    private boolean isSenior;

    public ChatRecyclerViewAdapter(Context context, ArrayList<Message> data, boolean isSenior) {
        this.context = context;
        this.data = data;
        this.isSenior = isSenior;
    }

//    Inflate the layout.
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ViewHolder(view);
    }

//    Put the messages into the view holder
    @Override
    public void onBindViewHolder(@NonNull ChatRecyclerViewAdapter.ViewHolder holder, int position) {
        if (data != null) {
            Message message = data.get(position);
            Log.d("msg", message.toString());
            if (message.from.equals("doctor")) {
                if (!isSenior){
                holder.getLeftTimeTV().setText("");
                holder.getLeftMsgTV().setText("");

                holder.getRightMsgTV().setText(message.msg);
                holder.getRightTimeTV().setText(message.time);}
                else{
                    holder.getRightTimeTV().setText("");
                    holder.getRightMsgTV().setText("");

                    holder.getLeftMsgTV().setText(message.msg);
                    holder.getLeftTimeTV().setText(message.time);

                }
            } else if (message.from.equals("patient")) {
                holder.getRightTimeTV().setText("");
                holder.getRightMsgTV().setText("");

                holder.getLeftMsgTV().setText(message.msg);
                holder.getLeftTimeTV().setText(message.time);
                holder.getLeftMsgTV().setTextColor(Color.RED);
            }else if(message.from.equals("senior")){
                if (isSenior){
                    holder.getLeftTimeTV().setText("");
                    holder.getLeftMsgTV().setText("");

                    holder.getRightMsgTV().setText(message.msg);
                    holder.getRightTimeTV().setText(message.time);
                }else{
                    holder.getRightTimeTV().setText("");
                    holder.getRightMsgTV().setText("");

                    holder.getLeftMsgTV().setText(message.msg);
                    holder.getLeftTimeTV().setText(message.time);
                    holder.getLeftMsgTV().setTextColor(Color.BLUE);

                }
            }
        }
    }

//    Get the count of messages
    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

//    Create the view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView leftMsgTV, rightMsgTV, leftTimeTV, rightTimeTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leftMsgTV = itemView.findViewById(R.id.leftMsgTV);
            rightMsgTV = itemView.findViewById(R.id.rightMsgTV);
            leftTimeTV = itemView.findViewById(R.id.leftTimeTV);
            rightTimeTV = itemView.findViewById(R.id.rightTimeTV);
        }

//        Util functions for getting textViews
        public TextView getLeftMsgTV() {
            return leftMsgTV;
        }

        public TextView getRightMsgTV() {
            return rightMsgTV;
        }

        public TextView getLeftTimeTV() {
            return leftTimeTV;
        }

        public TextView getRightTimeTV() {
            return rightTimeTV;
        }
    }

//    Util function for updating the whole chat recycler view.
    public void updateAdapter(ArrayList<Message> data) {
        this.data = data;
        notifyDataSetChanged();
    }

//    Util function for inserting the message to the bottom.
    public void insertAtBottom(ArrayList<Message> data) {
        this.data = data;
        notifyItemInserted(data.size() - 1);
    }


}
