package com.example.Handwriting_System;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.MappedByteBuffer;

import static android.content.ContentValues.TAG;

public class LetterRecognition extends AppCompatActivity implements View.OnClickListener {

    private Button[] identify, judge;
    private ImageView[] imageViews;
    private TextView[] textViews;
    private TextView txv_scale;
    private SeekBar seekBars;

    private int inputHigh, inputWidth, outputHigh, outputWidth;
    private Boolean isInitialized = false;

    private Interpreter tflite = null;
    private static final String MODEL_FILE = "tf.tflite";
    private float[][] inputFloats;
    private float[][] outFloats;

    private SubLetter[] subLetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.letter_recognition);

        initializeInterpreter();

        final LetterData myData = (LetterData) getApplicationContext();
        int[][] FPoint = myData.getFPoint();
        int[][] LetterMatrix = myData.getLetterMatrix();


        identify = new Button[FPoint.length];
        judge = new Button[FPoint.length];
        imageViews = new ImageView[FPoint.length];
        textViews = new TextView[FPoint.length];
        txv_scale = new TextView(this);
        seekBars = new SeekBar(this);
        seekBars.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txv_scale.setText(String.format("Scale Size : %.2f",(0.3+(float)seekBars.getProgress()/100)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(LetterRecognition.this,String.format("Scale Size : %.2f",(0.3+(float)seekBars.getProgress()/100)),Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout linearLayout = findViewById(R.id.letter_regonition);
        final float scale = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams Image_layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        Image_layoutParams.bottomMargin = 20;
        Image_layoutParams.width = (int) (300 * scale);
        Image_layoutParams.height = (int) (300 * scale);
        Image_layoutParams.gravity = Gravity.CENTER;
        LinearLayout.LayoutParams Button_layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        Button_layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        Button_layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams TextView_layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        TextView_layoutParams.height = (int) (300 * scale);
        LinearLayout.LayoutParams SeekBars_layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        txv_scale.setText("Scale Size : 0.48");
        txv_scale.setTextSize(12);
        linearLayout.addView(txv_scale,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        seekBars.setMax(40);
        seekBars.setProgress(18);
        linearLayout.addView(seekBars,SeekBars_layoutParams);
        for (int i = 0; i < FPoint.length; i++) {
            identify[i] = new Button(this);
            judge[i] = new Button(this);
            imageViews[i] = new ImageView(this);
            textViews[i] = new TextView(this);

            linearLayout.addView(imageViews[i], Image_layoutParams);
            identify[i].setId(i);
            identify[i].setOnClickListener(this);
            identify[i].setText("辨識");
            identify[i].setTextSize(16);
            linearLayout.addView(identify[i], Button_layoutParams);
            judge[i].setId(FPoint.length + i);
            judge[i].setOnClickListener(this);
            judge[i].setText("判斷");
            judge[i].setTextSize(16);
            linearLayout.addView(judge[i], Button_layoutParams);
            textViews[i].setMovementMethod(new ScrollingMovementMethod());
            linearLayout.addView(textViews[i], TextView_layoutParams);
        }


        subLetter = new SubLetter[FPoint.length];
        //by using original data
        for (int l = 0; l < FPoint.length; l++) {
            float[][] subletter = new float[FPoint[l][3] - FPoint[l][2]][FPoint[l][1] - FPoint[l][0]];
//            for (int i = 0; i < Math.min(FPoint[l][3] - FPoint[l][2], inputHigh); i++) {
//                for (int j = 0; j < Math.min(FPoint[l][1] - FPoint[l][0], inputWidth); j++) {
//                    inputFloats[l][Math.max((inputHigh - (FPoint[l][3] - FPoint[l][2])) / 2, 0) + i][Math.max((inputWidth - (FPoint[l][1] - FPoint[l][0])) / 2, 0) + j] = (float) LetterMatrix[i + Math.max(FPoint[l][2], FPoint[l][2] + (FPoint[l][3] - FPoint[l][2] - inputHigh) / 2)][j + Math.max(FPoint[l][0], FPoint[l][0] + (FPoint[l][1] - FPoint[l][0] - inputWidth) / 2)];
//                }
//            }
            for (int i = 0; i < subletter.length; i++) {
                for (int j = 0; j < subletter[0].length; j++) {
                    subletter[i][j] = (float) LetterMatrix[i + FPoint[l][2]][j + FPoint[l][0]];
                }
            }
            subLetter[l] = new SubLetter(subletter, FPoint[l]);
        }


        for (int i = 0; i < FPoint.length; i++) {
            textViews[i].setText(textViews[i].getText() + "\nCenter：\nx=" + String.format("%.4f", subLetter[i].getCPoint()[0][0]) + "\t\ty=" + String.format("%.4f", subLetter[i].getCPoint()[0][1]) + "\n");//
            for (int j = 1; j < 10; j++) {
                textViews[i].setText(textViews[i].getText() + "\n" + "重心" + j + "：" + String.format("%.4f", subLetter[i].getCPoint()[j][0]) + "∠" + String.format("%.4f", subLetter[i].getCPoint()[j][1] * 180 / Math.PI));//
            }
            imageViews[i].setImageBitmap(subLetter[i].getLetterBitmap());
        }

    }

    public void onClick(View view) {

        LetterData myData = (LetterData) getApplicationContext();

        if ((view.getId()) < textViews.length) {

            inputFloats = new float[inputHigh][inputWidth];
            outFloats = new float[outputHigh][outputWidth];


            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale((float) (0.3+(float)seekBars.getProgress()/100), (float) (0.3+(float)seekBars.getProgress()/100));
//            matrix.postScale((float) 0.42, (float) 0.42);

            Bitmap resizedBitmap = Bitmap.createBitmap(subLetter[view.getId()].getLetterBitmap(), 0, 0, subLetter[view.getId()].getLetterBitmap().getWidth(), subLetter[view.getId()].getLetterBitmap().getHeight(), matrix, false);
            int width = resizedBitmap.getWidth();
            int high = resizedBitmap.getHeight();
            for (int i = 0; i < high; i++) {
                for (int j = 0; j < width; j++) {
                    if (Color.red(resizedBitmap.getPixel(j, i)) > 150) {
                        inputFloats[Math.round((64 - high) / 2) + i][Math.round((64 - width) / 2) + j] = 0;
                    } else {
                        inputFloats[Math.round((64 - high) / 2) + i][Math.round((64 - width) / 2) + j] = 1;
                    }
                }
            }

            if (tflite != null) {
                tflite.run(inputFloats, outFloats);
            }
            float max = Math.max(outFloats[0][0], Math.max(outFloats[0][1], outFloats[0][2]));
            textViews[view.getId()].setText("陳：" + String.format("%.4f", outFloats[0][0]) + "\n" + "施：" + String.format("%.4f", outFloats[0][1]) + "\n" + "劉：" + String.format("%.4f", outFloats[0][2]) + "\n" + textViews[view.getId()].getText()+"\n");

            if (max == outFloats[0][0])
                myData.setLetterType(0);//陳
            else if (max == outFloats[0][1])
                myData.setLetterType(1);//施
            else if (max == outFloats[0][2])
                myData.setLetterType(2);//劉
        } else {
            myData.setSubLetter(subLetter[view.getId() - textViews.length]);
            Intent intent = new Intent(LetterRecognition.this, Feedback.class);
            startActivity(intent);
        }
    }


    private void initializeInterpreter() {
        Interpreter.Options options = new Interpreter.Options();
        options.setUseNNAPI(true);
        try {
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, MODEL_FILE);
            tflite = new Interpreter(tfliteModel, options);
        } catch (IOException e) {
            Toast.makeText(LetterRecognition.this, "modeul loaded fail", Toast.LENGTH_LONG).show();
            Log.e("tfliteException", "Error: couldn't load tflite model.", e);
        }
        int[] inputShape = tflite.getInputTensor(0).shape();
        int[] outputShape = tflite.getOutputTensor(0).shape();
        DataType in = tflite.getInputTensor(0).dataType();
        DataType out = tflite.getOutputTensor(0).dataType();
        inputHigh = inputShape[0];
        inputWidth = inputShape[1];
        outputHigh = outputShape[0];
        outputWidth = outputShape[1];

        isInitialized = true;
        Toast.makeText(LetterRecognition.this, "modeul loaded successful", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Initialized TFLite interpreter.");
    }


}