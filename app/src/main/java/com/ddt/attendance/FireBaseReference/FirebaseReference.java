package com.ddt.attendance.FireBaseReference;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.channels.OverlappingFileLockException;
import java.util.Objects;

public class FirebaseReference {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public CollectionReference collectionReference = db.collection("Users").document(Objects.requireNonNull(mAuth.getCurrentUser().getEmail())).collection("Classes");
    public DocumentReference documentReference = db.collection("Users").document(mAuth.getCurrentUser().getEmail());

    public FirebaseReference() {

    }

    public DocumentReference documentReference() {
        return documentReference;

    }

    public CollectionReference collectionReference() {
        return collectionReference;
    }

}
