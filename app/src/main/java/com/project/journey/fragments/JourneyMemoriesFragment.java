package com.project.journey.fragments;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.project.journey.R;

public class JourneyMemoriesFragment extends Fragment {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journey_memories, container, false);

        // Khởi tạo Bottom Sheet Behavior
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Đảm bảo Bottom Sheet có thể kéo lên/xuống
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Khởi tạo ở trạng thái đóng

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Xử lý các trạng thái khác nhau nếu cần
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Xử lý hành vi kéo của Bottom Sheet
            }
        });

        return view;
    }
}
