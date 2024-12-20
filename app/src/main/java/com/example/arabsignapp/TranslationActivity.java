package com.example.arabsignapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;


public class TranslationActivity extends AppCompatActivity {

    AlertDialog dialog;
    private ActivityResultLauncher<String> requestPermission;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageAnalysis imageAnalysis;
    Camera cam;
    Handler handler;
    Runnable runs;
    TextView translateView;
    private String translateText = "";
    private Socket socket;
    private String serverName = "0.tcp.in.ngrok.io";
    private int serverPort = 13560;
    private DataOutputStream outStream;
    private BufferedReader inStream;
    private Gson gson;
    private boolean newText;
    ExecutorService singleThreadEx;
    private Bitmap bitmapImg;
    LinkedHashMap<String,Object> dataMap;
    ByteArrayOutputStream baos;
    private boolean arabic_mode,lastSocketCallFinished=true;
    private long letterStartTime = System.currentTimeMillis();;
    private long letterCurrentTime;
    private long wordStartTime;
    private long wordCurrentTime;
    private String lastPrediction = null;
    private ArrayList<Double> wordAccuracy = new ArrayList<>();
    private Session session;
    private boolean previouslyTranslated = false;
    private final LinkedHashMap<String,String> arabicLetters = new LinkedHashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        newText = false;
        String sessionText = getIntent().getStringExtra("chatName");
        String selectedLanguage = getIntent().getStringExtra("selectedLanguage");
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        session = new Session(sessionText,new ArrayList<>(),userRef);
        initDictionary();
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
        if (!translateText.isEmpty()){
            translateText=translateText.substring(0,translateText.length()-1);
            double averageAccuracy = calculateWordAccuracy(wordAccuracy);
            double roundedAverageAccuracy=Math.round((averageAccuracy)*10000.0)/100.0;
            wordAccuracy.clear();
            String arabicAccuracy = convertAccuracyToArabic(String.valueOf(roundedAverageAccuracy));
            translateText = translateText.replace(" ","");

            if (!arabic_mode){
                try {
                    Executors.newSingleThreadExecutor().submit(() ->
                            translateToArabic(translateText.replace(" ",""), arabicAccuracy)).get();
                }
                catch (Exception e){
                    return;
                }
            }
            else {
                Word word = new Word(translateText, arabicAccuracy);
                session.getSentence().add(word);
            }
        }
        dialog.dismiss();
            FirebaseFirestore fsdb = FirebaseFirestore.getInstance();
            fsdb.collection("history")
                    .add(session)
                    .addOnSuccessListener(documentReference -> {
                        finish();
                        Intent intent = new Intent(this,UserMainActivity.class);
                        startActivity(intent);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(),"حدث خطأ في حفظ الجلسة",
                                Toast.LENGTH_SHORT).show();
                    });

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

            cam = camProcessProvider.bindToLifecycle(this,camSelector,camPreview,imageAnalysis);

            findViewById(R.id.rotateCamButton).setOnClickListener(v -> {
                int facing = cam.getCameraInfo().getLensFacing();
                CameraSelector cameraSelector = cam.getCameraInfo().getCameraSelector();
                if (facing==CameraSelector.LENS_FACING_FRONT){
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }
                else if (facing==CameraSelector.LENS_FACING_BACK){
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                }
                camProcessProvider.unbindAll();
                cam = camProcessProvider.bindToLifecycle(this,cameraSelector,camPreview,imageAnalysis);
            });

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
        //send frame to server here
        try {
            if (socket==null||outStream==null||inStream==null){
                image.close();
                return;
            }

            //capture current frame
            bitmapImg = image.toBitmap();
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

            String prediction = String.valueOf(dataMap.get("prediction"));
            String prediction_proba = String.valueOf(dataMap.get("prediction_proba"));
//            RemoveLetter(prediction, prediction_proba);

            dataMap.clear();
            if (prediction.equals("0.0")){
                translateView.setTextColor(getResources().getColor(R.color.dialogborder, null));
            }
            else{
                if (arabic_mode){
                    prediction = getArabicLetter(prediction);
                }
                String letter=checkLetter(prediction,prediction_proba);
                translateText += letter;
                translateView.setTextColor(getResources().getColor(R.color.green,null));
            }
            boolean condition = isWordEnd(prediction);
            Log.d("CONDITION", String.valueOf(condition));
            if (condition){
                translateText=translateText.substring(0,translateText.length()-1);
                double averageAccuracy = calculateWordAccuracy(wordAccuracy);
                double roundedAverageAccuracy=Math.round((averageAccuracy)*10000.0)/100.0;
                wordAccuracy.clear();
                String arabicAccuracy = convertAccuracyToArabic(String.valueOf(roundedAverageAccuracy));
                if (!previouslyTranslated) {
                    previouslyTranslated = true;
                    translateText = translateText.replace(" ","");
                    if (!arabic_mode) {
                        Executors.newSingleThreadExecutor().submit(() -> translateToArabic(
                                translateText, arabicAccuracy)).get();
                    } else {
                        previouslyTranslated = false;
                        Word word = new Word(translateText, arabicAccuracy);
                        session.getSentence().add(word);
                        translateText = "";
                    }
                }

                Log.d("SENTENCENEW", session.getSentence().toString());
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

            socket.connect(new InetSocketAddress(serverName,serverPort));
            outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            inStream = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(socket.getInputStream())));
            dataMap = new LinkedHashMap<>();
            baos = new ByteArrayOutputStream();
        }
        catch (Exception e){
            socket = null;
            translateView.setText("فشل الاتصال بالترجمة");
        }
    }

    private String checkLetter(String prediction,String prediction_proba){
        double accuracy =Double.parseDouble(prediction_proba);
        if (accuracy<0.5){
            return "";
        }
        if (!prediction.equals(lastPrediction)) {
            letterStartTime = System.currentTimeMillis();
            lastPrediction = prediction;
        }
        letterCurrentTime = System.currentTimeMillis();
        if(letterCurrentTime - letterStartTime >= 1500){
            letterStartTime = letterCurrentTime =0;
            lastPrediction = "";
            wordAccuracy.add(accuracy);
            return prediction + " ";
        }
        return "";
    }
    private boolean isWordEnd(String prediction){
        if(!(prediction.equals("0.0"))  || translateText.isEmpty()){
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

    private void initDictionary(){
        arabicLetters.put("alef", "ا");
        arabicLetters.put("baA", "ب");
        arabicLetters.put("ta", "ت");
        arabicLetters.put("tha", "ث");
        arabicLetters.put("jeem", "ج");
        arabicLetters.put("ha", "ح");
        arabicLetters.put("kha", "خ");
        arabicLetters.put("dhal", "ذ");
        arabicLetters.put("ra", "ر");
        arabicLetters.put("zay", "ز");
        arabicLetters.put("sin", "س");
        arabicLetters.put("shin", "ش");
        arabicLetters.put("sad", "ص");
        arabicLetters.put("dad", "ض");
        arabicLetters.put("tta", "ط");
        arabicLetters.put("za", "ظ");
        arabicLetters.put("ayn", "ع");
        arabicLetters.put("ghayn", "غ");
        arabicLetters.put("qaf", "ق");
        arabicLetters.put("kaf", "ك");
        arabicLetters.put("lam", "ل");
        arabicLetters.put("mem", "م");
        arabicLetters.put("non", "ن");
        arabicLetters.put("haA", "ه");
        arabicLetters.put("wow", "و");
        arabicLetters.put("yaA", "ي");
        arabicLetters.put("Fa", "ف");
        arabicLetters.put("dal", "د");
        arabicLetters.put("ya_maqsora", "ى");
        arabicLetters.put("ta_marbotah", "ة");
        arabicLetters.put("al", "ال");
        arabicLetters.put("la", "لا");
    }

    private String getArabicLetter(String englishLetter){
        return arabicLetters.get(englishLetter);
    }


    private void RemoveLetter(String prediction, String predictionProba) {
        double accuracy = Double.parseDouble(predictionProba);

        if (accuracy < 0.5 && accuracy != 0) {
            //Check for text in translation
            if (!translateText.isEmpty()) {
                //Delete the last letter using substring
                translateText = translateText.substring(0, translateText.length());
            }
        }

    }

    private String convertAccuracyToArabic(String numString) {
        try{
            Double.parseDouble(numString);
        }
        catch (NumberFormatException e){
            return numString;
        }

        String arabicNumbers = "٠١٢٣٤٥٦٧٨٩";
        StringBuilder stringBuilder = new StringBuilder();

        for (int i=0;i<numString.length();i++){
            if (Character.isDigit(numString.charAt(i))){
                stringBuilder.append(arabicNumbers.charAt((int)(numString.charAt(i))-48));
            }
            else{
                stringBuilder.append(numString.charAt(i));
            }
        }

        return stringBuilder.toString();

    }

    private double calculateWordAccuracy(ArrayList<Double> accuracies) {
        if (accuracies.isEmpty()) {
            return 0.0;
        }

        double totalAccuracy = 0.0;

        int i = 0;
        while (i < accuracies.size()) {
            totalAccuracy += accuracies.get(i);
            i++;
        }

        return totalAccuracy / accuracies.size();
    }




    private void translateToArabic(String englishWord,String arabicAccuracy) {
        String apiKey = "";
        String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;

// the format
        String jsonBody = "{ \"q\": \"" + englishWord + "\", \"source\": \"en\", \"target\": \"ar\", \"format\": \"text\" }";
// the client itself(us)
        OkHttpClient client = new OkHttpClient();
        //the req
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
       //declare the req
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                //read the req and make it string
                String responseBody = response.body().string();
                //extract the string
                String translatedText;
                try {
                    JSONObject jsonObject = new JSONObject(responseBody);

                    translatedText = jsonObject.getJSONObject("data")
                            .getJSONArray("translations")
                            .getJSONObject(0)
                            .getString("translatedText");
                } catch (Exception e) {
                    e.printStackTrace();
                    translatedText = "Error in translation";
                }
                Log.d("TRANSLATIONRESULT", translatedText);

                translateText = translatedText;

                Word word = new Word(translateText, arabicAccuracy);
                session.getSentence().add(word);

//                runOnUiThread(() -> {
//                    translateView.setText(translateText);
//                });


                runOnUiThread(() -> new CountDownTimer(2000, 500) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        previouslyTranslated = false;
                        translateText = "";
                    }
                }.start());
            } else if (response.code() > 400) {
                Word word = new Word(translateText, arabicAccuracy);
                session.getSentence().add(word);
            } else {
                Log.d("OKERROR", response.body().string());
            }

        }
        catch (Exception e){

        }

    }
}