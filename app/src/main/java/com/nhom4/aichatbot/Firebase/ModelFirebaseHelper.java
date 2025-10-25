package com.nhom4.aichatbot.Firebase;

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

    public ModelFirebaseHelper() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        this.firebaseModelsRef = userRef.child("models");
    }

    public void syncUserModels(ModelDbHelper modelDbHelper, SyncCallback callback) {
        firebaseModelsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Model model = snapshot.getValue(Model.class);
                    if (model != null) {
                        String Id = snapshot.getKey();
                        model.setId(Id);
                        if (modelDbHelper.getModelById(Id) == null) {
                            modelDbHelper.addModel(model, true);
                        } else {
                            if (modelDbHelper.getModelById(Id).isActive()) {
                                model.setActive(true);
                                count++;
                                if (count > 1) {
                                    model.setActive(false);
                                }
                            } else {
                                model.setActive(false);
                            }
                            modelDbHelper.updateModel(model, true);
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

    public DatabaseReference getModelsRef() {
        return firebaseModelsRef;
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