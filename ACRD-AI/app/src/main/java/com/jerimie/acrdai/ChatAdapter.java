package com.jerimie.acrdai;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.textViewMessage.setText(message.getText());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.textViewMessage.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.textViewMessage.setBackgroundColor(Color.parseColor("#D1FAE5"));
            holder.textViewMessage.setTextColor(Color.parseColor("#064E3B"));
        } else {
            params.gravity = Gravity.START;
            holder.textViewMessage.setBackgroundColor(Color.parseColor("#E5E7EB"));
            holder.textViewMessage.setTextColor(Color.parseColor("#111827"));
        }
        holder.textViewMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
