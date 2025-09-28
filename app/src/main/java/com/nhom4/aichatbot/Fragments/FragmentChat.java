package com.nhom4.aichatbot.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Adapter.ChatAdapter;
import com.nhom4.aichatbot.ChatActivity;
import com.nhom4.aichatbot.Database.CharacterDbHelper;
import com.nhom4.aichatbot.Database.ChatDbHelper;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Chat;
import com.nhom4.aichatbot.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FragmentChat extends Fragment implements ChatAdapter.OnChatClickListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ChatDbHelper chatDbHelper;
    private CharacterDbHelper characterDbHelper;
    private List<Chat> chatList;
    private List<Character> characterList;
    private DatabaseReference firebaseRef;
    private boolean isOnline = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatDbHelper = new ChatDbHelper(getContext());
        characterDbHelper = new CharacterDbHelper(getContext());
        chatList = new ArrayList<>();
        characterList = new ArrayList<>();
        isOnline = isNetworkAvailable();
        if (isOnline) {
            setupFirebase();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setupFirebase() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("chats");
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        // Check if chat already exists in SQLite
                        boolean chatExists = false;
                        for (Chat existingChat : chatDbHelper.getAllChats()) {
                            if (existingChat.getId().equals(chat.getId())) {
                                chatExists = true;
                                break;
                            }
                        }
                        if (!chatExists) {
                            chatDbHelper.addChat(chat);
                        } else {
                            chatDbHelper.updateChat(chat);
                        }
                    }
                }
                loadChatsFromDb();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to sync chats from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
        syncUnsyncedData();
    }

    private void syncUnsyncedData() {
        // No specific unsynced flag for chats, so we'll just push all local chats to Firebase
        // This might need refinement if a proper sync mechanism is implemented
        for (Chat chat : chatDbHelper.getAllChats()) {
            firebaseRef.child(chat.getId()).setValue(chat)
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to sync chat " + chat.getName() + " to Firebase", Toast.LENGTH_SHORT).show());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fabNewChat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewChatDialog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatsFromDb();
    }

    private void loadChatsFromDb() {
        chatList = chatDbHelper.getAllChats();
        adapter = new ChatAdapter(chatList, this);
        recyclerView.setAdapter(adapter);
    }

    private void showNewChatDialog() {
        characterList = characterDbHelper.getAllCharacters();
        if (characterList.size() < 2) {
            Toast.makeText(getContext(), "You need at least two characters to start a roleplay chat.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_chat, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.editTextNewChatName);
        EditText etDescription = dialogView.findViewById(R.id.editTextNewChatDescription);
        Spinner spinnerUser = dialogView.findViewById(R.id.spinnerUserCharacter);
        Spinner spinnerAi = dialogView.findViewById(R.id.spinnerAiCharacter);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancelNewChat);
        Button btnCreate = dialogView.findViewById(R.id.buttonCreateNewChat);

        List<String> characterNames = new ArrayList<>();
        for (Character character : characterList) {
            characterNames.add(character.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, characterNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUser.setAdapter(spinnerAdapter);
        spinnerAi.setAdapter(spinnerAdapter);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            int userCharPos = spinnerUser.getSelectedItemPosition();
            int aiCharPos = spinnerAi.getSelectedItemPosition();

            if (userCharPos == aiCharPos) {
                Toast.makeText(getContext(), "Please select two different characters.", Toast.LENGTH_SHORT).show();
                return;
            }
            String chatName = etName.getText().toString().trim();
            if (chatName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a chat name.", Toast.LENGTH_SHORT).show();
                return;
            }

            Character userChar = characterList.get(userCharPos);
            Character aiChar = characterList.get(aiCharPos);

            Chat newChat = new Chat();
            newChat.setId(UUID.randomUUID().toString());
            newChat.setName(chatName);
            newChat.setDescription(etDescription.getText().toString().trim());
            newChat.setCharacterUser(userChar); // Copied data
            newChat.setCharacterAI(aiChar);     // Copied data
            newChat.setMessages(new ArrayList<>()); // Start with empty message list

            chatDbHelper.addChat(newChat);
            if (isOnline && firebaseRef != null) {
                firebaseRef.child(newChat.getId()).setValue(newChat)
                        .addOnSuccessListener(aVoid -> {
                            // Optionally, you can add a success message or log here
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save chat to Firebase", Toast.LENGTH_SHORT).show());
            }
            loadChatsFromDb();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onChatClick(Chat chat) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("CHAT_ID", chat.getId());
        startActivity(intent);
    }

    @Override
    public void onLongChatClick(Chat chat) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this chat?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    chatDbHelper.deleteChat(chat);
                    if (isOnline && firebaseRef != null) {
                        firebaseRef.child(chat.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Chat deleted from Firebase", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete chat from Firebase", Toast.LENGTH_SHORT).show());
                    }
                    loadChatsFromDb();
                    Toast.makeText(getContext(), "Chat deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}