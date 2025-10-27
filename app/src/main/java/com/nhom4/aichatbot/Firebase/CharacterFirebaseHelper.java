package com.nhom4.aichatbot.Firebase;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Database.CharacterDbHelper;
import com.nhom4.aichatbot.Models.Character;
import com.nhom4.aichatbot.Models.Endpoint;

public class CharacterFirebaseHelper {
    private final DatabaseReference firebaseRef;

    public CharacterFirebaseHelper() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.firebaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("characters");
    }

    public void syncUserCharacters(CharacterDbHelper characterDbHelper, SyncCallback callback) {
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Character character = snapshot.getValue(Character.class);
                    if (character != null) {
                        String Id = snapshot.getKey();
                        character.setId(Id);
                        if (characterDbHelper.getCharacterById(Id) == null) {
                            characterDbHelper.addCharacter(character, true);
                        } else {
                            characterDbHelper.updateCharacter(character, true);
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