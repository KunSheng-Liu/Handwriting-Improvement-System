package com.example.Handwriting_System;

import android.app.Application;
import android.graphics.Bitmap;

public class LetterData extends Application {

    private int High, Width,LetterType;
    private SubLetter subLetter;
    private Bitmap LetterBitmap;
    private int[][] LetterMatrix,FPoint;
    private double[][][] LetterCharacter= new double[][][]{
            {{0.456,0.48},{0.466,133.89},{0.324,81.34},{0.469,42.6},{0.331,179},{0.025,-4.28},{0.346,0.964},{0.462,223.56},{0.318,-81.78},{0.481,-43.84}},//陳
            {{0.496,0.505},{0.471,134},{0.351,89.22},{0.45,46.5},{0.31,173.55},{0.05,141.66},{0.297,5.83},{0.485,224.89},{0.316,266.48},{0.455,-45.97}},//施
            {{0.514,0.482},{0.465,134.7},{0.322,87.82},{0.475,43.08},{0.322,180},{0.02,175},{0.338,1.332},{0.458,226.87},{0.31,264.78},{0.494,-42.55}}//劉
    };

    public double[][] getLetterCharacter() {
        return LetterCharacter[LetterType];
    }

    public int getLetterType() {
        return LetterType;
    }

    public void setLetterType(int letterType) {
        LetterType = letterType;
    }

    public void setHigh(int High) {
        this.High = High;
    }

    public void setWidth(int Width) {
        this.Width = Width;
    }

    public void setLetterBitmap(Bitmap LetterBitmap) {
        this.LetterBitmap = LetterBitmap;
    }

    public void setLetterMatrix(int[][] LetterMatrix) {
        this.LetterMatrix = LetterMatrix;
    }

    public void setFPoint(int[][] FPoint){
        this.FPoint=FPoint;
    }

    public int getHigh() {
        return this.High;
    }

    public int getWidth() {
        return this.Width;
    }

    public Bitmap getLetterBitmap() {
        return this.LetterBitmap;
    }

    public int[][] getLetterMatrix() {
        return this.LetterMatrix;
    }

    public int[][] getFPoint() {
        return this.FPoint;
    }

    public void setSubLetter(SubLetter subLetter) {
        this.subLetter = subLetter;
    }

    public SubLetter getSubLetter() {
        return subLetter;
    }

}
