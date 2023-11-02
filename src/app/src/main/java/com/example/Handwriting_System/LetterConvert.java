package com.example.Handwriting_System;

        import android.content.res.Resources;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;

        import java.util.Arrays;


public class LetterConvert {

    private Bitmap LetterBitmap;
    private int[][] LetterMatrix;
    private int[][] FPoint;
    private int High, Width;
    private int i, j;
    private int orientation;
    private Boolean done = false;

    public LetterConvert(Bitmap bitmap, int[][] matrix, int high, int width) {
        LetterBitmap = bitmap;
        LetterMatrix = matrix;
        High = high;
        Width = width;
    }

    public LetterConvert(Resources res, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inMutable = true;
        LetterBitmap = BitmapFactory.decodeResource(res, id, options);
        High = LetterBitmap.getHeight();
        Width = LetterBitmap.getWidth();
        for (int i = 0; i < High; i++) {
            LetterBitmap.setPixel(0,i,Color.WHITE);
            LetterBitmap.setPixel(Width-1,i,Color.WHITE);
        }
        for (int i = 0; i < Width; i++) {
            LetterBitmap.setPixel(i,0,Color.WHITE);
            LetterBitmap.setPixel(i,High-1,Color.WHITE);
        }
        BitmapToMatrix();
    }

//    public LetterConvert(String data) {
//
//        int length = data.length();
//        LetterMatrix = new int[length / 512][512];
//        for (int i = 1; i < length / 512-1; i++) {
//            for (int j = 1; j < 511; j++) {
//                LetterMatrix[i][j] = data.charAt(i * 512 + j) - 48;
//            }
//        }
//        High = length / 512;
//        Width = 512;
//        LetterBitmap = Bitmap.createBitmap(Width, High, Bitmap.Config.ARGB_8888);
//        MatrixToBitmap();
//    }

    public LetterConvert(String data) {

        int length = data.length();
        int count=0;
        LetterMatrix = new int[512][512];
        for (int i = 0; i < length; i++) {
            int temp=data.charAt(i);
            if(temp<128){
                temp-=32;
                if(temp>=32){
                    for(int j=0;j<temp-31;j++,count++){
                        if(count>=512*512)break;
                        if(count%512 == 0 || count%512 == 511 || count/512==0 || count/512==511)LetterMatrix[count/512][count%512]=0;
                        else LetterMatrix[count/512][count%512]=1;
                    }
                }else if(temp<32){
                    for(int j=0;j<temp+1;j++,count++){
                        if(count>=512*512)break;
                        LetterMatrix[count/512][count%512]=0;
                    }
                }
            }else break;
        }
        High = 512;
        Width = 512;
        LetterBitmap = Bitmap.createBitmap(Width, High, Bitmap.Config.ARGB_8888);
        MatrixToBitmap();
    }

    public Boolean ConvertFinish() {
        return done;
    }

//    public void setHigh(int high) {
//        High = high;
//    }
//
//    public void setWidth(int width) {
//        Width = width;
//    }
//
//    public void setLetterBitmap(Bitmap bitmap){
//        LetterBitmap = bitmap;
//    }
//
//    public void setLetterMatrix(int[][] matrix){
//        LetterMatrix = matrix;
//    }

    public int[][] ReturnMatrix() {
        return LetterMatrix;
    }

    public Bitmap ReturnBitmap() {
        return LetterBitmap;
    }

    public int[][] ReturnFPoint() {
        return FPoint;
    }

    public int ReturnWidth() {
        return Width;
    }

    public int ReturnHigh() {
        return High;
    }

    public int LengthOfBitmap() {
        return Width * High;
    }

    public void StartConverting() {
        done = false;

        MatrixToBitmap();
        LoG(3, 0.05);
        LetterFrame();
//        MatrixToBitmap();
        done = true;
    }

    private void BitmapToMatrix() {
        LetterMatrix = new int[High][Width];
        for (int i = 0; i < High; i++) {
            for (int j = 0; j < Width; j++) {
                if (Color.red(LetterBitmap.getPixel(j, i)) > 150) {
                    LetterMatrix[i][j] = 0;
                } else {
                    LetterMatrix[i][j] = 1;
                }
            }
        }
    }

