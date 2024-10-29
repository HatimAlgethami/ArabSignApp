package com.example.arabsignapp;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import androidx.camera.core.ImageProxy;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BitmapUtils {


    public static Bitmap getBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            return yuv420888ToBitmap(image);
        } else {
            throw new UnsupportedOperationException("Unsupported image format");
        }
    }


    private static Bitmap yuv420888ToBitmap(ImageProxy image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] jpegBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }
}
