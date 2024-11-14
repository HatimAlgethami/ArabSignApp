package com.example.arabsignapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslationActivity extends AppCompatActivity {

    AlertDialog dialog;
    private ActivityResultLauncher<String> requestPermission;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageAnalysis imageAnalysis;
    Handler handler;
    Runnable runs;
    TextView translateView;
    private String translateText = "";
    private Socket socket;
    private DataOutputStream outStream;
    private BufferedReader inStream;
    private Gson gson;
    private boolean newText;
    ExecutorService singleThreadEx;
    private Bitmap bitmapImg;
    LinkedHashMap<String,Object> dataMap;
    ByteArrayOutputStream baos;
    private boolean arabic_mode,lastSocketCallFinished=true;
    private long letterStartTime;
    private long letterCurrentTime;
    private long wordStartTime;
    private long wordCurrentTime;
    private String lastPrediction = "";
    private ArrayList<String> sentence= new ArrayList<String>();
    private final LinkedHashMap<String,String> arabicLetters = new LinkedHashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        newText = false;
        String selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        if (selectedLanguage==null){
            arabic_mode=true;
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
                    .setOutputImageRotationEnabled(true)
                    .build();
            imageAnalysis.setAnalyzer(Executors.newCachedThreadPool(), this::imageAnalyzer);

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
        closeSocket();
        imageAnalysis.clearAnalyzer();
        handler.removeCallbacks(runs);
    }

    private void imageAnalyzer(ImageProxy image){
        //send frame to server here (16ms per frame in 60fps according to docs)
        try {
            if (socket==null||outStream==null||inStream==null){
                image.close();
                return;
            }

            //capture current frame
            bitmapImg = image.toBitmap();
//            bitmapImg=Bitmap.createBitmap(bitmapImg,0,0,image.getWidth(),image.getHeight()
//                    ,matrix,false);
            sendToSocket();
            respondToSocket();
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

    }

    private void sendToSocket() throws Exception{
        if(!lastSocketCallFinished){
            return;
        }
        bitmapImg.compress(Bitmap.CompressFormat.JPEG,90,baos);
        String encodedImg = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        dataMap.put("base64Image",encodedImg);
        dataMap.put("arabic_mode",arabic_mode);
        String sentMessage = gson.toJson(dataMap)+'*';
        outStream.writeBytes(sentMessage);
        outStream.flush();
        baos.reset();
        dataMap.clear();
        lastSocketCallFinished=false;
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

//            String encodedImg = (String)dataMap.get("base64Image");
//            byte[] bytes = Base64.decode(encodedImg,Base64.DEFAULT);
//            BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            String prediction,prediction_proba;
            prediction = String.valueOf(dataMap.get("prediction"));
            prediction_proba = String.valueOf(dataMap.get("prediction_proba"));
            dataMap.clear();
            if(translateText.isEmpty()) translateText="تظهر الترجمة هنا";
            if (prediction.equals("0.0")){
                translateView.setTextColor(getResources().getColor(R.color.dialogborder, null));
            }
            else{
                if (arabic_mode == true && !prediction.equals("0.0")){
                    prediction = dictionary(prediction);
                }
                String letter=checkLetter(prediction,prediction_proba);
                if(translateText.equals("تظهر الترجمة هنا")) translateText="";
                translateText += letter;
                translateView.setTextColor(getResources().getColor(R.color.green,null));
            }
            boolean condition = isWordEnd(prediction);
            Log.d("CONDITION", String.valueOf(condition));
            if (condition){
                sentence.add(translateText);
                translateText = "";
                Log.d("SENTENCENEW", sentence.toString());
            }
            newText = true;
            lastSocketCallFinished=true;
        }
        catch (Exception e){
            Log.d("EXC", String.valueOf(e.getStackTrace()[0].getLineNumber()));
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

            //ENTER IP OF SERVER HERE
            socket.connect(new InetSocketAddress("0.tcp.in.ngrok.io",12494));
            outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            inStream = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(socket.getInputStream())));
            dataMap = new LinkedHashMap<>();
            baos = new ByteArrayOutputStream();
        }
        catch (Exception e){
//            boolean connected = reconnectToSocket(3);
//            //means connection failed
//            if (!connected){
//                socket = null;
//            }
            socket=null;
        }
    }

//    private boolean reconnectToSocket(int connCount){
//        try{
//            socket.connect(new InetSocketAddress("localhost",9090));
//        }
//        catch (Exception e){
//            if (connCount<1){
//                return false;
//            }
//            reconnectToSocket(connCount-1);
//        }
//        return true;
//    }
    private String checkLetter(String prediction,String prediction_proba){
        if (!prediction.equals(lastPrediction)) {
            letterStartTime = System.currentTimeMillis();
            lastPrediction = prediction;
        }
        letterCurrentTime = System.currentTimeMillis();
        if(letterCurrentTime - letterStartTime >=1500){
            letterStartTime = letterCurrentTime =0;
            lastPrediction = "";
            return prediction;
        }
        return "";
    }
    private boolean isWordEnd(String prediction){
        if(!(prediction.equals("0.0")) || translateText.equals("تظهر الترجمة هنا") || translateText.isEmpty()){
            wordCurrentTime = wordStartTime = System.currentTimeMillis();
            return false;
        }
        wordCurrentTime = System.currentTimeMillis();
        if(wordCurrentTime - wordStartTime >=5000){
            wordStartTime = wordCurrentTime =0;
            return true;
        }
        return false;
    }
    private String dictionary(String englishLetter){
        arabicLetters.put("alef", "ا");
        arabicLetters.put("BAa", "ب");
        arabicLetters.put("TAa", "ت");
        arabicLetters.put("tha", "ث");
        arabicLetters.put("jeem", "ج");
        arabicLetters.put("7ha", "ح");
        arabicLetters.put("kha", "خ");
        arabicLetters.put("Dal", "د");
        arabicLetters.put("thal", "ذ");
        arabicLetters.put("Ra", "ر");
        arabicLetters.put("Zay", "ز");
        arabicLetters.put("Sin", "س");
        arabicLetters.put("Shin", "ش");
        arabicLetters.put("Sad", "ص");
        arabicLetters.put("Dad", "ض");
        arabicLetters.put("Ta", "ط");
        arabicLetters.put("Za", "ظ");
        arabicLetters.put("Ain", "ع");
        arabicLetters.put("Ghin", "غ");
        arabicLetters.put("Fa", "ف");
        arabicLetters.put("Qaf", "ق");
        arabicLetters.put("Lam", "ل");
        arabicLetters.put("Kaf", "ك");
        arabicLetters.put("Mem", "م");
        arabicLetters.put("Noon", "ن");
        arabicLetters.put("Haa", "ه");
        arabicLetters.put("Waw", "و");
        arabicLetters.put("ya_maksorh", "ى");
        arabicLetters.put("Taa_marbotah", "ة");
        arabicLetters.put("Al", "ا");
        arabicLetters.put("La", "ل");
        arabicLetters.put("Yaa", "ي");
        return arabicLetters.get(englishLetter);
    }
}