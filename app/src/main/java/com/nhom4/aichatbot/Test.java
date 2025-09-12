package com.nhom4.aichatbot;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Test {
    Test(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("TEST");
        myRef.setValue("Hello, World!");
    }
}
