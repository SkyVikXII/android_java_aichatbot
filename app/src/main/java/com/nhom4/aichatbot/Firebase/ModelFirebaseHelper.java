package com.nhom4.aichatbot.Firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Database.ModelDbHelper;
import com.nhom4.aichatbot.Models.Model;

public class ModelFirebaseHelper {
    private final DatabaseReference firebaseModelsRef;
    public final ModelDbHelper sqlite;

    public ModelFirebaseHelper(Context context) {
        this.sqlite = new ModelDbHelper(context);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        this.firebaseModelsRef = userRef.child("models");
    }

    public void syncUserModels(SyncCallback callback) {
        firebaseModelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int activeCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Model model = snapshot.getValue(Model.class);
                    if (model != null) {
                        String id = snapshot.getKey();
                        model.setId(id);

                        Model existingModel = sqlite.getModelById(id);
                        if (existingModel == null) {
                            sqlite.addModel(model, true);
                        } else {
                            // Handle active model logic
                            if (existingModel.isActive()) {
                                activeCount++;
                                model.setActive(activeCount <= 1);
                            } else {
                                model.setActive(false);
                            }
                            sqlite.updateModel(model, true);
                            firebaseModelsRef.child(model.getId()).setValue(model);
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

    public void addModel(Model model) {
        firebaseModelsRef.child(model.getId()).setValue(model);
    }

    public void updateModel(Model model) {
        firebaseModelsRef.child(model.getId()).setValue(model);
    }

    public void deleteModel(String modelId) {
        firebaseModelsRef.child(modelId).removeValue();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}