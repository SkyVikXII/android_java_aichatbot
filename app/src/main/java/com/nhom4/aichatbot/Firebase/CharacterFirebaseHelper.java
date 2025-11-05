package com.nhom4.aichatbot.Firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Database.CharacterDbHelper;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Endpoint;

import java.util.List;


public class CharacterFirebaseHelper {
    private final DatabaseReference firebaseRef;
    public final CharacterDbHelper sqlite;

    public CharacterFirebaseHelper(Context context) {
        this.sqlite = new CharacterDbHelper(context);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.firebaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("characters");
    }

    public void syncUserCharacters(SyncCallback callback) {
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Character character = snapshot.getValue(Character.class);
                    if (character != null) {
                        String id = snapshot.getKey();
                        character.setId(id);

                        Character existingCharacter = sqlite.getCharacterById(id);
                        if (existingCharacter == null) {
                            sqlite.addCharacter(character, true);
                        } else {
                            sqlite.updateCharacter(character, true);
                            firebaseRef.child(character.getId()).setValue(character);
                        }
                    }
                }
                callback.onSuccess();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void syncUnsyncedCharacters() {
        List<Character> unsyncedCharacters = sqlite.getUnsyncedCharacters();
        for (Character character : unsyncedCharacters) {
            firebaseRef.child((character.getId())).setValue(character).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sqlite.markAsSynced(character.getId());
                }
            }
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public DatabaseReference getFirebaseRef() {
        return firebaseRef;
    }

    public void addCharacter(Character character) {
        firebaseRef.child(character.getId()).setValue(character);
    }

    public void updateCharacter(Character character) {
        firebaseRef.child(character.getId()).setValue(character);
    }

    public void deleteCharacter(String characterId) {
        firebaseRef.child(characterId).removeValue();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}