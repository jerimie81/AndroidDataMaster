package com.jerimie.acrdai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private RecyclerView recyclerViewChat;

    private EditText editTextPrompt;
    private Button buttonSend;

    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextPrompt   = findViewById(R.id.editTextPrompt);
        buttonSend       = findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> onSendClicked());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // -------------------------------------------------------------------------
    // Send logic
    // -------------------------------------------------------------------------
    private void onSendClicked() {
        String prompt = editTextPrompt.getText().toString().trim();
        if (TextUtils.isEmpty(prompt)) {
            return;
        }

        // 1. Append user message immediately and clear input
        appendMessage(new ChatMessage(prompt, true));
        editTextPrompt.setText("");
        buttonSend.setEnabled(false);

        // 2. Run JNI call off the main thread
        executor.execute(() -> {
            String result;
            try {
                result = processGeminiPrompt(prompt);
                if (result == null || result.isEmpty()) {
                    result = "(empty response)";
                }
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }

            final String finalResult = result;

            // 3. Post result back to the main thread
            mainHandler.post(() -> {
                appendMessage(new ChatMessage(finalResult, false));
                buttonSend.setEnabled(true);
            });
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void appendMessage(ChatMessage message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }
}
