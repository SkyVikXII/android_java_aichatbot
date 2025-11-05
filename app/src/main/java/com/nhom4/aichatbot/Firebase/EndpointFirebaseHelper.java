package com.nhom4.aichatbot.Firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhom4.aichatbot.Database.EndpointDbHelper;
import com.nhom4.aichatbot.Models.Endpoint;

import java.util.ArrayList;

public class EndpointFirebaseHelper {
    private final DatabaseReference firebaseEndpointsRef;
    private final DatabaseReference firebaseSystemEndpointsRef;
    public final EndpointDbHelper sqlite;

    public EndpointFirebaseHelper(Context context) {
        this.sqlite = new EndpointDbHelper(context);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        this.firebaseEndpointsRef = userRef.child("endpoints");
        this.firebaseSystemEndpointsRef = FirebaseDatabase.getInstance().getReference().child("system").child("endpoint");
    }

    public void syncSystemEndpoints(SyncCallback callback) {
        firebaseSystemEndpointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Endpoint endpoint = snapshot.getValue(Endpoint.class);
                    if (endpoint != null) {
                        String systemId = "system_" + snapshot.getKey();
                        endpoint.setId(systemId);

                        Endpoint existingEndpoint = sqlite.getEndpointById(systemId);
                        if (existingEndpoint == null) {
                            sqlite.addEndpoint(endpoint, true);
                        } else {
                            endpoint.setActive(existingEndpoint.isActive());
                            sqlite.updateEndpoint(endpoint, true);
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

    public void syncUserEndpoints(SyncCallback callback) {
        firebaseEndpointsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Endpoint endpoint = snapshot.getValue(Endpoint.class);
                    if (endpoint != null) {
                        String id = snapshot.getKey();
                        endpoint.setId(id);

                        Endpoint existingEndpoint = sqlite.getEndpointById(id);
                        if (existingEndpoint == null) {
                            sqlite.addEndpoint(endpoint, true);
                        } else {
                            endpoint.setActive(existingEndpoint.isActive());
                            sqlite.updateEndpoint(endpoint, true);
                            firebaseEndpointsRef.child(endpoint.getId()).setValue(endpoint);
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

    public void addEndpoint(Endpoint endpoint) {
        firebaseEndpointsRef.child(endpoint.getId()).setValue(endpoint);
    }

    public void updateEndpoint(Endpoint endpoint) {
        firebaseEndpointsRef.child(endpoint.getId()).setValue(endpoint);
    }

    public void deleteEndpoint(String endpointId) {
        firebaseEndpointsRef.child(endpointId).removeValue();
    }

    public interface SyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}