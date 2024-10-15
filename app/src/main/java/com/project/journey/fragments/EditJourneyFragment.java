package com.project.journey.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.journey.R;
import com.project.journey.adapters.TagAdapter;
import com.project.journey.databinding.FragmentEditJourneyBinding;
import com.project.journey.models.Journey;
import com.project.journey.models.Tag;
import com.project.journey.viewmodels.JourneyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditJourneyFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "EditJourneyFragment";
    private JourneyViewModel viewModel;
    private FragmentEditJourneyBinding binding;
    private RecyclerView locationsRecyclerView;
    private ArrayAdapter<String> spinnerAdapter;
    private TagAdapter tagAdapter;
    private List<Tag> tags;
    private GoogleMap googleMap;
    private String journeyID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditJourneyBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(JourneyViewModel.class);

        // Lấy journeyID từ arguments
        if (getArguments() != null) {
            journeyID = getArguments().getString("journeyID");
        }


        // Khởi tạo danh sách tag và adapter
        tags = new ArrayList<>();
        tagAdapter = new TagAdapter(tags, this::onTagClick);

        // Thiết lập RecyclerView
        binding.locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.locationsRecyclerView.setAdapter(tagAdapter);

        // Khởi tạo Firebase User
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        String userId = currentUser != null ? currentUser.getUid() : "unknown_user";

        // Quan sát dữ liệu từ ViewModel
        viewModel.getJourneys().observe(getViewLifecycleOwner(), this::updateSpinner);
        viewModel.fetchJourneys("unknown_user");
        viewModel.getTagsForJourney(journeyID).observe(getViewLifecycleOwner(), new Observer<List<Tag>>() {
            @Override
            public void onChanged(List<Tag> updatedTags) {
                updateRecyclerView(updatedTags);
            }
        });
        // Thêm swipe để xóa tag
        addSwipeToDelete(binding.locationsRecyclerView);

        // Thêm kéo thả để thay đổi thứ tự tag
        addDragAndDrop(binding.locationsRecyclerView);

        // Thiết lập map
        setUpMapFragment();

        // Khởi tạo và thêm SearchFragment
        setUpSearchFragment();

        return binding.getRoot();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
        updateMapMarkers();
        googleMap.setOnMapClickListener(this::onMapClick);
    }

    private void setUpMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    private void onMapClick(LatLng latLng) {
        if (isPointInMapBounds(latLng)) {
            Tag newTag = new Tag(UUID.randomUUID().toString(), "Unnamed Location", latLng.latitude, latLng.longitude, 0);
            onTagClick(newTag);
        }
    }

    private void updateSpinner(List<Journey> journeys) {
        List<String> journeyNames = new ArrayList<>();
        for (Journey journey : journeys) {
            journeyNames.add(journey.getJourneyName());
        }

        spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, journeyNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.journeySpinner.setAdapter(spinnerAdapter);

        binding.journeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Journey selectedJourney = journeys.get(position);
                fetchTagsForJourney(selectedJourney.getjourneyID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không có hành động nào
            }
        });
    }

    private void updateRecyclerView(List<Tag> updatedTags) {
        tags.clear();
        tags.addAll(updatedTags);
        tagAdapter.listTags(tags);
        tagAdapter.notifyDataSetChanged();
        updateMapMarkers();
    }

    private void fetchTagsForJourney(String journeyId) {
        viewModel.fetchTagsForJourney(journeyId, new JourneyViewModel.OnTagsFetchListener() {
            @Override
            public void onTagsFetched(List<Tag> newTags) {
                updateRecyclerView(newTags);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching tags: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onTagClick(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter the name for the location");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagName = input.getText().toString();
            if (!tagName.isEmpty()) {
                tag.setTagName(tagName);
                viewModel.addTag(tag);
                updateMapMarkers();
                tagAdapter.listTags(tags); // Cập nhật danh sách tag
            } else {
                Toast.makeText(getContext(), "The location name can't be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addDragAndDrop(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Di chuyển item trong adapter
                tagAdapter.moveTag(fromPosition, toPosition);

                // Cập nhật thứ tự trong Firestore
                updateTagsOrderInFirestore();

                return true; // Trả về true để thông báo rằng việc di chuyển đã hoàn tất
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không sử dụng cho kéo thả
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int position = viewHolder.getAdapterPosition();
                    Tag tagToDelete = tagAdapter.getTagAtPosition(position);
                    viewModel.removeTag(tagToDelete);
                    tags.remove(tagToDelete);
                    tagAdapter.listTags(tags);
                    updateMapMarkers();
                    Toast.makeText(getContext(), "Tag deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void updateTagsOrderInFirestore() {
        // Lấy danh sách tag đã cập nhật và lưu vào Firestore
        List<Tag> updatedTags = tagAdapter.getTags();
        viewModel.updateTagsOrder(updatedTags); // Cập nhật thứ tự mới lên Firestore
    }

    private void updateMapMarkers() {
        if (googleMap != null) {
            googleMap.clear(); // Xóa tất cả markers cũ trước khi thêm mới
            for (Tag tag : tags) {
                LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(position).title(tag.getTagName()));
            }
        }
    }

    private boolean isPointInMapBounds(LatLng latLng) {
        LatLng southwest = googleMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northeast = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        return latLng.latitude >= southwest.latitude && latLng.latitude <= northeast.latitude
                && latLng.longitude >= southwest.longitude && latLng.longitude <= northeast.longitude;
    }

    private void setUpSearchFragment() {
        SearchFragment searchFragment = new SearchFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.autocomplete_fragment, searchFragment);
        transaction.commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
