package com.example.Handwriting_System;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowLetter extends AppCompatActivity {

    private TextView ShowDim;
    private ImageView imv_Letter;
    private Button btn_convert, btn_Recongnition;

    private int[][] LetterMatrix, FPoint;
    private Bitmap LetterMap;
    private LetterConvert myLetter;

    private String LetterString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_letter);

        final LetterData myData = (LetterData) getApplicationContext();
        myLetter = new LetterConvert(myData.getLetterBitmap(), myData.getLetterMatrix(), myData.getHigh(), myData.getWidth());

        ShowDim = findViewById(R.id.ShowDim);
        imv_Letter = findViewById(R.id.imv_Letter1);
        imv_Letter.setImageBitmap(myLetter.ReturnBitmap());
        ShowDim.setText("High : " + myLetter.ReturnHigh() + "\nWidth : " + myLetter.ReturnWidth());

        btn_convert = findViewById(R.id.btn_convert);
        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myLetter.StartConverting();
                myData.setLetterBitmap(myLetter.ReturnBitmap());
                myData.setLetterMatrix(myLetter.ReturnMatrix());
                myData.setFPoint(myLetter.ReturnFPoint());
                imv_Letter.setImageBitmap(myLetter.ReturnBitmap());
                LetterMap = myLetter.ReturnBitmap();
                FPoint = myLetter.ReturnFPoint();
                showFrame();
                btn_Recongnition.setEnabled(true);
                btn_convert.setEnabled(false);
            }
        });

        btn_Recongnition = findViewById(R.id.btn_Recongnition);
        btn_Recongnition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowLetter.this, LetterRecognition.class);
                startActivity(intent);
            }
        });

    }


    private void showFrame() {
        Canvas canvas = new Canvas(LetterMap);
        Paint paint = new Paint();

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        for (int i = 0; i < FPoint.length; i++) {
            canvas.drawRect(FPoint[i][0], FPoint[i][2], FPoint[i][1], FPoint[i][3], paint);
        }
        imv_Letter.setImageBitmap(LetterMap);
    }

}
