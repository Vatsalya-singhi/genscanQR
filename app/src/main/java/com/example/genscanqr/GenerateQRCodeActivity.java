package com.example.genscanqr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GenerateQRCodeActivity extends AppCompatActivity {

    private TextView qrCodeTV;
    private ImageView qrCodeIV;
    private TextInputEditText dataEdt;
    private Button generateQRBtn;
    private Bitmap bitmap;

    private ImageButton mic;
    private SpeechRecognizer speechRecognizer;
    private boolean micFlag = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);
        qrCodeTV = findViewById(R.id.idTVGenerateQR);
        qrCodeIV = findViewById(R.id.idIVQRCode);
        dataEdt = findViewById(R.id.idEdtData);
        generateQRBtn = findViewById(R.id.idBtnGenerateQR);

        mic = findViewById(R.id.mic);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        Context context = this;

        // check mic permission
        micRecordPermission(context);

        final Intent speechRecogniserIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(micFlag) {
                    mic.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_24));
                    // start listening
                    speechRecognizer.startListening(speechRecogniserIntent);
                } else {
                    mic.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                    // stop listening
                    speechRecognizer.stopListening();
                }

                micFlag = !micFlag;
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Toast.makeText(context, "Listening now..", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(context, "Synthesising speech now..", Toast.LENGTH_SHORT).show();
                mic.setImageDrawable(getDrawable(R.drawable.ic_baseline_mic_off_24));
                // stop listening
                speechRecognizer.stopListening();
                micFlag = false;
            }

            @Override
            public void onError(int i) {
                Toast.makeText(context, "Some problem occurred", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                dataEdt.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                dataEdt.setText(data.get(0));
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        generateQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String data = dataEdt.getText().toString();
                if (data.isEmpty()) {
                    Toast.makeText(GenerateQRCodeActivity.this, "Please enter some data to generate QR Code", Toast.LENGTH_SHORT).show();
                }else {
                    WindowManager manager = (WindowManager) getSystemService (WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int dimen = (width < height) ? width: height;
                    dimen = 3/4 * dimen;

                    QRGEncoder qrgEncoder = new QRGEncoder(dataEdt.getText().toString(), null, QRGContents.Type.TEXT,dimen);
                    try {
                        //bitmap = qrgEncoder.encodeAsBitmap();
                        bitmap = qrgEncoder.getBitmap();
                        qrCodeTV.setVisibility(View.GONE);
                        qrCodeIV.setImageBitmap(bitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });


        qrCodeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Share to Social Media
                shareImage(bitmap);

//                Drawable mDrawable = qrCodeIV.getDrawable();
//                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                        bitmap, "Design", null);
//                Uri uri = Uri.parse(path);
//                Intent share = new Intent(Intent.ACTION_SEND);
//                share.setType("image");
//                share.putExtra(Intent.EXTRA_STREAM, uri);
//                share.putExtra(Intent.EXTRA_TEXT, "I found something cool!");
//                context.startActivity(Intent.createChooser(share, "Share Your Design!"));
            }
        });
    }

    private void shareImage(Bitmap bitmap) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        Uri uri;
        String textToShare = "I found something cool!";
        uri = saveImage(bitmap,getApplicationContext());
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(share,"Share Content"));
    }

    private static Uri saveImage(Bitmap bitmap, Context applicationContext) {
        File imageFolder = new File(applicationContext.getCacheDir(),"images");
        Uri uri = null;
        try{
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_images.jpg");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(Objects.requireNonNull(applicationContext.getApplicationContext()),
                    "com.example.genscanqr"+ ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uri;
    }

    private void micRecordPermission(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}