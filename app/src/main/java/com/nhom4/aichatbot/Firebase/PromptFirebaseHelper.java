package com.nhom4.aichatbot.Firebase;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Database.PromptDbHelper;
import com.nhom4.aichatbot.Models.Prompt;

public class PromptFirebaseHelper {
    private final DatabaseReference firebasePromptsRef;

    public PromptFirebaseHelper() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        this.firebasePromptsRef = userRef.child("prompts");
    }

    public void syncUserPrompts(PromptDbHelper promptDbHelper, SyncCallback callback) {
        firebasePromptsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Prompt prompt = snapshot.getValue(Prompt.class);
                    if (prompt != null) {
                        String Id = snapshot.getKey();
                        prompt.setId(Id);
                        if (promptDbHelper.getPromptById(Id) == null) {
                            promptDbHelper.addPrompt(prompt, true);
                        } else {
                            if (promptDbHelper.getPromptById(Id).isActive()) {
                                prompt.setActive(true);
                            } else {
                                prompt.setActive(false);
                            }
                            promptDbHelper.updatePrompt(prompt, true);
                            firebasePromptsRef.child(prompt.getId()).setValue(prompt);
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

    public DatabaseReference getPromptsRef() {
        return firebasePromptsRef;
    }

    public void addPrompt(Prompt prompt) {
        firebasePromptsRef.child(prompt.getId()).setValue(prompt);
    }

    public void updatePrompt(Prompt prompt) {
        firebasePromptsRef.child(prompt.getId()).setValue(prompt);
    }

    public void deletePrompt(String promptId) {
        firebasePromptsRef.child(promptId).removeValue();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}