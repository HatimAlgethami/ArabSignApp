package com.example.arabsignapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranslationActivity extends AppCompatActivity {

    AlertDialog dialog;
    private ActivityResultLauncher<String> requestPermission;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageAnalysis imageAnalysis;
    Handler handler;
    Runnable runs;
    TextView translateView;
    private String translateText;
    private Socket socket;
    private DataOutputStream outStream;
    private BufferedReader inStream;
    private Gson gson;
    private boolean newText;
    ExecutorService singleThreadEx;
    private ScheduledExecutorService socketEx;
    private Bitmap bitmapImg;
    LinkedHashMap<String,Object> dataMap;
    ByteArrayOutputStream baos;
    private boolean arabic_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        newText = false;
        String selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        if (selectedLanguage==null){
            arabic_mode=false;
        }
        else if (selectedLanguage.equals("ASL")){
            arabic_mode=false;
        }
        else if (selectedLanguage.equals("ARSL")){
            arabic_mode=true;
        }
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

        boolean camAllowed = ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        if (camAllowed){
            //initialize variables
            singleThreadEx =Executors.newSingleThreadExecutor();
            socketEx = Executors.newScheduledThreadPool(2);
            handler = new Handler();
            translateView = findViewById(R.id.translateView);
            gson = new Gson();
            setUpSocket();

            //activate camera
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
        //set up preview
        PreviewView camPV = findViewById(R.id.camPV);
        try {

            Preview camPreview = new Preview.Builder()
                    .build();

            CameraSelector camSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            camPreview.setSurfaceProvider(camPV.getSurfaceProvider());
            camProcessProvider.unbindAll();

            imageAnalysis = new ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build();
            imageAnalysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, this::imageAnalyzer);

            camProcessProvider.bindToLifecycle(this,camSelector,camPreview,imageAnalysis);

            if (socket!=null){
                createTranslationEvents();
            }

        }
        catch (NullPointerException npe){
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketEx.shutdownNow();
        closeSocket();
        imageAnalysis.clearAnalyzer();
        handler.removeCallbacks(runs);
    }

    private void imageAnalyzer(ImageProxy image){
        //send frame to server here (16ms per frame in 60fps according to docs)
        try {
            if (socket==null||socketEx==null||outStream==null||inStream==null){
                image.close();
                return;
            }

            //capture current frame
            bitmapImg = image.toBitmap();
        }
        catch (Exception e) {

        }
        image.close();
    }

    private void createTranslationEvents(){
        runs = () -> {
            //set the result of translation here
            try {
                if (newText) {
                    translateView.setText(translateText);
                    newText = false;
                }
            } catch (Exception e) {
            }
            handler.postDelayed(runs,16);
        };
        handler.post(runs);

        //get translation result in a dedicated thread
        socketEx.scheduleWithFixedDelay(this::respondToSocket,16,16, TimeUnit.MILLISECONDS);

        socketEx.scheduleWithFixedDelay(() -> {
            try {
                if (bitmapImg!=null){
                    //send current frame in a dedicated thread
                    sendToSocket();
                }
            }
            catch (Exception e){

            }
        }, 16, 16, TimeUnit.MILLISECONDS);
    }

    private void sendToSocket() throws Exception{
        bitmapImg.compress(Bitmap.CompressFormat.PNG,100,baos);
        String encodedImg = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        dataMap.put("base64Image",encodedImg);
        dataMap.put("arabic_mode",arabic_mode);
        String sentMessage = gson.toJson(dataMap)+'\n';
        outStream.writeBytes(sentMessage);
        outStream.flush();
        baos.reset();
        dataMap.clear();
    }

    private void respondToSocket(){
        try {
            if (!inStream.ready()){
                return;
            }
            String response = inStream.readLine();
            Type mapType = new TypeToken<LinkedHashMap<String, Object>>() {
            }.getType();
            dataMap = gson.fromJson(response, mapType);
//        String encodedImg = (String)dataMap.get("base64Image");
//        byte[] bytes = Base64.decode(encodedImg,Base64.DEFAULT);
//        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//        return BitmapFactory.decodeStream(bais);
            String prediction,prediction_proba;
            prediction = String.valueOf(dataMap.get("prediction"));
            prediction_proba = String.valueOf(dataMap.get("prediction_proba"));
            dataMap.clear();
            if (prediction.equals("0.0")){
                translateText="تظهر الترجمة هنا";
                translateView.setTextColor(getResources().getColor(R.color.white,null));
            }
            else{
                translateText = "الترجمة: " + prediction + '\n' + "الدقة: " + prediction_proba;
                translateView.setTextColor(getResources().getColor(R.color.green,null));
            }
            newText = true;
        }
        catch (Exception e){
        }
    }

    private void setUpSocket(){
        singleThreadEx.execute(this::initializeSocket);
    }

    private void closeSocket(){
        singleThreadEx.execute(() -> {
            try {
                if (socket!=null && !socket.isClosed()){
                    socket.close();
                }
            } catch (Exception e) {
            }
        });
    }

    private void initializeSocket(){
        socket = null;
        outStream = null;
        inStream = null;
        try{
            //initialize socket
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost",9090));
            outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            inStream = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(socket.getInputStream())));
            dataMap = new LinkedHashMap<>();
            baos = new ByteArrayOutputStream();
        }
        catch (Exception e){
            //means connection failed
            socket = null;
        }
    }
}