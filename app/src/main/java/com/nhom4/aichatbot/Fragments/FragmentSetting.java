package com.nhom4.aichatbot.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nhom4.aichatbot.Adapter.EndpointAdapter;
import com.nhom4.aichatbot.Adapter.ModelAdapter;
import com.nhom4.aichatbot.Adapter.PromptAdapter;
import com.nhom4.aichatbot.Database.EndpointDbHelper;
import com.nhom4.aichatbot.Database.ModelDbHelper;
import com.nhom4.aichatbot.Database.PromptDbHelper;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.Models.Prompt;
import com.nhom4.aichatbot.R;

import java.util.List;

public class FragmentSetting extends Fragment implements EndpointAdapter.OnEndpointClickListener, ModelAdapter.OnModelClickListener, PromptAdapter.OnPromptClickListener {

    private RecyclerView recyclerViewEndpoints, recyclerViewModels, recyclerViewPrompts;
    private EndpointAdapter endpointAdapter;
    private ModelAdapter modelAdapter;
    private PromptAdapter promptAdapter;
    private EndpointDbHelper endpointDbHelper;
    private ModelDbHelper modelDbHelper;
    private PromptDbHelper promptDbHelper;
    private DatabaseReference firebaseEndpointsRef, firebaseModelsRef, firebasePromptsRef, firebaseSystemEndpointsRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpointDbHelper = new EndpointDbHelper(getContext());
        modelDbHelper = new ModelDbHelper(getContext());
        promptDbHelper = new PromptDbHelper(getContext());

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        firebaseEndpointsRef = userRef.child("endpoints");
        firebaseModelsRef = userRef.child("models");
        firebasePromptsRef = userRef.child("prompts");

