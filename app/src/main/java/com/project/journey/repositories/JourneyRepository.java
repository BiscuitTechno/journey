package com.project.journey.repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.project.journey.models.Journey;
import com.project.journey.models.Tag;

import java.util.List;

public class JourneyRepository {
    private final FirebaseFirestore firestore;
    private String currentJourneyId;


    public JourneyRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface OnJourneySaveListener {
        void onSuccess();

        void onFailure(Exception e);
    }

    public void saveJourney(Journey journey, OnJourneySaveListener listener) {
        firestore.collection("journeys").document(journey.getjourneyID())
                .set(journey, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(); // Call success callback
                })
                .addOnFailureListener(listener::onFailure); // Call failure callback
    }

    public void setJourneyId(String journeyId) {
        this.currentJourneyId = journeyId;
    }
    public String getJourneyId() {
        return currentJourneyId;
    }
    public void updateTagsOrder(List<Tag> updatedTags) {
        // Lấy ID của journey tương ứng với tags
        String journeyId = getJourneyId(); // Lấy journeyId từ repository

        // Cập nhật Firestore
        for (Tag tag : updatedTags) {
            firestore.collection("journeys")
                    .document(journeyId)
                    .collection("tags")
                    .document(tag.getTagId()) // Lấy tag ID
                    .update("order", tag.getOrder()) // Cập nhật trường order
                    .addOnSuccessListener(aVoid -> {
                        // Xử lý khi cập nhật thành công
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi xảy ra lỗi
                    });
        }
    }
}
