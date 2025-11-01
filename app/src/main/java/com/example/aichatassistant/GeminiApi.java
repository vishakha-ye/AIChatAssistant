package com.example.aichatassistant;



import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GeminiApi {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private String apiKey;

    public GeminiApi(String apiKey) {
        this.apiKey = apiKey;
    }

    // ✅ With Chat History
    public void getReplyWithHistory(List<ChatMessage> history, GeminiCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(API_URL + apiKey);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);

                // JSON Build
                JSONArray contents = new JSONArray();
                for (ChatMessage msg : history) {
                    JSONObject content = new JSONObject();
                    JSONArray parts = new JSONArray();
                    JSONObject textPart = new JSONObject();
                    textPart.put("text", (msg.isUser() ? "User: " : "AI: ") + msg.getMessage());
                    parts.put(textPart);
                    content.put("parts", parts);
                    contents.put(content);
                }

                JSONObject data = new JSONObject();
                data.put("contents", contents);

                // Send Request
                OutputStream os = connection.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                BufferedReader reader;

                // ✅ Agar error mila toh getErrorStream() se read karo
                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject json = new JSONObject(response.toString());
                    JSONArray candidates = json.getJSONArray("candidates");
                    JSONObject first = candidates.getJSONObject(0);
                    JSONObject contentObj = first.getJSONObject("content");
                    JSONArray partsArray = contentObj.getJSONArray("parts");
                    String reply = partsArray.getJSONObject(0).getString("text");

                    new Handler(Looper.getMainLooper()).post(() -> callback.onResponse(reply));
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onError("API Error (" + responseCode + "): " + response));
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public interface GeminiCallback {
        void onResponse(String reply);
        void onError(String error);
    }
}

