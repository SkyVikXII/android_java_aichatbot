package com.nhom4.aichatbot;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.Models.Message;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.Models.Prompt;

import java.io.IOException;
import java.util.List;
import okhttp3.*;

public class ApiCall {
    private static final String TAG = "ApiCall";
    private final OkHttpClient client;
    private final Gson gson;

    // Constants for API types
    private static final String API_TYPE_OPENAI = "openai";
    private static final String API_TYPE_GOOGLE = "google";

    public interface ApiResponseListener {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public ApiCall() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }
    public void makeApiCall(Endpoint endpoint, Model model, String userMessage, List<Message> conversationHistory, Character userCharacter, Character aiCharacter, List<Prompt> prompts, ApiResponseListener listener) {
        try {
            int maxTokens = safeParseInt(model.getMax_tokens(), 1000);
            float temperature = safeParseFloat(model.getTemperature(), 0.7f);
            float topP = safeParseFloat(model.getTop_p(), 1.0f);
            float frequencyPenalty = safeParseFloat(model.getFrequency_penalty(), 0.0f);

            String apiType = determineApiType(endpoint.getEndpoint_url());

            String systemPrompt = buildSystemPrompt(userCharacter, aiCharacter, prompts);
            JsonArray messages = buildMessages(systemPrompt, userMessage, conversationHistory, userCharacter);
/*
curl "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer GEMINI_API_KEY" \
-d '{
    "model": "gemini-2.0-flash",
    "messages": [
        {"role": "user", "content": "Explain to me how AI works"}
    ]
    }'
*/
            RequestBody body = createRequestBody(
                    model.getApi_model_id(),
                    messages,
                    maxTokens,
                    temperature,
                    topP,
                    frequencyPenalty,
                    apiType
            );

            Request request = createRequest(endpoint.getEndpoint_url(), endpoint.getAPI_KEY(), body);

            executeRequest(request, apiType, listener);

        } catch (Exception e) {
            Log.e(TAG, "Exception in makeApiCall: " + e.getMessage(), e);
            listener.onFailure("Error: " + e.getMessage());
        }
    }

    private String determineApiType(String apiEndpoint) {
        if (apiEndpoint.contains("generativelanguage.googleapis.com") ||
                apiEndpoint.contains("googleapis.com")) {
            return API_TYPE_GOOGLE;
        }
        return API_TYPE_OPENAI;
    }

    private int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse integer: " + value + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private float safeParseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse float: " + value + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private String buildSystemPrompt(Character userCharacter, Character aiCharacter, List<Prompt> prompts) {
        StringBuilder prompt = new StringBuilder();

        // Add system prompts (type = 1)
        for (Prompt p : prompts) {
            if (p.getType() == 1 && p.isActive()) {
                if (prompt.length() > 0) prompt.append("\n");
                prompt.append(p.getContent());
            }
        }

        // Add character info
        if (userCharacter != null || aiCharacter != null) {
            if (prompt.length() > 0) prompt.append("\n\n");

            if (userCharacter != null && hasContent(userCharacter.getDescription())) {
                prompt.append("User: ").append(userCharacter.getName()).append("\n")
                        .append(userCharacter.getDescription()).append("\n\n");
            }

            if (aiCharacter != null && hasContent(aiCharacter.getDescription())) {
                prompt.append("AI Character: ").append(aiCharacter.getName()).append("\n")
                        .append(aiCharacter.getDescription());
            }
        }
        for (Prompt p : prompts) {
            if (p.getType() == 2 && p.isActive()) {
                if (prompt.length() > 0) prompt.append("\n");
                prompt.append(p.getContent());
            }
        }

        return prompt.toString();
    }

    private JsonArray buildMessages(String systemPrompt, String userMessage, List<Message> history, Character userCharacter) {
        JsonArray messages = new JsonArray();

        if (!systemPrompt.isEmpty()) {
            messages.add(createMessage("system", systemPrompt));
        }

        for (Message msg : history) {
            String role = msg.getRole().equals(userCharacter.getId()) ? "user" : "assistant";
            messages.add(createMessage(role, msg.getContent()));
        }

        messages.add(createMessage("user", userMessage));

        return messages;
    }

    private JsonObject createMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private RequestBody createRequestBody(String modelName, JsonArray messages, int maxTokens, float temperature, float topP, float frequencyPenalty, String apiType) {
        JsonObject body = new JsonObject();
        body.addProperty("model", modelName);
        body.add("messages", messages);
        body.addProperty("max_tokens", maxTokens);
        body.addProperty("temperature", temperature);
        body.addProperty("top_p", topP);

        if (API_TYPE_OPENAI.equals(apiType)) {
            body.addProperty("frequency_penalty", frequencyPenalty);
        }

        if (API_TYPE_GOOGLE.equals(apiType)) {
        }

        return RequestBody.create(
                body.toString(),
                MediaType.parse("application/json")
        );
    }

    private Request createRequest(String url, String apiKey, RequestBody body) {
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private void executeRequest(Request request, String apiType, ApiResponseListener listener) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network failure: " + e.getMessage());
                listener.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        String aiResponse = extractContent(responseBody, apiType);
                        listener.onSuccess(aiResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        listener.onFailure("Error parsing response");
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + responseBody);
                    listener.onFailure("API error " + response.code() + ": " + getErrorMessage(responseBody));
                }
            }
        });
    }

    private String extractContent(String responseBody, String apiType) {
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);

        if (API_TYPE_GOOGLE.equals(apiType)) {
            // Google Gemini format
            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                return message.get("content").getAsString();
            }
        } else {
            // OpenAI format
            if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                return message.get("content").getAsString();
            }
        }

        throw new RuntimeException("No valid response found in API response");
    }

    private String getErrorMessage(String responseBody) {
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            if (json.has("error") && json.getAsJsonObject("error").has("message")) {
                return json.getAsJsonObject("error").get("message").getAsString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error message: " + e.getMessage());
        }
        return "Unknown error";
    }

    private boolean hasContent(String text) {
        return text != null && !text.trim().isEmpty();
    }
}