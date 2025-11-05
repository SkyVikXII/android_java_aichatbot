package com.nhom4.aichatbot.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Adapter.EndpointAdapter;
import com.nhom4.aichatbot.Adapter.ModelAdapter;
import com.nhom4.aichatbot.Adapter.PromptAdapter;
import com.nhom4.aichatbot.Database.EndpointDbHelper;
import com.nhom4.aichatbot.Database.ModelDbHelper;
import com.nhom4.aichatbot.Database.PromptDbHelper;
import com.nhom4.aichatbot.Firebase.EndpointFirebaseHelper;
import com.nhom4.aichatbot.Firebase.ModelFirebaseHelper;
import com.nhom4.aichatbot.Firebase.PromptFirebaseHelper;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.Models.Prompt;
import com.nhom4.aichatbot.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentSetting extends Fragment implements EndpointAdapter.OnEndpointClickListener, ModelAdapter.OnModelClickListener, PromptAdapter.OnPromptClickListener {

    private RecyclerView recyclerViewEndpoints, recyclerViewModels, recyclerViewPrompts;
    private EndpointAdapter endpointAdapter;
    private ModelAdapter modelAdapter;
    private PromptAdapter promptAdapter;

    private ModelFirebaseHelper modelFirebaseHelper;
    private EndpointFirebaseHelper endpointFirebaseHelper;
    private PromptFirebaseHelper promptFirebaseHelper;

    private DatabaseReference firebaseEndpointsRef, firebaseModelsRef, firebasePromptsRef, firebaseSystemEndpointsRef;
    int activeEndpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endpointFirebaseHelper = new EndpointFirebaseHelper(getContext());
        modelFirebaseHelper = new ModelFirebaseHelper(getContext());
        promptFirebaseHelper = new PromptFirebaseHelper(getContext());

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
        syncAllData();
        //syncSystemEndpoints();
        //syncFirebaseEndpointdata();
        //syncFirebaseModeldata();
        //syncFirebasePromptdata();
    }
    private void syncAllData(){
        modelFirebaseHelper.syncUserModels( new ModelFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
                loadModels();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        endpointFirebaseHelper.syncSystemEndpoints( new EndpointFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
                loadEndpoints();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        endpointFirebaseHelper.syncUserEndpoints( new EndpointFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
                loadEndpoints();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
        promptFirebaseHelper.syncUserPrompts( new PromptFirebaseHelper.SyncCallback() {
            @Override
            public void onSuccess() {
                loadPrompts();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Không thể tải du lieu tu server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncSystemEndpoints() {
        firebaseSystemEndpointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Endpoint endpoint = snapshot.getValue(Endpoint.class);
                    if (endpoint != null) {
                        String systemId = "system_"+snapshot.getKey();
                        endpoint.setId(systemId);
                        if (endpointFirebaseHelper.sqlite.getEndpointById(systemId) == null) {
                            endpointFirebaseHelper.sqlite.addEndpoint(endpoint, true);
                        } else {
                            if(endpointFirebaseHelper.sqlite.getEndpointById(systemId).isActive()){
                                endpoint.setActive(true);
                            }else{
                                endpoint.setActive(false);
                            }
                            endpointFirebaseHelper.sqlite.updateEndpoint(endpoint, true);
                        }
                    }
                }
                loadEndpoints();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Không thể tải các điểm cuối của hệ thống.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void syncFirebaseEndpointdata() {
        firebaseEndpointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Endpoint endpoint = snapshot.getValue(Endpoint.class);
                    if (endpoint != null) {
                        String Id = snapshot.getKey();
                        endpoint.setId(Id);
                        if (endpointFirebaseHelper.sqlite.getEndpointById(Id) == null) {
                            endpointFirebaseHelper.sqlite.addEndpoint(endpoint, true);
                        } else {
                            if(endpointFirebaseHelper.sqlite.getEndpointById(Id).isActive()){
                                activeEndpoint++;
                                if(activeEndpoint>1){
                                    endpoint.setActive(false);
                                }else{
                                    endpoint.setActive(true);
                                }
                            }else{
                                endpoint.setActive(false);
                            }
                            endpointFirebaseHelper.sqlite.updateEndpoint(endpoint, true);
                            firebaseEndpointsRef.child(endpoint.getId()).setValue(endpoint);
                        }
                    }
                }
                loadEndpoints();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Không thể tải dữ liệu từ Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void syncFirebaseModeldata() {
        firebaseModelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Model model = snapshot.getValue(Model.class);
                    if (model != null) {
                        String Id = snapshot.getKey();
                        model.setId(Id);
                        if (modelFirebaseHelper.sqlite.getModelById(Id) == null) {
                            modelFirebaseHelper.sqlite.addModel(model, true);
                        } else {
                            if(modelFirebaseHelper.sqlite.getModelById(Id).isActive()){
                                model.setActive(true);
                                count++;
                                if(count>1){
                                    model.setActive(false);
                                }
                            }else{
                                model.setActive(false);
                            }
                            modelFirebaseHelper.sqlite.updateModel(model, true);
                            firebaseModelsRef.child(model.getId()).setValue(model);
                        }
                    }
                }
                loadModels();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Không thể tải dữ liệu mô hình từ Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void syncFirebasePromptdata() {
        firebasePromptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Prompt prompt = snapshot.getValue(Prompt.class);
                    if (prompt != null) {
                        String Id = snapshot.getKey();
                        prompt.setId(Id);
                        if (promptFirebaseHelper.sqlite.getPromptById(Id) == null) {
                            promptFirebaseHelper.sqlite.addPrompt(prompt, true);
                        } else {
                            if(promptFirebaseHelper.sqlite.getPromptById(Id).isActive()){
                                prompt.setActive(true);
                            }else{
                                prompt.setActive(false);
                            }
                            promptFirebaseHelper.sqlite.updatePrompt(prompt, true);
                            firebasePromptsRef.child(prompt.getId()).setValue(prompt);
                        }
                    }
                }
                loadPrompts();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError databaseError) {
                Toast.makeText(getContext(), "Không thể tải các lệnh hệ thống từ Firebase", Toast.LENGTH_SHORT).show();
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

        //help dialog
        View buttonhelp_endpoint = view.findViewById(R.id.button_SettingHelp_Endpoint);
        buttonhelp_endpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpEndpointDialog();
            }
        });
        View buttonhelp_model = view.findViewById(R.id.button_SettingHelp_Model);
        buttonhelp_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpModelDialog();
            }
        });
        View buttonhelp_prompt = view.findViewById(R.id.button_SettingHelp_Prompt);
        buttonhelp_prompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpPromptDialog();
            }
        });
    }

    private void loadAllDataFromSqlite() {
        loadEndpoints();
        loadModels();
        loadPrompts();
    }

    // Endpoint Management
    private void loadEndpoints() {
        List<Endpoint> endpoints = endpointFirebaseHelper.sqlite.getAllEndpoints();
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
                Toast.makeText(getContext(), "Tên và URL không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endpoint == null) {
                Endpoint newEndpoint = new Endpoint(endpointName, endpointUrl, endpointApiKey);
                newEndpoint.setId(generateId("endpoint"));
                endpointFirebaseHelper.sqlite.addEndpoint(newEndpoint, true);
                firebaseEndpointsRef.child(newEndpoint.getId()).setValue(newEndpoint);
            } else {
                endpoint.setName(endpointName);
                endpoint.setEndpoint_url(endpointUrl);
                endpoint.setAPI_KEY(endpointApiKey);
                endpointFirebaseHelper.sqlite.updateEndpoint(endpoint, true);
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
            Toast.makeText(getContext(), "Không thể xóa các mặc định của hệ thống.", Toast.LENGTH_SHORT).show();
            return;
        }
        endpointFirebaseHelper.sqlite.deleteEndpoint(endpoint.getId());
        firebaseEndpointsRef.child(endpoint.getId()).removeValue();
        loadEndpoints();
    }

    // Model Management
    private void loadModels() {
        List<Model> models = modelFirebaseHelper.sqlite.getAllModels();
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
            name.setText("");
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
                Toast.makeText(getContext(), "Tên model không được để trống", Toast.LENGTH_SHORT).show();
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
                modelFirebaseHelper.sqlite.addModel(modelToSave, true);
            } else {
                modelFirebaseHelper.sqlite.updateModel(modelToSave, true);
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
            Toast.makeText(getContext(), "Không thể xóa các model mặc định.", Toast.LENGTH_SHORT).show();
            return;
        }
        modelFirebaseHelper.sqlite.deleteModel(model.getId());
        firebaseModelsRef.child(model.getId()).removeValue();
        loadModels();
    }

    // Prompt Management
    private void loadPrompts() {
        List<Prompt> prompts = promptFirebaseHelper.sqlite.getAllPrompts();
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
                Toast.makeText(getContext(), "Tên prompt không được để trống", Toast.LENGTH_SHORT).show();
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
                promptFirebaseHelper.sqlite.addPrompt(promptToSave, true);
            }
            else {
                promptFirebaseHelper.sqlite.updatePrompt(promptToSave, true);
            }
            firebasePromptsRef.child(promptToSave.getId()).setValue(promptToSave);
            loadPrompts();
            dialog.dismiss();
        });
        dialog.show();
    }
    private void showHelpEndpointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help_endpoint, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView name = dialogView.findViewById(R.id.textview_dialog_help_endpoint);
        name.append("Endpoint là 1 địa chỉ để kết nối đến 1 nhà cung cấp dịch vu mô hình ngôn ngữ lớn (LLM), bạn phải có API để kết nối đến máy chủ\n");
        name.append("Ứng dụng hỗ trợ 2 end point phòng trường hợp bảo trì hoặc vấn đề đột xuất phát sinh\n");
        name.append("Bạn có thể dùng open router cùng với DeepSeek: DeepSeek V3 0324(free) và OpenAI: gpt-oss-20b (free)\n");
        name.append("Bạn có thể dùng google và Google: gemini 2.0 flash\n");
        name.append("Không dùng Open Router với Google: gemini 2.0 flash vì Open Router không có model với mã định danh đó\n");
        View cancel = dialogView.findViewById(R.id.button_dialog_help_endpoint_close);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void showHelpModelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help_model, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView name = dialogView.findViewById(R.id.textview_dialog_help_model);
        name.append("Cơ bản về các thông số kỹ thuật của model\n");
        name.append("Tên Mô Hình (Model) phần này người dùng tự đặt\n");
        name.append("Mô hình Id: mã định danh của mô hình, mỗi mô hình có mã định nhanh khác nhau và phải khớp với mã định danh được quy định bởi nhà cung cấp dịch vụ (bắc buộc)\n");
        name.append("Mô tả, Phần này không bắt buộc\n");
        name.append("Các Thông Số\n");
        name.append("Context Length: Giới hạn số token mà Mô hình có thể xử lý càng nhiều đồng nghĩa với việc mô hình có thể chứa thông tin lớn, xem giới hạn context được quy định ở nhà cung cấp dịch vụ của mô hình đó, tốt nhất giá trị phải nhỏ hơn thông số của nhà cung cấp dịch vụ đề ra\n");
        name.append("Max Tokens: Độ dài mà mô hình có thể phản hồi, ví dụ giá trị lớn mô hình có thể phản hồi như 1 đoạn văn, giá trị nhỏ mô hình chỉ có thể trả lời ngắn, nhưng tùy thuộc vào Lệnh hệ thống (system prompt), Không thể yêu cầu AI phản hồi bằng đoạn văn dài với token thấp được\n");
        name.append("Token: AI không hiểu ngôn ngữ của con người, tất cả chữ đều được dịch sang token, tùy thuộc vào loại chữ mà có độ dài token khác nhau\n");
        name.append("Các Thông Số Dưới này bạn có thể không cần chỉnh\n");
        name.append("Temperature (Nhiệt độ)\n 0.0 đến 2.0 Temperature kiểm soát mức độ ngẫu nhiên/sáng tạo trong câu trả lời\n");
        name.append("Top P (Nuclear Sampling)\n 0.0 đến 1.0 Top P giới hạn tập hợp từ khả thi dựa trên tích lũy xác suất\n");
        name.append("Frequency Penalty (Phạt tần suất)\n -2.0 đến 2.0 Giảm xác suất của các từ xuất hiện thường xuyên trong văn bản\n");
        name.append("Presence Penalty (Phạt sự hiện diện)\n -2.0 đến 2.0 Giảm xác suất của các từ đã xuất hiện trong văn bản\n");
        name.append("Google không hỗ trợ Frequency Penalty (Phạt tần suất) và Presence Penalty (Phạt sự hiện diện)");
        View cancel = dialogView.findViewById(R.id.button_dialog_help_model_close);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void showHelpPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_help_prompt, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView name = dialogView.findViewById(R.id.textview_dialog_help_prompt);
        name.append("Lệnh Hệ Thống (System Prompt)\n");
        name.append("Nơi mà bạn có thể yêu cầu những gì AI nên làm hoặc không được làm\n");
        name.append("Bạn có thể yêu cầu phản hồi ngắn, hoặc yêu cầu phản hồi bằng những ngôn ngữ khác nhau\n");
        name.append("Hoặc là giả lập 1 thế giới bằng cách đưa vào những ngữ cảnh khác nhau\n");
        name.append("Bạn có thể đặt tình huống là buổi phỏng vấn và AI sẽ là người phỏng vấn bạn\n");
        name.append("Sự sáng tạo là vô hạn, \n");
        View cancel = dialogView.findViewById(R.id.button_dialog_help_prompt_close);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    @Override
    public void onEditClick(Prompt prompt) { showAddEditPromptDialog(prompt); }

    @Override
    public void onDeleteClick(Prompt prompt) {
        if (prompt.isDefault()) {
            Toast.makeText(getContext(), "Không thể xóa các prompt mặc định.", Toast.LENGTH_SHORT).show();
            return;
        }
        promptFirebaseHelper.sqlite.deletePrompt(prompt.getId());
        firebasePromptsRef.child(prompt.getId()).removeValue();
        loadPrompts();
    }

    @Override
    public void onActivateClick(Endpoint endpoint, boolean isActive) {
        if (isActive) {
            endpointFirebaseHelper.sqlite.setEndpointActive(endpoint.getId());
            loadEndpoints(); // Reload to update all switches
        }
    }

    @Override
    public void onActivateClick(Model model, boolean isActive) {
        if (isActive) {
            modelFirebaseHelper.sqlite.setModelActive(model.getId());
            loadModels();
        }
    }

    @Override
    public void onActivateClick(Prompt prompt, boolean isActive) {
        promptFirebaseHelper.sqlite.setPromptActive(prompt.getId(), isActive);
        loadPrompts();
    }

    private String generateId(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }
}