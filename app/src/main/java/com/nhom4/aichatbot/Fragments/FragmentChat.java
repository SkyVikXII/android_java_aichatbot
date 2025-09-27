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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatDbHelper = new ChatDbHelper(getContext());
        characterDbHelper = new CharacterDbHelper(getContext());
        chatList = new ArrayList<>();
        characterList = new ArrayList<>();
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
        fab.setOnClickListener(v -> showNewChatDialog());
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
}