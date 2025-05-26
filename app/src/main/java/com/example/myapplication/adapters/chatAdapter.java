package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.MessageModel;

import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MessageModel> messages;
    private Context context;
    private String receiverName;

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public chatAdapter(Context context, List<MessageModel> messages, String receiverName) {
        this.context = context;
        this.messages = messages;
        this.receiverName = receiverName;
    }

    @Override
    public int getItemViewType(int position) {
        // Gửi = TYPE_SENT, Nhận = TYPE_RECEIVED
        return messages.get(position).isSentByMe() ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).txtMessage.setText(message.getText());
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).txtMessage.setText(message.getText());
            ((ReceivedViewHolder) holder).txtName.setText(receiverName);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.YourMess); // ID trong item_message_sent.xml
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        TextView txtName;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.YourMess);
            txtName = itemView.findViewById(R.id.YourName);// ID trong item_message_received.xml
        }
    }
}
