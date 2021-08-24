package com.example.dummypatientclient;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ChatRecyclerViewAdapter
    extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {
  private ArrayList<Message> data;
  private Context context;

  public ChatRecyclerViewAdapter(Context context, ArrayList<Message> data) {
    this.context = context;
    this.data = data;
  }

  @NotNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(
      @NonNull @NotNull ChatRecyclerViewAdapter.ViewHolder holder, int position) {
    if (data != null) {
      Message message = data.get(position);
      if (message.from.equals("patient")) {
        holder.getLeftTimeTV().setText("");
        holder.getLeftMsgTV().setText("");

        holder.getRightMsgTV().setText(message.msg);
        holder.getRightTimeTV().setText(message.time);
      } else if (message.from.equals("doctor")) {
        holder.getRightTimeTV().setText("");
        holder.getRightMsgTV().setText("");

        holder.getLeftMsgTV().setText(message.msg);
        holder.getLeftTimeTV().setText(message.time);
        holder.getLeftMsgTV().setTextColor(Color.BLACK);
      }else if (message.from.equals("senior")){
        holder.getRightTimeTV().setText("");
        holder.getRightMsgTV().setText("");

        holder.getLeftMsgTV().setText(message.msg);
        holder.getLeftTimeTV().setText(message.time);
        holder.getLeftMsgTV().setTextColor(Color.RED);
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

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView leftMsgTV, rightMsgTV, leftTimeTV, rightTimeTV;

    public ViewHolder(@NonNull @NotNull View itemView) {
      super(itemView);
      leftMsgTV = itemView.findViewById(R.id.leftMsgTV);
      rightMsgTV = itemView.findViewById(R.id.rightMsgTV);
      leftTimeTV = itemView.findViewById(R.id.leftTimeTV);
      rightTimeTV = itemView.findViewById(R.id.rightTimeTV);
    }

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

  public void updateAdapter(ArrayList<Message> data) {
    this.data = data;
    notifyDataSetChanged();
  }

  public void insertAtBottom(ArrayList<Message> data) {
    this.data = data;
    notifyItemInserted(data.size() - 1);
  }
}
