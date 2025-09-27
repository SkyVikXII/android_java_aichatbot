package com.nhom4.aichatbot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nhom4.aichatbot.Models.User;
import com.nhom4.aichatbot.Models.Character;

public class FirebaseAuthHelper {
    private final String TAG = "FirebaseAuthHelper";
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // Create new user
    public void createUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    // Create user without uid field
                    User user = new User(email);
                    saveUserData(user, uid);  // Pass UID separately
                }
            }
            listener.onComplete(task);
        });
    }
    private void saveUserData(User user, String uid) {
        mDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // User data saved successfully
            }
        });
    }

    // Sign in user
    public void signInUser(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }
    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }
    public void signOut() {
        mAuth.signOut();
    }
    public boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}