    private void MatrixToBitmap() {
        Bitmap NewBitmap = Bitmap.createBitmap(Width, High, LetterBitmap.getConfig());
        for (int i = 0; i < High; i++) {
            for (int j = 0; j < Width; j++) {
                if (LetterMatrix[i][j] == 0) {
                    NewBitmap.setPixel(j, i, Color.WHITE);
                } else {
                    NewBitmap.setPixel(j, i, Color.BLACK);
                }
            }
        }
        LetterBitmap = NewBitmap;
    }

    private void LoG(int Mask_size, double sigma) {
        double[][] mask = new double[Mask_size][Mask_size];
        double count = 0;

        for (int i = 0; i < Mask_size; i++) {
            int x = i - (Mask_size - 1) / 2;
            for (int j = 0; j < Mask_size; j++) {
                int y = j - (Mask_size - 1) / 2;

                double temp = -(1 / (Math.PI * (Math.pow(sigma, 4)))) * (1 - (Math.pow(x, 2) + Math.pow(y, 2)) / (2 * Math.pow(sigma, 2))) * Math.exp(-(Math.pow(x, 2) + Math.pow(y, 2)) / (2 * Math.pow(sigma, 2)));
                mask[i][j] = temp;
                count += temp;
            }
        }
        count = count / (Mask_size * Mask_size);
        for (int i = 0; i < Mask_size; i++) {
            for (int j = 0; j < Mask_size; j++) {
                mask[i][j] -= count;
            }
        }
        Conv2d(mask);
    }

    private void Conv2d(double[][] mask) {
        int Ori_High = High;
        int Ori_Width = Width;
        int[][] NewMatrix = new int[Ori_High][Ori_Width];
        LetterMatrix = ResizeMatrix(LetterMatrix, Ori_High + (mask[0].length + 1) / 2, Ori_Width + (mask[1].length + 1) / 2);

        for (int i = 0; i < Ori_High; i++) {
            for (int j = 0; j < Ori_Width; j++) {
                for (int k = 0; k < mask[0].length; k++) {
                    for (int l = 0; l < mask[1].length; l++) {
                        NewMatrix[i][j] += LetterMatrix[i + 1 + k - (mask[0].length - 1) / 2][j + 1 + l - (mask[1].length - 1) / 2] * mask[k][l];
                    }
                }
            }
        }
        High = Ori_High;
        Width = Ori_Width;
        LetterMatrix = NewMatrix;
        MatrixToBitmap();
        BitmapToMatrix();
    }

    public int[][] ResizeMatrix(int[][] Matrix, int NewHigh, int NewWidth) {

        int[][] NewMatrix = new int[NewHigh][NewWidth];

        int HighDeviation = Math.round((NewHigh - High) / 2);//計算出實際高度與預期輸出高度的誤差
        int WidthDeviation = Math.round((NewWidth - Width) / 2);//計算出實際寬度與預期輸出寬度的誤差
        if (HighDeviation >= 0) {
            if (WidthDeviation >= 0) {
                for (int i = 0; i < High; i++) {
                    for (int j = 0; j < Width; j++) {
                        NewMatrix[HighDeviation + i][WidthDeviation + j] = Matrix[i][j];
                    }
                }
            } else {
                for (int i = 0; i < High; i++) {
                    for (int j = 0; j < NewWidth; j++) {
                        NewMatrix[HighDeviation + i][j] = Matrix[i][j - WidthDeviation];
                    }
                }
            }

        } else {
            if (WidthDeviation >= 0) {
                for (int i = 0; i < NewHigh; i++) {
                    for (int j = 0; j < Width; j++) {
                        NewMatrix[i][WidthDeviation + j] = Matrix[i - HighDeviation][j];
                    }
                }
            } else {
                for (int i = 0; i < NewHigh; i++) {
                    for (int j = 0; j < NewWidth; j++) {
                        NewMatrix[i][j] = Matrix[i - HighDeviation][j - WidthDeviation];
                    }
                }
            }
        }
        return NewMatrix;
    }

