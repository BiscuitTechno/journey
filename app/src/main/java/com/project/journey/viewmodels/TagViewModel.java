package com.project.journey.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.journey.models.Tag;
import com.project.journey.repositories.TagRepository;

import java.util.ArrayList;
import java.util.List;

public class TagViewModel extends ViewModel {

    private String journeyName; // Chỉ cần lưu tên hành trình
    private final List<Tag> tags = new ArrayList<>();


    // Lấy tên hành trình
    public String getJourneyName() {
        return journeyName;
    }

    // Đặt tên hành trình
    public void setJourneyName(String name) {
        this.journeyName = name;
    }

    // Lấy danh sách thẻ
    public List<Tag> getTags() {
        return tags;
    }

    // Thêm thẻ vào danh sách
    public void addTag(Tag tag) {
        tags.add(tag); // Thêm tag vào danh sách
    }
}