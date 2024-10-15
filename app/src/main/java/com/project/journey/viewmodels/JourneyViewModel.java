package com.project.journey.viewmodels;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.journey.models.Tag;
import com.project.journey.models.Journey;
import com.project.journey.repositories.JourneyRepository;
import com.project.journey.repositories.TagRepository;

import java.util.ArrayList;
import java.util.List;

public class JourneyViewModel extends ViewModel {
    private final MutableLiveData<String> journeyName = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Journey>> journeysLiveData = new MutableLiveData<>();
    private final JourneyRepository journeyRepository;
    private final TagRepository tagRepository; // Khai báo tagRepository
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnTagsFetchListener {
        void onTagsFetched(List<Tag> tags);

        // Gọi khi danh sách thẻ được lấy thành công
        void onError(Exception e);
    }

    public JourneyViewModel(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
        journeyRepository = new JourneyRepository();
    }

    public LiveData<String> getJourneyName() {
        return journeyName;
    }

    public void setJourneyName(String name) {
        journeyName.setValue(name);
    }

    public LiveData<List<Tag>> getTags() {
        return tags;
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

    public LiveData<List<Journey>> getJourneys() {
        return journeysLiveData;
    }
    public JourneyViewModel() {
        journeyRepository = new JourneyRepository();
        tagRepository = new TagRepository();
    }

    // Hàm lấy hành trình từ Firestore
    public void fetchJourneys(String userId) {
        db.collection("journeys")
                .whereEqualTo("createdBy", userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("JourneyViewModel", "Listen failed.", e);
                        return;
                    }
                    if (snapshot != null) {
                        List<Journey> journeyList = snapshot.toObjects(Journey.class);
                        journeysLiveData.setValue(journeyList);
                    }
                });
    }
    // Lấy danh sách thẻ dựa trên journeyId
    public void fetchTagsForJourney(String journeyId, OnTagsFetchListener listener) {
        db.collection("journeys").document(journeyId)
                .collection("tags") // Truy vấn các thẻ bên trong hành trình
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Tag> tags = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Tag tag = document.toObject(Tag.class);
                            tags.add(tag);
                        }
                        listener.onTagsFetched(tags); // Gọi listener khi đã lấy được danh sách tags
                    } else {
                        listener.onError(task.getException()); // Gọi listener khi có lỗi
                    }
                });
    }

    public void updateTagsOrder(List<Tag> updatedTags) {
        journeyRepository.updateTagsOrder(updatedTags); // Gọi phương thức trong repository để cập nhật Firestore
    }

    public void saveJourney(Journey journey, JourneyRepository.OnJourneySaveListener listener) {
        journeyRepository.saveJourney(journey, listener);
    }
    public LiveData<List<Tag>> getTagsForJourney(String journeyID) {
        MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy tài liệu hành trình theo journeyID
        db.collection("journeys")
                .document(journeyID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy danh sách thẻ từ tài liệu hành trình
                            List<Tag> tags = (List<Tag>) document.get("tags");
                            tagsLiveData.setValue(tags);
                        } else {
                            // Nếu tài liệu không tồn tại
                            tagsLiveData.setValue(new ArrayList<>());
                        }
                    } else {
                        // Xử lý lỗi
                        tagsLiveData.setValue(new ArrayList<>()); // Hoặc thông báo lỗi
                    }
                });

        return tagsLiveData;
    }

    public void updateTagOrder(String journeyID, List<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            tag.setOrder(i); // Thiết lập thuộc tính index để lưu trữ thứ tự

            // Sử dụng tagRepository để cập nhật Firestore
            TagRepository.updateTag(tag.getTagId(), tag)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tag order updated: " + tag.getTagId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating tag order: ", e);
                    });
        }
    }
}