    private void SetInMatrix() {

        int Max, min;
        int[] ColumnBondle;


        ColumnBondle = MaxColumn();//取出出現最高點和最低點的列
        Max = ColumnScan(ColumnBondle[0])[0];//取出最高點的位置
        min = ColumnScan(ColumnBondle[1])[1];//取出最低點的位置

        int deviation = Math.round((High - (Max - min)) / 2 - min);//計算出目前圖片垂直高度與中心點的誤差

        int[][] NewMatrix = new int[High][Width];
        for (int i = 0; i < High - deviation; i++) {
            if (deviation >= 0) {
                NewMatrix[i + deviation] = LetterMatrix[i];
            } else {
                NewMatrix[High - i + deviation] = LetterMatrix[High - i];
            }
        }
        LetterMatrix = NewMatrix;

        int[] RowBondle;

        RowBondle = MaxRow();//取出出現最左側點和最右側點的行
        Max = RowScan(RowBondle[0])[0];//取出最左側點的位置
        min = RowScan(RowBondle[1])[1];//取出最右側點的位置

        deviation = Math.round((Width - (Max - min)) / 2 - min);//計算出目前圖片左右位置與中心點的誤差

        NewMatrix = new int[High][Width];

        for (int i = 0; i < High; i++) {
            for (int j = 0; j < Width - deviation; j++) {
                if (deviation >= 0) {
                    NewMatrix[i][j + deviation] = LetterMatrix[i][j];
                } else {
                    NewMatrix[i][Width - j + deviation] = LetterMatrix[i][Width - j];
                }
            }
        }
        LetterMatrix = NewMatrix;
        MatrixToBitmap();

    }

    private int[] MaxRow() {
        int min_temp = High;
        int Max_temp = 0;
        int minOfRow = 0, MaxOfRow = 0;
        int[] bondle;

        for (int i = 0; i < High; i++) {
            bondle = RowScan(i);

            if (Max_temp < bondle[0]) {
                Max_temp = bondle[0];
                MaxOfRow = i;
            }
            if (min_temp > bondle[1] && bondle[1] != 0) {
                min_temp = bondle[1];
                minOfRow = i;
            }
        }
        return new int[]{MaxOfRow, minOfRow};
    }

    private int[] MaxColumn() {
        int min_temp = Width;
        int Max_temp = 0;
        int MaxOfColumn = 0, minOfColumn = 0;
        int[] bondle;

        for (int i = 0; i < Width; i++) {
            bondle = ColumnScan(i);

            if (Max_temp < bondle[0]) {
                Max_temp = bondle[0];
                MaxOfColumn = i;
            }
            if (min_temp > bondle[1] && bondle[1] != 0) {
                min_temp = bondle[1];
                minOfColumn = i;
            }
        }
        return new int[]{MaxOfColumn, minOfColumn};
    }

    private int[] RowScan(int high) {
        int min = 0, Max = 0;
        for (int i = 0; i < Width; i++) {
            if (LetterMatrix[high][i] == 1 && min == 0) {
                min = i;
            }
            if (LetterMatrix[high][Width - i - 1] == 1 && Max == 0) {
                Max = Width - i;
            }
        }
        return new int[]{Max, min};
    }

    private int[] ColumnScan(int width) {
        int min = 0, Max = 0;
        for (int i = 0; i < High; i++) {
            if (LetterMatrix[i][width] == 1 && min == 0) {
                min = i;
            }
            if (LetterMatrix[High - i - 1][width] == 1 && Max == 0) {
                Max = High - i;
            }
        }
        return new int[]{Max, min};
    }

    private void LetterFrame() {
        FPoint = new int[512][4];
        int count = 0;
        Boolean en;

        for (int i = 0; i < LetterMatrix.length; i++) {
            for (int j = 0; j < LetterMatrix[0].length; j++) {
                en = Boolean.TRUE;
                for (int k = 0; k < count; k++) {
                    if ((j >= FPoint[k][0] && j <= FPoint[k][1]) && (i >= FPoint[k][2] && i <= FPoint[k][3])) {
                        en = Boolean.FALSE;
                    }
                }
                if (LetterMatrix[i][j] == 1 && en) {
                    FPoint[count] = findFrame(i, j); //[P_h,P_v]=doFrame(y,x,Matrix)
                    count = count + 1;
                }
            }
        }
        for (int i = count; i < LetterMatrix[0].length; i++) {
            FPoint[i] = null;
        }

        frameComb(count);
        Overlap_Frame();

    }

