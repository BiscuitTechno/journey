package com.project.journey.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.journey.R;
import com.project.journey.fragments.JourneyButtonFragment;

import java.util.Arrays;
import java.util.List;

public class JourneyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private BottomNavigationView bottomNavigationView;
    private GoogleMap googleMap;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey); // Ensure this layout is correctly set up

        // Initialize Google Places API
        Places.initialize(getApplicationContext(), "AIzaSyBZvoWYwyeOmhAjoX5xuXcas6RIL79dbJQ");

        // Initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Wait for the map to be ready
        }

        bottomNavigationView = findViewById(R.id.bottom_nav_menu); // ID of BottomNavigationView in layout

        // Initialize JourneyButtonFragment
        if (savedInstanceState == null) {
            displayFragment(new JourneyButtonFragment());
        }

        // Set up listener for BottomNavigationView
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
                if (item.getItemId() == R.id.bottom_journey) {
                    if (!(currentFragment instanceof JourneyButtonFragment)) {
                        displayFragment(new JourneyButtonFragment());
                    }
                    return true; // Indicate that the event has been handled
                }
                return false; // If no item matched, return false
            }
        });
    }

    // Method to display the fragment
    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.commit();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        // Perform map operations here, e.g., adding markers
        LatLng location = new LatLng(21.0285, 105.8542); // Hà Nội
        googleMap.addMarker(new MarkerOptions().position(location).title("Marker in Hanoi"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

        // Call the function to find current places
        findCurrentPlaces();

    }
    public GoogleMap getGoogleMap() {
        return googleMap; // Getter for the GoogleMap instance
    }
    private void findCurrentPlaces() {
        // Define the place fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION);

        // Create a request for current place
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call the Places API
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<FindCurrentPlaceResponse> task = placesClient.findCurrentPlace(request);
        task.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (PlaceLikelihood placeLikelihood : task.getResult().getPlaceLikelihoods()) {
                        // Add markers for each place
                        LatLng latLng = placeLikelihood.getPlace().getLatLng();
                        if (latLng != null) {
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(placeLikelihood.getPlace().getName()));
                        }
                    }
                }
            }
        });
    }
}