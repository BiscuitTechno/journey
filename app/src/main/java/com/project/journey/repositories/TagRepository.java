package com.project.journey.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.journey.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagRepository {
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>(new ArrayList<>());
    private static FirebaseFirestore firestore= FirebaseFirestore.getInstance();
    private static final String TAG = "TagRepository";

    public LiveData<List<Tag>> getTags() {
        return tags;
    }
    public TagRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void addTag(Tag tag) {
        List<Tag> currentTags = tags.getValue();
        if (currentTags != null) {
            currentTags.add(tag);
            tags.setValue(currentTags);
        }
    }

    public void removeTag(Tag tag) {
        List<Tag> currentTags = tags.getValue();
        if (currentTags != null) {
            currentTags.remove(tag);
            tags.setValue(currentTags);
        }
    }
    public static Task<Void> updateTag(String tagId, Tag tag) {
        return firestore.collection("tags")
                .document(tagId)
                .set(tag) // Lưu thông tin tag mới
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tag updated successfully: " + tagId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating tag: ", e);
                });
    }
}
