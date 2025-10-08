package com.nhom4.aichatbot;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Message;
import com.nhom4.aichatbot.Models.Prompt;

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

    public void makeApiCall(String apiEndpoint, String apiKey, String modelName, int maxResponseToken, float temperature, float repetitionPenalty, float topP, String userMessage, List<Message> conversationHistory, Character userCharacter, Character aiCharacter, List<Prompt> prompts, ApiResponseListener listener) {
        try {
            // Determine API type based on endpoint
            String apiType = determineApiType(apiEndpoint);

            RequestBody body;
            Request request;

            switch (apiType) {
                case API_TYPE_GOOGLE:
                    body = createGoogleRequestBody(modelName, userMessage, conversationHistory, userCharacter, aiCharacter, prompts, maxResponseToken, temperature, topP);
                    request = createGoogleRequest(apiEndpoint, apiKey, body);
                    break;
                case API_TYPE_OPENAI:
                default:
                    body = createOpenAIRequestBody(modelName, userMessage, conversationHistory, userCharacter, aiCharacter, prompts, maxResponseToken, temperature, repetitionPenalty, topP);
                    request = createOpenAIRequest(apiEndpoint, apiKey, body);
                    break;
            }

            Log.d(TAG, "Request: " + request.toString());

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
                            String aiResponse = parseApiResponse(apiType, responseBody);
                            listener.onSuccess(aiResponse);
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

    private String determineApiType(String apiEndpoint) {
        if (apiEndpoint.contains("generativelanguage.googleapis.com") ||
                apiEndpoint.contains("googleapis.com")) {
            return API_TYPE_GOOGLE;
        }
        return API_TYPE_OPENAI;
    }

    private String buildCharacterPrompt(Character userCharacter, Character aiCharacter, List<Prompt> prompts) {
        StringBuilder finalPrompt = new StringBuilder();

        // Add system prompts (type = 1)
        for (Prompt prompt : prompts) {
            if (prompt.getType() == 1 && prompt.isActive()) {
                if (finalPrompt.length() > 0) {
                    finalPrompt.append("\n");
                }
                finalPrompt.append(prompt.getContent());
            }
        }

        // Add character information section
        if (userCharacter != null || aiCharacter != null) {
            if (finalPrompt.length() > 0) {
                finalPrompt.append("\n\n");
            }
            finalPrompt.append("<ROLEPLAY_INFO>\n");

            if (userCharacter != null && userCharacter.getDescription() != null && !userCharacter.getDescription().isEmpty()) {
                finalPrompt.append("[").append(userCharacter.getName()).append(" character information]\n");
                finalPrompt.append(userCharacter.getDescription()).append("\n");
            }

            if (aiCharacter != null && aiCharacter.getDescription() != null && !aiCharacter.getDescription().isEmpty()) {
                if (userCharacter != null && userCharacter.getDescription() != null && !userCharacter.getDescription().isEmpty()) {
                    finalPrompt.append("\n");
                }
                finalPrompt.append("[").append(aiCharacter.getName()).append(" character information]\n");
                finalPrompt.append(aiCharacter.getDescription()).append("\n");
            }

            finalPrompt.append("</ROLEPLAY_INFO>");
        }

        // Add response instructions section (type = 2)
        boolean hasResponseInstructions = false;
        StringBuilder responseInstructions = new StringBuilder();

        for (Prompt prompt : prompts) {
            if (prompt.getType() == 2 && prompt.isActive()) {
                if (responseInstructions.length() > 0) {
                    responseInstructions.append("\n");
                }
                responseInstructions.append(prompt.getContent());
                hasResponseInstructions = true;
            }
        }

        if (hasResponseInstructions) {
            if (finalPrompt.length() > 0) {
                finalPrompt.append("\n\n");
            }
            finalPrompt.append("<RESPONSE_INSTRUCTION>\n");
            finalPrompt.append(responseInstructions.toString());
            finalPrompt.append("\n</RESPONSE_INSTRUCTION>");
        }

        return finalPrompt.toString();
    }

    private RequestBody createOpenAIRequestBody(String modelName, String userMessage, List<Message> conversationHistory, Character userCharacter, Character aiCharacter, List<Prompt> prompts, int maxResponseToken, float temperature, float repetitionPenalty, float topP) {
        JsonArray messagesArray = new JsonArray();

        // Build the combined system prompt with character information
        String combinedSystemPrompt = buildCharacterPrompt(userCharacter, aiCharacter, prompts);
        if (!combinedSystemPrompt.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", combinedSystemPrompt);
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
        return RequestBody.create(requestBodyString, MediaType.parse("application/json"));
    }

    private RequestBody createGoogleRequestBody(String modelName, String userMessage, List<Message> conversationHistory, Character userCharacter, Character aiCharacter, List<Prompt> prompts, int maxResponseToken, float temperature, float topP) {
        JsonArray messagesArray = new JsonArray();

        // Build the combined system prompt with character information
        String combinedSystemPrompt = buildCharacterPrompt(userCharacter, aiCharacter, prompts);
        if (!combinedSystemPrompt.isEmpty()) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", combinedSystemPrompt);
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

        // Create the request body for Google Gemini
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);
        requestBody.add("messages", messagesArray);
        requestBody.addProperty("max_tokens", maxResponseToken);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("top_p", topP);
        // Note: Google Gemini doesn't support repetition_penalty in the same way

        String requestBodyString = requestBody.toString();
        return RequestBody.create(requestBodyString, MediaType.parse("application/json"));
    }

    private Request createOpenAIRequest(String apiEndpoint, String apiKey, RequestBody body) {
        return new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private Request createGoogleRequest(String apiEndpoint, String apiKey, RequestBody body) {
        return new Request.Builder()
                .url(apiEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private String parseApiResponse(String apiType, String responseBody) {
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

        switch (apiType) {
            case API_TYPE_GOOGLE:
                if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                    JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                    JsonObject message = choice.getAsJsonObject("message");
                    return message.get("content").getAsString();
                } else if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                    // Alternative Google response format
                    JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                    JsonObject content = candidate.getAsJsonObject("content");
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts.size() > 0) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
                break;

            case API_TYPE_OPENAI:
            default:
                if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                    JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                    JsonObject message = choice.getAsJsonObject("message");
                    return message.get("content").getAsString();
                }
                break;
        }

        throw new RuntimeException("No valid response found in API response");
    }
}