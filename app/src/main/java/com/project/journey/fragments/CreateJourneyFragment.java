package com.project.journey.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.project.journey.R;
import com.project.journey.models.Journey;
import com.project.journey.models.Tag;
import com.project.journey.repositories.JourneyRepository;
import com.project.journey.viewmodels.JourneyViewModel;
import com.project.journey.adapters.TagAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateJourneyFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "CreateJourneyFragment";
    private JourneyViewModel viewModel;
    private TextView journeyNameTextView;
    private SearchView searchView;
    private RecyclerView locationsRecyclerView;
    private Button saveButton;
    private TagAdapter locationAdapter;
    private GoogleMap googleMap; // Declare GoogleMap variable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_journey, container, false);

        // Initialize ViewModel and other components
        viewModel = new ViewModelProvider(this).get(JourneyViewModel.class);
        initializeFragment(view);

        // Khởi tạo và thêm SearchFragment
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setTagSelectedListener(tag -> {
            // Xử lý tag được chọn từ SearchFragment
            viewModel.addTag(tag); // Thêm tag vào ViewModel
            updateMapMarkers();
            Log.d(TAG, "Tag added from SearchFragment: " + tag.getTagName());
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.autocomplete_fragment, searchFragment); // ID của container nơi bạn muốn hiển thị SearchFragment
        fragmentTransaction.commit();

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Get the map asynchronously
        }

        return view;
    }

    private void initializeFragment(View view) {
        journeyNameTextView = view.findViewById(R.id.journeyName);
        locationsRecyclerView = view.findViewById(R.id.locations_recycler_view);
        saveButton = view.findViewById(R.id.save_locations_button);

        // Set up RecyclerView
        locationAdapter = new TagAdapter(new ArrayList<>(), this::onLocationClick);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationsRecyclerView.setAdapter(locationAdapter);

        // Observe location list changes
        viewModel.getTags().observe(getViewLifecycleOwner(), this::updateRecyclerView);

        // Set up save button listener
        saveButton.setOnClickListener(v -> saveJourney());

        // Add swipe functionality to delete tag
        addSwipeToDelete(locationsRecyclerView);

        // Show journey name dialog when fragment is created
        showJourneyNameDialog();
    }

    private void addSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // No move action needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int position = viewHolder.getAdapterPosition();
                    Tag tagToDelete = locationAdapter.getTagAtPosition(position);
                    viewModel.removeTag(tagToDelete);
                    updateRecyclerView(viewModel.getTags().getValue());
                    updateMapMarkers();
                    Toast.makeText(getContext(), "Tag deleted", Toast.LENGTH_SHORT).show();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // Initialize the GoogleMap
        LatLng initialPosition = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 10));
        googleMap.setOnMapClickListener(latLng -> {
            if (isPointInMapBounds(latLng)) {
                // Tạo một Tag mới với vị trí đã bấm
                Tag newTag = new Tag(UUID.randomUUID().toString(), "Unnamed Location", latLng.latitude, latLng.longitude,0);
                // Gọi onLocationClick để hiện dialog nhập tên
                onLocationClick(newTag);
                Log.d(TAG, "Location clicked and added: " + latLng.toString());
            } else {
                Log.d(TAG, "Click outside of map bounds: " + latLng.toString());
            }
        });
        // Thêm markers cho tất cả các tags đã thêm vào
        updateMapMarkers();

        Log.d(TAG, "GoogleMap is ready");
    }
    // Phương thức kiểm tra nếu điểm nằm trong giới hạn của bản đồ
    private boolean isPointInMapBounds(LatLng latLng) {
        LatLng southwest = googleMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northeast = googleMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        return latLng.latitude >= southwest.latitude && latLng.latitude <= northeast.latitude &&
                latLng.longitude >= southwest.longitude && latLng.longitude <= northeast.longitude;
    }

    private void showJourneyNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Journey Name");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String journeyName = input.getText().toString();
            viewModel.setJourneyName(journeyName); // Save journey name in ViewModel
            journeyNameTextView.setText(journeyName); // Update TextView
            Log.d(TAG, "Journey created with name: " + journeyName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveJourney() {
        String journeyName = viewModel.getJourneyName().getValue(); // Get journey name
        List<Tag> tags = viewModel.getTags().getValue(); // Get tags

        if (tags == null || tags.isEmpty()) {
            Log.e(TAG, "No tags available to create Journey");
            Toast.makeText(getContext(), "No locations added to the journey", Toast.LENGTH_SHORT).show();
            return;
        }

        String userID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "unknown_user";
        String journeyID = UUID.randomUUID().toString();
        Journey journey = new Journey(
                UUID.randomUUID().toString(), // Generate unique ID for the journey
                journeyName,                  // The name of the journey
                userID,                       // The ID of the user who created the journey
                tags                           // Pass the List<Tag> to the Journey
        );

        // Save journey using ViewModel
        viewModel.saveJourney(journey, new JourneyRepository.OnJourneySaveListener() {
            @Override
            public void onSuccess() {
                requireActivity().onBackPressed(); // Go back to Journey Button Fragment
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error saving journey: " + e.getMessage());
            }
        });
    }
    private void updateMapMarkers() {
        googleMap.clear(); // Xóa tất cả markers cũ trước khi thêm mới

        // Lấy danh sách các tag
        List<Tag> tags = viewModel.getTags().getValue();
        if (tags != null) {
            for (Tag tag : tags) {
                // Thêm marker cho từng tag
                LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(position).title(tag.getTagName()));
            }
            // Nếu có ít nhất một marker, phóng to bản đồ để tất cả markers đều nằm trong tầm nhìn
            if (!tags.isEmpty()) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Tag tag : tags) {
                    builder.include(new LatLng(tag.getLatitude(), tag.getLongitude()));
                }
                LatLngBounds bounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }
    private void onLocationClick(Tag Tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter name for the location");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String tagName = input.getText().toString();
            if (!tagName.isEmpty()) {
                // Tạo tag mới với tên đã nhập
                Tag newTag = new Tag(UUID.randomUUID().toString(), tagName, Tag.getLatitude(), Tag.getLongitude(),0);
                viewModel.addTag(newTag); // Thêm tag vào ViewModel
                Log.d(TAG, "Added new tag: " + tagName + " at (" + Tag.getLatitude() + ", " + Tag.getLongitude() + ")");
                updateRecyclerView(viewModel.getTags().getValue()); // Cập nhật RecyclerView với danh sách tag mới
                updateMapMarkers();
            } else {
                Toast.makeText(getContext(), "Location name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateRecyclerView(List<Tag> tags) {
        locationAdapter.listTags(tags); // Update RecyclerView with new tags
        locationAdapter.notifyDataSetChanged(); // Notify the adapter about data change
        Log.d(TAG, "RecyclerView updated with tags: " + tags);
    }
}
