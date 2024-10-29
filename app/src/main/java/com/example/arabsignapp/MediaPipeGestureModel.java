package com.example.arabsignapp;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult;

public class MediaPipeGestureModel implements GestureRecognitionModel {

    public MediaPipeGestureModel(Context context) {

    }

    @Override
    public String recognizeGesture(Bitmap image) {
        //MPImage mpImage = MPImage.create(image);

        // Process the image with each landmarker and get results
        HandLandmarkerResult handResult;
        PoseLandmarkerResult poseResult;
        FaceLandmarkerResult faceResult ;

        String gesture = "";

        return gesture;
    }

    private String extractGestureFromResults(HandLandmarkerResult handResult, PoseLandmarkerResult poseResult, FaceLandmarkerResult faceResult) {
        return "Sample Gesture";
    }
}
