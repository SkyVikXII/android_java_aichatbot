package com.nhom4.aichatbot;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nhom4.aichatbot.Adapter.MessageAdapter;
import com.nhom4.aichatbot.Database.ChatDbHelper;
import com.nhom4.aichatbot.Database.EndpointDbHelper;
import com.nhom4.aichatbot.Database.ModelDbHelper;
import com.nhom4.aichatbot.Database.PromptDbHelper;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.Models.Endpoint;
import com.nhom4.aichatbot.Models.Message;
import com.nhom4.aichatbot.Models.Model;
import com.nhom4.aichatbot.Models.Prompt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements ApiCall.ApiResponseListener {

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private Toolbar toolbar;

    private ChatDbHelper chatDbHelper;
    private EndpointDbHelper endpointDbHelper;
    private ModelDbHelper modelDbHelper;
    private PromptDbHelper promptDbHelper;
    private ApiCall apiCall;

    private Chat currentChat;
    private DatabaseReference firebaseChatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String chatId = getIntent().getStringExtra("CHAT_ID");
        if (chatId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID cuộc trò chuyện.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        initHelpers();
        setupToolbar();
        List<Chat> allChats = chatDbHelper.getAllChats();
        for(Chat chat : allChats) {
            if(chat.getId().equals(chatId)) {
                currentChat = chat;
                break;
            }
        }

        if (currentChat == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy cuộc trò chuyện trong cơ sở dữ liệu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseChatRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats").child(currentChat.getId());

        setTitle(currentChat.getName());
        setupRecyclerView();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarChat);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
    }

    private void initHelpers() {
        chatDbHelper = new ChatDbHelper(this);
        endpointDbHelper = new EndpointDbHelper(this);
        modelDbHelper = new ModelDbHelper(this);
        promptDbHelper = new PromptDbHelper(this);
        apiCall = new ApiCall();
    }

    private void updateChatInFirebase() {
        if (firebaseChatRef != null && currentChat != null) {
            firebaseChatRef.setValue(currentChat).addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Đồng bộ hóa cuộc trò chuyện lên Firebase thất bại", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_reset_chat) {
            resetChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetChat() {
        if (currentChat != null) {
            currentChat.setMessages(new ArrayList<>());
            chatDbHelper.updateChat(currentChat);
            updateChatInFirebase();
            setupRecyclerView();
            messageAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đặt lại cuộc trò chuyện thành công.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentChat.getMessages(), currentChat.getCharacterUser().getId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        int messageSize = currentChat.getMessages().size();
        if(messageText.isEmpty()&&currentChat.getMessages()!=null && messageSize!=0){
            if(currentChat.getMessages().get(messageSize-1).getRole().equals(currentChat.getCharacterUser().getId())){
                messageText = currentChat.getMessages().get(messageSize -1).getContent();
            }
        }else if (messageText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn trước khi gửi.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Message userMessage = new Message(UUID.randomUUID().toString(), new Date(), currentChat.getCharacterUser().getId(), messageText);
            currentChat.getMessages().add(userMessage);
            messageAdapter.notifyItemInserted(currentChat.getMessages().size() - 1);
            recyclerViewMessages.scrollToPosition(currentChat.getMessages().size() - 1);
            editTextMessage.setText("");
            chatDbHelper.updateChat(currentChat);
            updateChatInFirebase();
        }
        getAiResponse(messageText);
    }

    private void getAiResponse(String userMessageText) {
        // Gather all active settings for the API call
        Endpoint activeEndpoint = getActiveEndpoint();
        Model activeModel = getActiveModel();
        List<Prompt> activeSystemPrompts = getActiveSystemPrompts();

        if (activeEndpoint == null || activeModel == null) {
            onFailure("Lỗi: Không tìm thấy Endpoint hoặc Model đang hoạt động trong cài đặt.");
            return;
        }
        buttonSend.setClickable(false);
        apiCall.makeApiCall(
                activeEndpoint,
                activeModel,
                userMessageText,
                currentChat.getMessages(),
                currentChat.getCharacterUser(),
                currentChat.getCharacterAI(),
                activeSystemPrompts,
                this
        );
    }

    @Override
    public void onSuccess(String response) {
        runOnUiThread(() -> {
            Message aiMessage = new Message(UUID.randomUUID().toString(), new Date(), currentChat.getCharacterAI().getId(), response);
            currentChat.getMessages().add(aiMessage);
            chatDbHelper.updateChat(currentChat);
            updateChatInFirebase();
            messageAdapter.notifyItemInserted(currentChat.getMessages().size() - 1);
            recyclerViewMessages.scrollToPosition(currentChat.getMessages().size() - 1);
            buttonSend.setClickable(true);
        });
    }

    @Override
    public void onFailure(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            buttonSend.setClickable(true);
        });
    }

    // Helper methods to get active settings
    private Endpoint getActiveEndpoint() {
        for (Endpoint e : endpointDbHelper.getAllEndpoints()) {
            if (e.isActive()) return e;
        }
        return null;
    }

    private Model getActiveModel() {
        for (Model m : modelDbHelper.getAllModels()) {
            if (m.isActive()) return m;
        }
        return null;
    }

    private List<Prompt> getActiveSystemPrompts() {
        List<Prompt> prompts = new ArrayList<>();
        for (Prompt p : promptDbHelper.getAllPrompts()) {
            if (p.isActive() && (p.getType() == 1||p.getType() == 2)) {
                // Type 1 = System, 2 inject
                prompts.add(p);
            }
        }
        return prompts;
    }
}
