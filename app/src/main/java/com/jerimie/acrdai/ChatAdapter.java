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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    // User bubble: indigo-ish; AI bubble: dark grey
    private static final int COLOR_USER = Color.parseColor("#3F51B5");
    private static final int COLOR_AI   = Color.parseColor("#424242");

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        holder.textView.setText(msg.getText());

        if (msg.isUser()) {
            holder.textView.setBackgroundColor(COLOR_USER);
            holder.textView.setTextColor(Color.WHITE);
            // Align bubble to the END (right)
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) holder.textView.getLayoutParams();
            params.gravity = Gravity.END;
            holder.textView.setLayoutParams(params);
        } else {
            holder.textView.setBackgroundColor(COLOR_AI);
            holder.textView.setTextColor(Color.WHITE);
            // Align bubble to the START (left)
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) holder.textView.getLayoutParams();
            params.gravity = Gravity.START;
            holder.textView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