    private void Overlap_Frame() {
        int P_length = FPoint.length;

        //overlap data combin
        int count = P_length;
        for (int i = 0; i < P_length; i++) {
            for (int j = 0; j < P_length; j++) {
                if (FPoint[i][0] > FPoint[j][1] || FPoint[j][0] > FPoint[i][1] || FPoint[i][2] > FPoint[j][3] || FPoint[j][2] > FPoint[i][3]) {
                    continue;
                } else if (i != j) {
                    FPoint[i] = doCombin(FPoint[j], FPoint[i]);
                    FPoint[j] = new int[]{0, 0, 0, 0};
                    count--;
                }
            }
        }

        //delete the noise point
        for (int i = 0; i < P_length; i++) {
            if (sum(FPoint[i]) == 0) {
                continue;
            } else {
                int sum = 0;
                for (int j = FPoint[i][2]; j < FPoint[i][3] + 1; j++) {
                    for (int k = FPoint[i][0]; k < FPoint[i][1] + 1; k++) {
                        sum += LetterMatrix[j][k];
                    }
                }
                if (sum < 50) {
                    FPoint[i] = new int[]{0, 0, 0, 0};
                    count--;
                }
            }
        }

        int[][] Cerrect_P = new int[count][4];
        count = 0;
        for (int i = 0; i < P_length; i++) {
            if (sum(FPoint[i]) != 0) {//消除雜訊與無用資料，保留字跡
                Cerrect_P[count] = FPoint[i];
                count = count + 1;
            }
        }


        FPoint = new int[count][4];
        FPoint = Cerrect_P;
//        FPoint = antiNoise(Cerrect_P);
    }

//    private int[][] antiNoise(int[][] FPoint){
//        int count=FPoint.length;
//        for (int i = 0; i < FPoint.length; i++) {
//            int sum=0;
//            for (int j = 0; j < FPoint[i][3]-FPoint[i][2]; j++) {
//                for (int k = 0; k < FPoint[i][1]-FPoint[i][0]; k++) {
//                    if(LetterMatrix[FPoint[i][0]+k][FPoint[i][2]+j]==1)sum++;
//                }
//            }
//            if(sum<150){
//                FPoint[i]=new int[]{0,0,0,0};
//                count--;
//            }
//        }
//        int[][] Cerrect_P = new int[count][4];
//        count = 0;
//        for (int i = 0; i < FPoint.length; i++) {
//            if (sum(FPoint[i]) != 0) {//消除雜訊與無用資料，保留字跡
//                Cerrect_P[count] = FPoint[i];
//                count = count + 1;
//            }
//        }
//        return Cerrect_P;
//    }

    private void frameComb(int P_length) {
        int new_length = P_length;
        int[] x_deviation = new int[Math.max(Math.round(new_length / 3), 1)];
        int[] y_deviation = new int[Math.max(Math.round(new_length / 3), 1)];
        int[][] P_center = new int[P_length][2];
        int[] Serch_length = new int[P_length];

        //find avgerage x,y and center
        for (int i = 0; i < P_length; i++) {
            int x_length = FPoint[i][1] - FPoint[i][0];
            int y_length = FPoint[i][3] - FPoint[i][2];
            x_deviation = myBobbleSort(x_deviation, x_length);
            y_deviation = myBobbleSort(y_deviation, y_length);
            P_center[i][0] = Math.round((FPoint[i][1] + FPoint[i][0]) / 2);
            P_center[i][1] = Math.round((FPoint[i][3] + FPoint[i][2]) / 2);
            Serch_length[i] = Math.min(x_length, y_length);
        }
        int x_avg = Math.round(avg(x_deviation));
        int y_avg = Math.round(avg(y_deviation));
        int length_avg = Math.round((x_avg + y_avg) / 2);

        //serch nearest frame less than serch_length
        for (int i = 0; i < P_length; i++) {
            int addr = i;
            for (int j = 0; j < P_length; j++) {
                if (j != i) {
                    int minDistance = length_avg - Serch_length[j];
                    int distance = (int) Math.sqrt(Math.pow(P_center[i][0] - P_center[j][0], 2) + Math.pow(P_center[i][1] - P_center[j][1], 2));
                    if (distance <= minDistance) {
                        addr = j;
                    }
                }
            }
            if (addr != i) {
                FPoint[addr] = doCombin(FPoint[i], FPoint[addr]);
                FPoint[i] = new int[]{0, 0, 0, 0};
                P_center[addr][0] = Math.round((FPoint[addr][0] + FPoint[addr][1]) / 2);
                P_center[addr][1] = Math.round((FPoint[addr][2] + FPoint[addr][3]) / 2);
                P_center[i] = new int[]{0, 0};
                Serch_length[i] = 0;
                new_length = new_length - 1;
            }
        }

        //remake P_vector
        int[][] newVector = new int[new_length][4];
        new_length = 0;
        for (int i = 0; i < P_length; i++) {
            if (sum(FPoint[i]) != 0) {
                newVector[new_length] = FPoint[i];
                new_length++;
            }
        }
        FPoint = new int[new_length][4];
        FPoint = newVector;
    }

