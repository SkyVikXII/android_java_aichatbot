package com.nhom4.aichatbot;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Message;

import java.io.IOException;
import java.util.List;
import okhttp3.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiCall {
    private static final String TAG = "ApiCall";
    private final OkHttpClient client;
    private final Gson gson;

    public interface ApiResponseListener {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public ApiCall() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public void makeApiCall(String apiEndpoint, String apiKey, String modelName, int maxResponseToken, float temperature, float repetitionPenalty, float topP, String userMessage, List<Message> conversationHistory, Character userCharacter, Character aiCharacter, List<String> systemPrompts, ApiResponseListener listener) {
        try {
            JsonArray messagesArray = new JsonArray();

            // Add system prompts
            for (String prompt : systemPrompts) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", prompt);
                messagesArray.add(systemMessage);
            }

            // Add conversation history
            for (Message msg : conversationHistory) {
                JsonObject messageObj = new JsonObject();
                String role = msg.getRole().equals(userCharacter.getId()) ? "user" : "assistant";
                messageObj.addProperty("role", role);
                messageObj.addProperty("content", msg.getContent());
                messagesArray.add(messageObj);
            }

            // Add the current user message
            JsonObject userMessageObj = new JsonObject();
            userMessageObj.addProperty("role", "user");
            userMessageObj.addProperty("content", userMessage);
            messagesArray.add(userMessageObj);

            // Create the request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", modelName);
            requestBody.add("messages", messagesArray);
            requestBody.addProperty("max_tokens", maxResponseToken);
            requestBody.addProperty("temperature", temperature);
            requestBody.addProperty("repetition_penalty", repetitionPenalty);
            requestBody.addProperty("top_p", topP);

            String requestBodyString = requestBody.toString();
            RequestBody body = RequestBody.create(requestBodyString, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(apiEndpoint)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage(), e);
                    listener.onFailure("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                                JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                                JsonObject message = choice.getAsJsonObject("message");
                                String aiResponse = message.get("content").getAsString();
                                listener.onSuccess(aiResponse);
                            } else {
                                listener.onFailure("No choices in response");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            listener.onFailure("Error parsing response");
                        }
                    } else {
                        Log.e(TAG, "API Error: " + response.code() + " - " + responseBody);
                        listener.onFailure("API error " + response.code() + ": " + responseBody);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in makeApiCall: " + e.getMessage(), e);
            listener.onFailure("Error: " + e.getMessage());
        }
    }
}
