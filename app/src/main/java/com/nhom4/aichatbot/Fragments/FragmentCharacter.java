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
        // Required empty public constructor
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
        firebaseRef = FirebaseDatabase.getInstance().getReference()
                .child("characters")
                .child(currentUserId);
        // Sync from Firebase to SQLite
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Character character = snapshot.getValue(Character.class);
                    if (character != null) {
                        // Save to SQLite and mark as synced
                        dbHelper.addCharacter(character, true);
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
                showAddCharacterDialog();
            }
        });
    }

    private void showAddCharacterDialog() {
        // Create dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_character, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Get references to dialog views
        final EditText editTextName = dialogView.findViewById(R.id.editTextCharacterName);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextCharacterDescription);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        // Set up button listeners
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String description = editTextDescription.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter character name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter character description", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveCharacter(name, description);
                dialog.dismiss();
            }
        });
    }

    private void saveCharacter(String name, String description) {
        // Create new character object
        Character newCharacter = new Character();
        newCharacter.setId(generateId());
        newCharacter.setName(name);
        newCharacter.setDescription(description);

        // Set current date and time
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        newCharacter.setDatecreate(currentDate);
        newCharacter.setDateupdate(currentDate);

        // Save to SQLite
        boolean isSynced = isOnline;
        long result = dbHelper.addCharacter(newCharacter, isSynced);

        if (result != -1) {
            Toast.makeText(getContext(), "Character saved successfully", Toast.LENGTH_SHORT).show();

            // If online, also save to Firebase
            if (isOnline && firebaseRef != null) {
                firebaseRef.child(newCharacter.getId()).setValue(newCharacter)
                        .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dbHelper.markAsSynced(newCharacter.getId());
                                Toast.makeText(getContext(), "Synced to cloud", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Save to cloud failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            // Refresh the list
            loadDataFromSqlite();
        } else {
            Toast.makeText(getContext(), "Failed to save character", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateId() {
        return "char_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    @Override
    public void onEditClick(Character character) {
        // Edit character logic
        Toast.makeText(getContext(), "Edit: " + character.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Character character) {
        // Delete from SQLite
        dbHelper.deleteCharacter(character.getId());

        // If online, also delete from Firebase
        if (isOnline && firebaseRef != null) {
            firebaseRef.child(character.getId()).removeValue()
                    .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully deleted from Firebase
                        }
                    })
                    .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Delete from cloud failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        loadDataFromSqlite();
        Toast.makeText(getContext(), "Character deleted", Toast.LENGTH_SHORT).show();
    }
}