    private int[] doCombin(int[] initial, int[] target) {
        int[] newpoint = initial;

        newpoint[0] = Math.min(initial[0], target[0]);
        newpoint[1] = Math.max(initial[1], target[1]);
        newpoint[2] = Math.min(initial[2], target[2]);
        newpoint[3] = Math.max(initial[3], target[3]);

        return newpoint;
    }

    private int[] findFrame(int y, int x) {
        i = y;
        j = x;
        orientation = 1;//east=1,south=2,west=3,north=4

        int left = 0, front = 0;
        int[] P_Bondle = {j, j, i, i}; //0,1 : horizontal  2,3 : vertical

        do {
            if (i > P_Bondle[3]) {
                P_Bondle[3] = i;
            } else if (i < P_Bondle[2]) {
                P_Bondle[2] = i;
            }

            if (j > P_Bondle[1]) {
                P_Bondle[1] = j;
            } else if (j < P_Bondle[0]) {
                P_Bondle[0] = j;
            }

            switch (orientation) { //east=0,south=1,west=2,north=3
                case 0: //east=1
                    left = LetterMatrix[i - 1][j];
                    front = LetterMatrix[i][j + 1];
                    break;
                case 1: //south=2
                    left = LetterMatrix[i][j + 1];
                    front = LetterMatrix[i + 1][j];
                    break;
                case 2: //west=3
                    left = LetterMatrix[i + 1][j];
                    front = LetterMatrix[i][j - 1];
                    break;
                case 3: //north=4
                    left = LetterMatrix[i][j - 1];
                    front = LetterMatrix[i - 1][j];
                    break;
            }

            if (left == 0) {
                if (front == 1) {
                    move();
                } else {
                    turnRight();
                }
            } else {
                turnLeft();
                move();
            }

        } while (!(i == y && j == x));

        return P_Bondle;
    }

    private void move() {

        switch (orientation) { //east=1,south=2,west=3,north=4
            case 0: //east=1
                j++;
                break;
            case 1: //south=2
                i++;
                break;
            case 2: //west=3
                j--;
                break;
            case 3: //north=4
                i--;
                break;
        }
    }

    private void turnLeft() {
        orientation = (orientation + 7) % 4;
    }

    private void turnRight() {
        orientation = (orientation + 1) % 4;
    }

    private int[] myBobbleSort(int[] array, int data) {
        int[] newarray = new int[array.length + 1];
        System.arraycopy(array, 0, newarray, 0, array.length);
        newarray[newarray.length - 1] = data;
        for (int i = 0; i < newarray.length; i++) {
            for (int j = 1; j < newarray.length; j++) {
                if (newarray[j] > newarray[j - 1]) {
                    int temp = newarray[j - 1];
                    newarray[j - 1] = newarray[j];
                    newarray[j] = temp;
                }
            }
        }
        return Arrays.copyOf(newarray, newarray.length - 1);
    }

    private static int sum(int[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum;
    }

    private static int avg(int[] data) {
        return sum(data) / data.length;
    }
}
