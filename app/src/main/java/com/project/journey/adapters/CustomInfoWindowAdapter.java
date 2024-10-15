//package com.project.journey.adapters;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.TextView;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.Marker;
//import com.project.journey.models.Tag;
//import com.project.journey.R;
//
//public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
//    private final View mWindow;
//    private Context mContext;
//
//    public CustomInfoWindowAdapter(Context context) {
//        mContext = context;
//        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
//    }
//
//    private void renderWindowText(Tag tag, View view) {
//        // Set the tag name
//        TextView tagNameTextView = view.findViewById(R.id.tagNameTextView);
//        tagNameTextView.setText(tag.getName());
//
//        // Set color for the tag name
//        tagNameTextView.setTextColor(Color.parseColor(tag.getColor())); // Set the tag color
//
//        // Display notes or additional information
//        TextView notesTextView = view.findViewById(R.id.notesTextView);
//        notesTextView.setText(tag.getNotes());
//
//        // Additional UI elements can be added here if needed
//    }
//
//    @Override
//    public View getInfoWindow(Marker marker) {
//        // Get the tag associated with the marker
//        Tag tag = (Tag) marker.getTag(); // Assuming tag object is stored in the marker's tag
//        if (tag != null) {
//            renderWindowText(tag, mWindow);
//        }
//        return mWindow;
//    }
//
//    @Override
//    public View getInfoContents(Marker marker) {
//        return null; // Use default behavior for contents
//    }
//}
