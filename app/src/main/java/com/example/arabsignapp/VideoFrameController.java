package com.example.arabsignapp;

import android.graphics.Bitmap;
import android.graphics.NinePatch;

import androidx.camera.core.ImageProxy;
import com.google.mediapipe.framework.image.MPImage;

public class VideoFrameController {
    private final GestureRecognitionModel gestureRecognitionModel;

    public VideoFrameController(GestureRecognitionModel model) {
        this.gestureRecognitionModel = model;
    }


    public String processFrame(ImageProxy image) {
        // Convert ImageProxy to a format suitable for model input (e.g., Bitmap or MPImage)
        Bitmap bitmap = imageToBitmap(image);

        // Process with the model and get result
        String recognizedGesture = gestureRecognitionModel.recognizeGesture(bitmap);

        // Close the image after processing
        image.close();

        return recognizedGesture;
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        return BitmapUtils.getBitmap(image);
    }

}
