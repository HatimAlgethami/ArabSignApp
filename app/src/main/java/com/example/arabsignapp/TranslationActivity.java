package com.example.arabsignapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class TranslationActivity extends AppCompatActivity {

    AlertDialog dialog;
    private ActivityResultLauncher<String> requestPermission;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
//    String translateText;
//    ImageAnalysis imageAnalysis;
//    Handler handler;
//    Runnable runs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission
        = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
            if (isGranted){
                cameraActivate();
            }
            else{
                customPopupTsl(R.layout.dialog_permission_denied);
            }
        });
        setContentView(R.layout.translation_activity);
        findViewById(R.id.conv_end).setOnClickListener(v -> popupTsl());
        requestCamera();
    }

    public void toActivityTsl(){
        dialog.dismiss();
        Intent intent = new Intent(this,UserMainActivity.class);
        startActivity(intent);
    }

    public void popupTsl() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.getContext().setTheme(R.style.dialogradius);

        LayoutInflater inflater = this.getLayoutInflater();
        View eventView = inflater.inflate(R.layout.dialog_save,null);

        eventView.findViewById(R.id.save_button).setOnClickListener(v -> toActivityTsl());
        eventView.findViewById(R.id.cancel_save_button).setOnClickListener(v -> returnFromPopupTsl());

        builder.setView(eventView);
        dialog = builder.create();
        dialog.show();
    }

    public void customPopupTsl(int layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.getContext().setTheme(R.style.dialogradius);

        View eventView = inflater.inflate(layout,null);
        eventView.findViewById(R.id.denied_return_button).setOnClickListener(v -> outOfActivityTsl());

        builder.setView(eventView);
        dialog = builder.create();
        dialog.show();
    }

    public void returnFromPopupTsl() {
        dialog.dismiss();
    }

    public void outOfActivityTsl(){
        dialog.dismiss();
        Intent intent = new Intent(this,UserMainActivity.class);
        startActivity(intent);
    }

    private void requestCamera(){

        boolean allowed = ContextCompat.checkSelfPermission(
                getApplicationContext(),Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        if (allowed){
            cameraActivate();
        }
        else{
            requestPermission.launch(Manifest.permission.CAMERA);
            }
        }

    private void cameraActivate(){
        try {
            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        }catch (NullPointerException e){
        }
        cameraProviderFuture.addListener(()->{
            try{
                ProcessCameraProvider camProcessProvider
                        = cameraProviderFuture.get();
                cameraStart(camProcessProvider);
            }
            catch (ExecutionException | InterruptedException camE){

            }
        },ContextCompat.getMainExecutor(this));
    }

    private void cameraStart(ProcessCameraProvider camProcessProvider){
        PreviewView camPV = findViewById(R.id.camPV);
        try {
            Preview camPreview = new Preview.Builder().build();
            CameraSelector camSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            camPreview.setSurfaceProvider(camPV.getSurfaceProvider());
            camProcessProvider.unbindAll();

//            imageAnalysis = new ImageAnalysis.Builder().build();
//            TextView translateView = findViewById(R.id.translateView);

//            imageAnalysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, image -> {
//                //send frame to server here (16ms per frame in 60fps according to docs)
//
//                translateText = ""+image.getFormat();
//                image.close();
//            });

            Camera cam =
                    camProcessProvider
                    .bindToLifecycle(this,camSelector,camPreview);

//            handler = new Handler();
//            runs = new Runnable() {
//                @Override
//                public void run() {
//                    //send the translated text here
//                    translateView.setText(translateText);
//                    handler.postDelayed(this,160);
//                }
//            };
//            handler.post(runs);

        }
        catch (NullPointerException npe){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacks(runs);
//        imageAnalysis.clearAnalyzer();
    }
}