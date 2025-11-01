package com.example.aichatassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GeminiApi geminiApi;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText etMessage;
    private ImageButton btnSend, btnMic;

    // Database object
    ChatDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize database
        db = new ChatDatabase(this);

        // ✅ Initialize Gemini API with key
        geminiApi = new GeminiApi("AIzaSyDXrGtLV0HnX2UAshjt50kfQXLYB9PCCqM");

        // Load old chat history
        loadChatHistory();

        btnSend.setOnClickListener(v -> sendMessage());
        btnMic.setOnClickListener(v -> startVoiceInput());
    }

    // 🧩 Load old chats when app starts
    private void loadChatHistory() {
        Cursor cursor = db.getAllMessages();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String msg = cursor.getString(1); // message text
                boolean isUser = cursor.getInt(2) == 1; // user=1, bot=0
                messages.add(new ChatMessage(msg, isUser));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messages.size() - 1);
        }
    }

    // 🧩 Send new message (save + display)
    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        // 👩‍💻 User message
        messages.add(new ChatMessage(text, true));
        adapter.notifyDataSetChanged();
        db.insertMessage(text, true);
        etMessage.setText("");
        recyclerView.scrollToPosition(messages.size() - 1);

        // 🤖 Loading message
        messages.add(new ChatMessage("Thinking... 🤖", false));
        adapter.notifyDataSetChanged();

        // ✅ Limit conversation history (Fix HTTPS GIVEN_URL_LARGE)
        final int MAX_HISTORY_SIZE = 10;  // sirf last 10 messages bhejna
        List<ChatMessage> limitedMessages;
        if (messages.size() > MAX_HISTORY_SIZE) {
            limitedMessages = messages.subList(messages.size() - MAX_HISTORY_SIZE, messages.size());
        } else {
            limitedMessages = new ArrayList<>(messages);
        }

        // 🌐 Run API call in background thread
        new Thread(() -> {
            geminiApi.getReplyWithHistory(limitedMessages, new GeminiApi.GeminiCallback() {
                @Override
                public void onResponse(String reply) {
                    runOnUiThread(() -> {
                        messages.remove(messages.size() - 1);
                        messages.add(new ChatMessage(reply, false));
                        adapter.notifyDataSetChanged();
                        db.insertMessage(reply, false);
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("Error: " + error, false));
                        adapter.notifyDataSetChanged();
                    });
                }
            });
        }).start();
    }

    // 🎤 Voice Input (Speech to Text)
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etMessage.setText(result.get(0));
        }
    }
}
