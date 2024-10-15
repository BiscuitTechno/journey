package com.project.journey.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private VelocityTracker velocityTracker;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    // Variables to store the initial touch position
    private float initialX;
    private float initialY;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Initialize VelocityTracker and record initial touch positions
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(event);
                initialX = event.getX(); // Save the initial X position
                initialY = event.getY(); // Save the initial Y position
                break;

            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(1000); // Compute velocity in pixels per second
                float velocityX = velocityTracker.getXVelocity();
                float velocityY = velocityTracker.getYVelocity();

                // Calculate the difference from the initial position
                float deltaX = event.getX() - initialX;
                float deltaY = event.getY() - initialY;

                // Detect horizontal swipes
                if (Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (velocityX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                }

                // Clean up the VelocityTracker
                if (velocityTracker != null) {
                    velocityTracker.recycle(); // Recycle the VelocityTracker
                    velocityTracker = null;
                }
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
        // Override this method to handle right swipe
    }

    public void onSwipeLeft() {
        // Override this method to handle left swipe
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        public boolean onFling(MotionEvent e1, MotionEvent e2) {
            // This can be left empty if we're using onTouch for swipe detection
            return false;
        }
    }
}
