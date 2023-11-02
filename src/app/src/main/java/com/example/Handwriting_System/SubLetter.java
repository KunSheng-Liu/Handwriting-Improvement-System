package com.example.Handwriting_System;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class SubLetter {

    private Bitmap LetterBitmap;
    private int High, Width;
    private float[][] LetterMatrix;
    private int[] FPoint;
    private double[][] CPoint = new double[10][2];//[radius][angle] angle:0~2pi
    private double[][] Horizontal = new double[3][2];
    private double[][] Vertical = new double[3][2];
    private Boolean[] Problem_flag={false,false};//0：blurred,1：NaN

    public Boolean[] getProblem_flag() {
        return Problem_flag;
    }

    public SubLetter(float[][] Letter, int[] FPoint) {
        LetterMatrix = Letter;
        this.FPoint = FPoint;
        High = LetterMatrix.length;
        Width = LetterMatrix[0].length;
        LetterBitmap = Bitmap.createBitmap(Width, High, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < High; i++) {
            for (int j = 0; j < Width; j++) {
                if (LetterMatrix[i][j] == 0) {
                    LetterBitmap.setPixel(j, i, Color.WHITE);
                } else {
                    LetterBitmap.setPixel(j, i, Color.BLACK);
                }
            }
        }//set Bitmap

        int[][] high = new int[3][2], width = new int[3][2];
        switch (High % 3) {
            case 0:
                high[0] = new int[]{0, Math.round(High / 3) - 1};
                high[1] = new int[]{Math.round(High / 3), 2 * Math.round(High / 3) - 1};
                high[2] = new int[]{2 * Math.round(High / 3), 3 * Math.round(High / 3) - 1};
                break;
            case 1:
                high[0] = new int[]{0, Math.round(High / 3) - 1};
                high[1] = new int[]{Math.round(High / 3), 2 * Math.round(High / 3)};
                high[2] = new int[]{2 * Math.round(High / 3) + 1, 3 * Math.round(High / 3)};
                break;
            case 2:
                high[0] = new int[]{0, Math.round(High / 3)};
                high[1] = new int[]{Math.round(High / 3) + 1, 2 * Math.round(High / 3)};
                high[2] = new int[]{2 * Math.round(High / 3) + 1, 3 * Math.round(High / 3) + 1};
                break;

        }
        switch (Width % 3) {
            case 0:
                width[0] = new int[]{0, Math.round(Width / 3) - 1};
                width[1] = new int[]{Math.round(Width / 3), 2 * Math.round(Width / 3) - 1};
                width[2] = new int[]{2 * Math.round(Width / 3), 3 * Math.round(Width / 3) - 1};
                break;
            case 1:
                width[0] = new int[]{0, Math.round(Width / 3) - 1};
                width[1] = new int[]{Math.round(Width / 3), 2 * Math.round(Width / 3)};
                width[2] = new int[]{2 * Math.round(Width / 3) + 1, 3 * Math.round(Width / 3)};
                break;
            case 2:
                width[0] = new int[]{0, Math.round(Width / 3)};
                width[1] = new int[]{Math.round(Width / 3) + 1, 2 * Math.round(Width / 3)};
                width[2] = new int[]{2 * Math.round(Width / 3) + 1, 3 * Math.round(Width / 3) + 1};
                break;

        }

        CPoint[0] = CenterPoint(new int[]{0, High - 1}, new int[]{0, Width - 1});
        CPoint[0][0] /= Width;
        CPoint[0][1] /= High;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                CPoint[i * 3 + j + 1] = CenterPoint(high[i], width[j]);
                CPoint[i * 3 + j + 1][0] /= Width;//normallize x-axis
                CPoint[i * 3 + j + 1][1] /= High;//normallize y-axis
            }
        }
        for (int i = 1; i < 10; i++) {
            CPoint[i] = Coordinate_conversion(CPoint[i], CPoint[0]);
        }
//        least_square_caculate(new double[][]{CPoint[1],CPoint[2],CPoint[3]});
//        least_square_caculate(new double[][]{CPoint[4],CPoint[5],CPoint[6]});
//        least_square_caculate(new double[][]{CPoint[7],CPoint[8],CPoint[9]});
//        least_square_caculate(new double[][]{CPoint[1],CPoint[4],CPoint[7]});
//        least_square_caculate(new double[][]{CPoint[2],CPoint[5],CPoint[8]});
//        least_square_caculate(new double[][]{CPoint[3],CPoint[6],CPoint[9]});

        Horizontal[0]=ConnectionLine(new double[][]{CPoint[1],CPoint[2],CPoint[3]},"Horizontal");
        Horizontal[1]=ConnectionLine(new double[][]{CPoint[4],CPoint[5],CPoint[6]},"Horizontal");
        Horizontal[2]=ConnectionLine(new double[][]{CPoint[7],CPoint[8],CPoint[9]},"Horizontal");
        Vertical[0]=ConnectionLine(new double[][]{CPoint[1],CPoint[4],CPoint[7]},"Vertical");
        Vertical[1]=ConnectionLine(new double[][]{CPoint[2],CPoint[5],CPoint[8]},"Vertical");
        Vertical[2]=ConnectionLine(new double[][]{CPoint[3],CPoint[6],CPoint[9]},"Vertical");

        Canvas canvas = new Canvas(LetterBitmap);
        Paint paint = new Paint();

        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth((float) 1);

