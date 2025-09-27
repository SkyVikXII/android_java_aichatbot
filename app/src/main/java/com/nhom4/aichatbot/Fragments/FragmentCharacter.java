package com.nhom4.aichatbot.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Adapter.CharacterAdapter;
import com.nhom4.aichatbot.Database.CharacterDbHelper;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentCharacter extends Fragment implements CharacterAdapter.OnCharacterClickListener {

    private RecyclerView recyclerView;
    private CharacterAdapter adapter;
    private CharacterDbHelper dbHelper;
    private DatabaseReference firebaseRef;
    private boolean isOnline = false;

    public FragmentCharacter() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new CharacterDbHelper(getContext());
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
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("characters");
        // Sync from Firebase to SQLite
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Character character = snapshot.getValue(Character.class);
                    if (character != null) {
                        if (dbHelper.getCharacterById(character.getId()) == null) {
                            dbHelper.addCharacter(character, true);
                        } else {
                            dbHelper.updateCharacter(character, true);
                        }
                    }
                }
                loadDataFromSqlite();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Sync failed", Toast.LENGTH_SHORT).show();
            }
        });
        syncUnsyncedData();
    }

    private void syncUnsyncedData() {
        List<Character> unsyncedCharacters = dbHelper.getUnsyncedCharacters();
        for (Character character : unsyncedCharacters) {
            firebaseRef.child(character.getId()).setValue(character)
                    .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dbHelper.markAsSynced(character.getId());
                        }
                    })
                    .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Sync failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_character, container, false);

        setupRecyclerView(view);
        setupAddButton(view);
        loadDataFromSqlite();

        return view;
    }

    private void loadDataFromSqlite() {
        List<Character> characters = dbHelper.getAllCharacters();
        if (adapter != null) {
            adapter.updateData(characters);
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCharacters);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CharacterAdapter(getContext(), dbHelper.getAllCharacters(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupAddButton(View view) {
        View btnAddCharacter = view.findViewById(R.id.btnAddCharacter);
        btnAddCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEditCharacterDialog(null);
            }
        });
    }

    private void showAddEditCharacterDialog(final Character character) {
        final boolean isEditing = character != null;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_character, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText editTextName = dialogView.findViewById(R.id.editTextCharacterName);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextCharacterDescription);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancelCharacter);
        Button buttonSave = dialogView.findViewById(R.id.buttonSaveCharacter);

        if (isEditing) {
            editTextName.setText(character.getName());
            editTextDescription.setText(character.getDescription());
            dialog.setTitle("Edit Character");

            if (character.isDefault()) {
                editTextName.setEnabled(false);
                editTextDescription.setEnabled(false);
                buttonSave.setVisibility(View.GONE);
                dialog.setTitle("View Character");
            }
        } else {
            dialog.setTitle("Add Character");
        }

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Name and description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                updateCharacter(character.getId(), name, description);
            } else {
                saveCharacter(name, description);
            }
            dialog.dismiss();
        });
    }

    private void saveCharacter(String name, String description) {
        Character newCharacter = new Character();
        newCharacter.setId(generateId());
        newCharacter.setName(name);
        newCharacter.setDescription(description);
        newCharacter.setDefault(false); // Ensure user-added characters are not default

        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        newCharacter.setDatecreate(currentDate);
        newCharacter.setDateupdate(currentDate);

        isOnline = isNetworkAvailable();
        dbHelper.addCharacter(newCharacter, isOnline);

        Toast.makeText(getContext(), "Character saved successfully", Toast.LENGTH_SHORT).show();

        if (isOnline && firebaseRef != null) {
            firebaseRef.child(newCharacter.getId()).setValue(newCharacter)
                    .addOnSuccessListener(aVoid -> dbHelper.markAsSynced(newCharacter.getId()));
        }
        loadDataFromSqlite();
    }

    private String generateId() {
        return "char_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    @Override
    public void onEditClick(Character character) {
        showAddEditCharacterDialog(character);
    }

    private void updateCharacter(String characterId, String name, String description) {
        Character existingCharacter = dbHelper.getCharacterById(characterId);

        if (existingCharacter != null) {
            existingCharacter.setName(name);
            existingCharacter.setDescription(description);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            existingCharacter.setDateupdate(currentDate);

            isOnline = isNetworkAvailable();
            dbHelper.updateCharacter(existingCharacter, isOnline);

            Toast.makeText(getContext(), "Character updated successfully", Toast.LENGTH_SHORT).show();

            if (isOnline && firebaseRef != null) {
                firebaseRef.child(characterId).setValue(existingCharacter)
                        .addOnSuccessListener(aVoid -> dbHelper.markAsSynced(characterId));
            }
            loadDataFromSqlite();
        }
    }
    @Override
    public void onDeleteClick(Character character) {
        if (character.isDefault()) {
            Toast.makeText(getContext(), "Default characters cannot be deleted.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.deleteCharacter(character.getId());

        if (isOnline && firebaseRef != null) {
            firebaseRef.child(character.getId()).removeValue();
        }

        loadDataFromSqlite();
        Toast.makeText(getContext(), "Character deleted", Toast.LENGTH_SHORT).show();
    }
}