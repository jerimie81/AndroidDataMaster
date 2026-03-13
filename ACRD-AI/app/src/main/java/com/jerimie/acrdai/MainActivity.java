package com.jerimie.acrdai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("rust_core");
    }

    private native String processGeminiPrompt(String prompt);

    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private EditText editTextPrompt;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextPrompt = findViewById(R.id.editTextPrompt);
        Button buttonSend = findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter(messages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> sendPrompt());
    }

    private void sendPrompt() {
        final String prompt = editTextPrompt.getText().toString().trim();
        if (prompt.isEmpty()) {
            return;
        }

        appendMessage(new ChatMessage(prompt, true));
        editTextPrompt.setText("");

        executorService.execute(() -> {
            String response;
            try {
                response = processGeminiPrompt(prompt);
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            final String finalResponse = response;
            mainHandler.post(() -> appendMessage(new ChatMessage(finalResponse, false)));
        });
    }

    private void appendMessage(ChatMessage message) {
        messages.add(message);
        int newIndex = messages.size() - 1;
        chatAdapter.notifyItemInserted(newIndex);
        recyclerViewChat.scrollToPosition(newIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