//顯示回歸線
        for (int i = 0; i < 3; i++) {
            canvas.drawLine((float) 0, (float) (CPoint[0][1]*High - (-Width / 2 * Horizontal[i][0] + High * Horizontal[i][1])), (float) Width, (float) (CPoint[0][1]*High - (Width / 2 * Horizontal[i][0] + High * Horizontal[i][1])), paint);

            canvas.drawLine((float) 0, (float) (CPoint[0][1]*High - (-Width / 2 * Vertical[i][0] + Width * Vertical[i][1])), (float) Width, (float) (CPoint[0][1]*High - (Width / 2 * Vertical[i][0] + Width * Vertical[i][1])), paint);
        }
        paint.setColor(Color.RED);
        for (int i = 1; i < CPoint.length; i++) {
            double x=CPoint[i][0]*Math.cos(CPoint[i][1]);
            double y=CPoint[i][0]*Math.sin(CPoint[i][1]);
            float size=1;
            canvas.drawOval((float) (x*Width+Width/2)-size,(float)(-y*High+High/2)-size,(float)(x*Width+Width/2)+size,(float)(-y*High+High/2)+size,paint);
        }
    }


    private double[] CenterPoint(int[] frame_y, int[] frame_x) {
        double x = 0, y = 0;
        int count = 0;

        for (int i = frame_y[0]; i < frame_y[1]; i++) {
            for (int j = frame_x[0]; j < frame_x[1]; j++) {
                if (LetterMatrix[i][j] == 1) {
                    y += i;
                    x += j;
                    count++;
                }
            }
        }
        x /= count;
        y /= count;

        if(count>(frame_y[0]-frame_y[1])*(frame_x[0]-frame_x[1])*0.9)Problem_flag[0]=true;//是否模糊
        else if(count==0)Problem_flag[1]=true;//NaN
//        if (count != 0) LetterBitmap.setPixel((int) x, (int) y, Color.GREEN);//標出重心點

        return new double[]{x, y};
    }

    private double[] Coordinate_conversion(double[] Point, double[] Center) {
        double radius, degree;

        radius = Math.sqrt(Math.pow(Point[0] - Center[0], 2) + Math.pow(Point[1] - Center[1], 2));
        if (Point[0] > Center[0])
            degree = Math.asin((Center[1] - Point[1]) / radius);//Taylor offset pi/2 need to plus 2
        else
            degree = Math.PI - Math.asin((Center[1] - Point[1]) / radius);//Taylor offset pi/2 need to plus 2
        return new double[]{radius, degree};
    }

    private double[]  ConnectionLine(double[][] CPoint,String type){
        double[] x=new double[CPoint.length];
        double[] y=new double[CPoint.length];
        double temp1=0,temp2=0;
        double[] ans=new double[2];

        for (int i = 0; i < CPoint.length; i++) {
            x[i]=CPoint[i][0]*Math.cos(CPoint[i][1]);
            y[i]=CPoint[i][0]*Math.sin(CPoint[i][1]);
            temp1+=x[i]/3;
            temp2+=y[i]/3;
        }

        ans[0]=(y[0]-y[2])/(x[0]-x[2]);
        ans[1]=(x[0]*y[2]-x[2]*y[0])/(x[0]-x[2]);

        return ans;
    }

    //ATAX=ATB
    private void least_square_caculate(double[][] CPoint) {
        double[] x=new double[CPoint.length];
        double[] y=new double[CPoint.length];
        double temp1=0,temp2=0,temp3=0;
        double[] ans=new double[2];//(ATA)^-1*ATB

        for (int i = 0; i < CPoint.length; i++) {
            x[i]=CPoint[i][0]*Math.cos(CPoint[i][1]);
            y[i]=CPoint[i][0]*Math.sin(CPoint[i][1]);
            temp1+=Math.pow(x[i],2);
            temp2+=x[i];
        }
        temp3=temp1*CPoint.length-Math.pow(temp2,2);//det(ATA)

        ans[0]=((x[0]*CPoint.length-temp2)*y[0]+(x[1]*CPoint.length-temp2)*y[1]+(x[2]*CPoint.length-temp2)*y[2])/temp3;
        ans[1]=((temp1-x[0]*temp2)*y[0]+(temp1-x[1]*temp2)*y[1]+(temp1-x[2]*temp2)*y[2])/temp3;

        Canvas canvas = new Canvas(LetterBitmap);
        Paint paint = new Paint();

        paint.setColor(Color.YELLOW);
        paint.setStrokeWidth((float) 1);

//        canvas.drawLine((float)0,(float)(High/2-(-Width/2*ans[0]+Width*ans[1])),(float)Width,(float)(High/2-(Width/2*ans[0]+Width*ans[1])),paint);

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth((float) 1);
        for (int i = 0; i < CPoint.length; i++) {
            float size=5;
            canvas.drawOval((float) (x[i]*Width+Width/2)-size,(float)(y[i]*High+High/2)-size,(float)(x[i]*Width+Width/2)+size,(float)(y[i]*High+High/2)+size,paint);
        }
    }

    public int getWidth() {
        return Width;
    }

    public int getHigh() {
        return High;
    }

    public double[][] getCPoint() {
        return CPoint;
    }

    public double[][] getHorizontal() {
        return Horizontal;
    }

    public double[][] getVertical() {
        return Vertical;
    }

    public float[][] getLetterMatrix() {
        return LetterMatrix;
    }

    public Bitmap getLetterBitmap() {
        return LetterBitmap;
    }
}
