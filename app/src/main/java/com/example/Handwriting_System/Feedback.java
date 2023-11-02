package com.example.Handwriting_System;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Float.NaN;

public class Feedback extends AppCompatActivity {

    private SubLetter subLetter;
    private double[][] Vertical, Horizontal, LetterCharacter;

    private ImageView imv_feedback, imv_letter;
    private TextView txv_problem, txv_titleProblem;
    private TextView txv_suggest,txv_dim;

    private int letter_case;//{fall left , fall right}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);

        imv_letter = findViewById(R.id.imv_letter);
        imv_feedback = findViewById(R.id.imv_feedback);
        txv_problem = findViewById(R.id.txv_problem);
        txv_suggest = findViewById(R.id.txv_suggest);
        txv_titleProblem = findViewById(R.id.txv_titleProblem);
        txv_dim = findViewById(R.id.txv_dim);

        LetterData myData = (LetterData) getApplicationContext();

        subLetter = myData.getSubLetter();
        Vertical = subLetter.getVertical();
        Horizontal = subLetter.getHorizontal();
        LetterCharacter = myData.getLetterCharacter();

        Bitmap letterMap = subLetter.getLetterBitmap();
        Canvas canvas = new Canvas(letterMap);
        Paint paint = new Paint();
        int Width = subLetter.getWidth();
        int High = subLetter.getHigh();
        paint.setColor(Color.BLUE);
        for (int i = 1; i < LetterCharacter.length; i++) {
            double x = LetterCharacter[i][0] * Math.cos(LetterCharacter[i][1] * Math.PI / 180);
            double y = LetterCharacter[i][0] * Math.sin(LetterCharacter[i][1] * Math.PI / 180);
            float size = 1;
            canvas.drawOval((float) (x * Width + Width / 2) - size, (float) (-y * High + High / 2) - size, (float) (x * Width + Width / 2) + size, (float) (-y * High + High / 2) + size, paint);
        }
        imv_letter.setImageBitmap(letterMap);
//        txv_dim.setText("Width：High = 1：" + (float)High/Width);
        txv_dim.setText("Width："+Width+"\t\tHigh = " + High);


        double angle_H[] = new double[3];
        double angle_V[] = new double[3];

        for (int i = 0; i < 3; i++) {
            angle_H[i] = (Math.atan(Horizontal[i][0]) * 180 / Math.PI + 180) % 180;
            angle_V[i] = (Math.atan(Vertical[i][0]) * 180 / Math.PI + 180) % 180;
        }

        double index = 5, max = 0, Point = 20;
        for (int i = 1; i < 10; i++) {
            txv_suggest.setText(txv_suggest.getText() + "重心點" + i + "\n" + String.format("%.4f",subLetter.getCPoint()[i][0])  + "∠" + String.format("%.4f",subLetter.getCPoint()[i][1] * 180 / 3.14) + "\n");
            txv_suggest.setText(txv_suggest.getText() + "" + String.format("%.4f",LetterCharacter[i][0]) + "∠" + String.format("%.4f",LetterCharacter[i][1]) + "\n");
            if (i == 5) continue;//中心不列入判斷
            double dist = distance(subLetter.getCPoint()[i], LetterCharacter[i]);
            txv_suggest.setText(txv_suggest.getText() + "誤差量:" + String.format("%.5f",dist) + "\n\n");
            Point += (10 - 60 * dist);
            if (dist > 0.07) {
                txv_titleProblem.setText(txv_titleProblem.getText() + " " + i);
                if (dist > max) {
                    max = dist;
                    index = i;
                }
            }
        }

        double angle = (angle_V[0] + angle_V[1] + angle_V[2]) - 270;

        Point -= Math.abs(angle) * 1.3;

        if (subLetter.getProblem_flag()[0]){
            txv_titleProblem.setText("字跡模糊，注意間距或放大字跡");
            Point=NaN;
            index=NaN;
        }else if(subLetter.getProblem_flag()[1]){
            txv_titleProblem.setText("字跡結構非方正，注意間距或傾斜");
            Point=NaN;
            index=NaN;
        }
        txv_problem.setText("系統評分：" + Point + "\n");
        if(index!=5)
            txv_problem.setText(txv_problem.getText() + "最嚴重部份：" + index + "\n");

        int degree = 4;
        if (degree > angle && angle > -degree) txv_problem.setText(txv_problem.getText() + "\n無傾斜");//無左右倒
        else if (angle > degree) txv_problem.setText(txv_problem.getText() + "\n左倒");//左倒
        else if (-degree > angle) txv_problem.setText(txv_problem.getText() + "\n右倒");//右倒


        if (txv_titleProblem.getText().equals("問題：")  && Point > 80)
            txv_titleProblem.setText("問題：字跡已達工整");

//        switch (letter_case) {
//            case 0: {
//                txv_problem.setText(txv_problem.getText() + "\n無傾斜");
//                break;
//            }
//            case 1: {
//                txv_problem.setText(txv_problem.getText() + "\n左倒");
////                imv_feedback.setImageResource(R.drawable.feedback_left);
////                txv_suggest.setText("妳需要準備像上圖一樣的格子，並在上面寫上你的字，並嘗試將你的字置中在格子內\n" +
////                        "因為您的文字向右傾斜，本系統建議您可以按照左斜的格子練習：夕、勿、刁、力" +
////                        "等等的左斜文字來加以矯正您的書寫習慣\n");
//
//                break;
//            }
//            case 2: {
//                txv_problem.setText(txv_problem.getText() + "\n右倒");
////                imv_feedback.setImageResource(R.drawable.feedback_right);
////                txv_suggest.setText("妳需要準備像上圖一樣的格子，並在上面寫上你的字，並嘗試將你的字置中在格子內\n" +
////                        "因為您的文字向左傾斜，本系統建議您可以按照右斜的格子練習：或、戒、弋、曳" +
////                        "等等的右斜文字來加以矯正您的書寫習慣\n");
//
//                break;
//            }
//            default:
//                break;
//        }


    }


    private double distance(double[] P1, double[] P2) {
        return Math.sqrt(Math.pow(P1[0], 2) + Math.pow(P2[0], 2) - 2 * P1[0] * P2[0] * Math.cos(P1[1] - P2[1] * Math.PI / 180));
    }
}