        firebaseSystemEndpointsRef = FirebaseDatabase.getInstance().getReference().child("system").child("endpoint");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAllViews(view);
        loadAllDataFromSqlite();
        syncSystemEndpoints();
    }

    private void syncSystemEndpoints() {
        firebaseSystemEndpointsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Endpoint endpoint = snapshot.getValue(Endpoint.class);
                    if (endpoint != null) {
                        String systemId = "system_" + snapshot.getKey();
                        endpoint.setId(systemId);
                        if (endpointDbHelper.getEndpointById(systemId) == null) {
                            endpointDbHelper.addEndpoint(endpoint, true);
                        } else {
                            endpointDbHelper.updateEndpoint(endpoint, true);
                        }
                    }
                }
                loadEndpoints();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load system endpoints.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAllViews(View view) {
        // Endpoints
        recyclerViewEndpoints = view.findViewById(R.id.recyclerView_SettingEndpoint);
        recyclerViewEndpoints.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.buttonSetting_addEndpoint).setOnClickListener(v -> showAddEditEndpointDialog(null));

        // Models
        recyclerViewModels = view.findViewById(R.id.recyclerView_SettingModel);
        recyclerViewModels.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.buttonSetting_addModel).setOnClickListener(v -> showAddEditModelDialog(null));

        // Prompts
        recyclerViewPrompts = view.findViewById(R.id.recyclerView_SettingPrompt);
        recyclerViewPrompts.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.buttonSetting_addPrompt).setOnClickListener(v -> showAddEditPromptDialog(null));
    }

    private void loadAllDataFromSqlite() {
        loadEndpoints();
        loadModels();
        loadPrompts();
    }

    // Endpoint Management
    private void loadEndpoints() {
        List<Endpoint> endpoints = endpointDbHelper.getAllEndpoints();
        endpointAdapter = new EndpointAdapter(getContext(), endpoints, this);
        recyclerViewEndpoints.setAdapter(endpointAdapter);
    }

    private void showAddEditEndpointDialog(final Endpoint endpoint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_endpoint, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText name = dialogView.findViewById(R.id.editTextEndpointName);
        EditText url = dialogView.findViewById(R.id.editTextEndpointUrl);
        EditText apiKey = dialogView.findViewById(R.id.editTextApiKey);
        Button cancel = dialogView.findViewById(R.id.buttonCancelEndpoint);
        Button save = dialogView.findViewById(R.id.buttonSaveEndpoint);

        boolean isSystemEndpoint = endpoint != null && endpoint.getId() != null && endpoint.getId().startsWith("system_");

        if (endpoint != null) {
            name.setText(endpoint.getName());
            url.setText(endpoint.getEndpoint_url());
            apiKey.setText(endpoint.getAPI_KEY());

            if (isSystemEndpoint) {
                name.setEnabled(false);
                url.setEnabled(false);
                apiKey.setEnabled(false);
                save.setVisibility(View.GONE);
            }
        }

        cancel.setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(v -> {
            String endpointName = name.getText().toString().trim();
            String endpointUrl = url.getText().toString().trim();
            String endpointApiKey = apiKey.getText().toString().trim();

            if (endpointName.isEmpty() || endpointUrl.isEmpty()) {
                Toast.makeText(getContext(), "Name and URL cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endpoint == null) {
                Endpoint newEndpoint = new Endpoint(endpointName, endpointUrl, endpointApiKey);
                newEndpoint.setId(generateId("endpoint"));
                endpointDbHelper.addEndpoint(newEndpoint, true);
                firebaseEndpointsRef.child(newEndpoint.getId()).setValue(newEndpoint);
            } else {
                endpoint.setName(endpointName);
                endpoint.setEndpoint_url(endpointUrl);
                endpoint.setAPI_KEY(endpointApiKey);
                endpointDbHelper.updateEndpoint(endpoint, true);
                firebaseEndpointsRef.child(endpoint.getId()).setValue(endpoint);
            }
            loadEndpoints();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onEditClick(Endpoint endpoint) { 
        // System endpoints are read-only, but we can allow viewing them.
        showAddEditEndpointDialog(endpoint); 
    }

    @Override
    public void onDeleteClick(Endpoint endpoint) {
        if (endpoint.getId() != null && endpoint.getId().startsWith("system_")) {
            Toast.makeText(getContext(), "System defaults cannot be deleted.", Toast.LENGTH_SHORT).show();
            return;
        }
        endpointDbHelper.deleteEndpoint(endpoint.getId());
        firebaseEndpointsRef.child(endpoint.getId()).removeValue();
        loadEndpoints();
    }

    // Model Management
    private void loadModels() {
        List<Model> models = modelDbHelper.getAllModels();
        modelAdapter = new ModelAdapter(getContext(), models, this);
        recyclerViewModels.setAdapter(modelAdapter);
    }

    private void showAddEditModelDialog(final Model model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_model, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText name = dialogView.findViewById(R.id.editTextModelName);
        EditText description = dialogView.findViewById(R.id.editTextModelDescription);
        EditText contextLength = dialogView.findViewById(R.id.editTextContextLength);
        EditText maxTokens = dialogView.findViewById(R.id.editTextMaxTokens);
        EditText temperature = dialogView.findViewById(R.id.editTextTemperature);
        EditText topP = dialogView.findViewById(R.id.editTextTopP);
        EditText freqPenalty = dialogView.findViewById(R.id.editTextFrequencyPenalty);
        EditText presPenalty = dialogView.findViewById(R.id.editTextPresencePenalty);
        Button saveButton = dialogView.findViewById(R.id.buttonSaveModel);

        if (model != null) {
            name.setText(model.getName());
            description.setText(model.getDescription());
            contextLength.setText(model.getContext_length());
            maxTokens.setText(model.getMax_tokens());
            temperature.setText(model.getTemperature());
            topP.setText(model.getTop_p());
            freqPenalty.setText(model.getFrequency_penalty());
            presPenalty.setText(model.getPresence_penalty());

            if (model.isDefault()) {
                name.setEnabled(false);
                description.setEnabled(false);
                contextLength.setEnabled(false);
                maxTokens.setEnabled(false);
                temperature.setEnabled(false);
                topP.setEnabled(false);
                freqPenalty.setEnabled(false);
                presPenalty.setEnabled(false);
                saveButton.setVisibility(View.GONE);
            }
        } else {
            name.setText("My Model");
            contextLength.setText("4096");
            maxTokens.setText("1024");
            temperature.setText("1.0");
            topP.setText("1.0");
            freqPenalty.setText("0.0");
            presPenalty.setText("0.0");
        }

        dialogView.findViewById(R.id.buttonCancelModel).setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> {
            String modelName = name.getText().toString().trim();
            if (modelName.isEmpty()) {
                Toast.makeText(getContext(), "Model name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Model modelToSave = (model == null) ? new Model() : model;
            if (model == null) {
                modelToSave.setId(generateId("model"));
                modelToSave.setDefault(false);
            }
            modelToSave.setName(modelName);
            modelToSave.setDescription(description.getText().toString().trim());
            modelToSave.setContext_length(contextLength.getText().toString().trim());
            modelToSave.setMax_tokens(maxTokens.getText().toString().trim());
            modelToSave.setTemperature(temperature.getText().toString().trim());
            modelToSave.setTop_p(topP.getText().toString().trim());
            modelToSave.setFrequency_penalty(freqPenalty.getText().toString().trim());
            modelToSave.setPresence_penalty(presPenalty.getText().toString().trim());

            if (model == null) {
                modelDbHelper.addModel(modelToSave, true);
            } else {
                modelDbHelper.updateModel(modelToSave, true);
            }
            firebaseModelsRef.child(modelToSave.getId()).setValue(modelToSave);
            loadModels();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onEditClick(Model model) { showAddEditModelDialog(model); }

    @Override
    public void onDeleteClick(Model model) {
        if (model.isDefault()) {
            Toast.makeText(getContext(), "Default models cannot be deleted.", Toast.LENGTH_SHORT).show();
            return;
        }
        modelDbHelper.deleteModel(model.getId());
        firebaseModelsRef.child(model.getId()).removeValue();
        loadModels();
    }

    // Prompt Management
    private void loadPrompts() {
        List<Prompt> prompts = promptDbHelper.getAllPrompts();
        promptAdapter = new PromptAdapter(getContext(), prompts, this);
        recyclerViewPrompts.setAdapter(promptAdapter);
    }

    private void showAddEditPromptDialog(final Prompt prompt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_prompt, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText name = dialogView.findViewById(R.id.editTextPromptName);
        EditText content = dialogView.findViewById(R.id.editTextPromptContent);
        RadioGroup typeGroup = dialogView.findViewById(R.id.radioGroupPromptType);
        Button saveButton = dialogView.findViewById(R.id.buttonSavePrompt);

        if (prompt != null) {
            name.setText(prompt.getName());
            content.setText(prompt.getContent());
            if (prompt.getType() == 1) {
                typeGroup.check(R.id.radioButtonSystem);
            } else {
                typeGroup.check(R.id.radioButtonInjection);
            }

            if (prompt.isDefault()) {
                name.setEnabled(false);
                content.setEnabled(false);
                for (int i = 0; i < typeGroup.getChildCount(); i++) {
                    typeGroup.getChildAt(i).setEnabled(false);
                }
                saveButton.setVisibility(View.GONE);
            }
        }

        dialogView.findViewById(R.id.buttonCancelPrompt).setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> {
            String promptName = name.getText().toString().trim();
            if (promptName.isEmpty()) {
                Toast.makeText(getContext(), "Prompt name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTypeId = typeGroup.getCheckedRadioButtonId();
            int type = (selectedTypeId == R.id.radioButtonSystem) ? 1 : 2;

            Prompt promptToSave = (prompt == null) ? new Prompt() : prompt;
            if (prompt == null) {
                promptToSave.setId(generateId("prompt"));
                promptToSave.setDefault(false);
            }
            promptToSave.setName(promptName);
            promptToSave.setContent(content.getText().toString().trim());
            promptToSave.setType(type);

            if (prompt == null) {
                promptDbHelper.addPrompt(promptToSave, true);
            }
            else {
                promptDbHelper.updatePrompt(promptToSave, true);
            }
            firebasePromptsRef.child(promptToSave.getId()).setValue(promptToSave);
            loadPrompts();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onEditClick(Prompt prompt) { showAddEditPromptDialog(prompt); }

    @Override
    public void onDeleteClick(Prompt prompt) {
        if (prompt.isDefault()) {
            Toast.makeText(getContext(), "Default prompts cannot be deleted.", Toast.LENGTH_SHORT).show();
            return;
        }
        promptDbHelper.deletePrompt(prompt.getId());
        firebasePromptsRef.child(prompt.getId()).removeValue();
        loadPrompts();
    }

    @Override
    public void onActivateClick(Endpoint endpoint, boolean isActive) {
        if (isActive) {
            endpointDbHelper.setEndpointActive(endpoint.getId());
            loadEndpoints(); // Reload to update all switches
        }
    }

    @Override
    public void onActivateClick(Model model, boolean isActive) {
        if (isActive) {
            modelDbHelper.setModelActive(model.getId());
            loadModels();
        }
    }

    @Override
    public void onActivateClick(Prompt prompt, boolean isActive) {
        promptDbHelper.setPromptActive(prompt.getId(), isActive);
        loadPrompts();
    }

    private String generateId(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}