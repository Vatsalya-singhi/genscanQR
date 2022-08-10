package com.example.genscanqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GenerateQRCodeActivity extends AppCompatActivity {

    private TextView qrCodeTV;
    private ImageView qrCodeIV;
    private TextInputEditText dataEdt;
    private Button generateQRBtn;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);
        qrCodeTV = findViewById(R.id.idTVGenerateQR);
        qrCodeIV = findViewById(R.id.idIVQRCode);
        dataEdt = findViewById(R.id.idEdtData);
        generateQRBtn = findViewById(R.id.idBtnGenerateQR);
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
                    int dimen = width<height ? width: height;
                    dimen = dimen* 3/4;

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

        Context context = this;

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

}