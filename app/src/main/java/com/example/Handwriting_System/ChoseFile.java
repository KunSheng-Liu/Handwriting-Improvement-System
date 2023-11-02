package com.example.Handwriting_System;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ChoseFile extends AppCompatActivity implements View.OnClickListener {


    private Spinner spi_file;
    private Button btn_chose;
    private ImageView imv_file;

    private int id;
    private String[] file_list = {"textdata1", "textdata2", "textdata3", "textdata4", "textdata5", "textdata6", "textdata7", "textdata8", "textdata9",
            "textdata10","textdata11", "figure1", "figure2", "figure3", "figure4", "figure5", "figure6", "name_3x3", "letter_matrix_4x4", "ramdon_text"};
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chose_file);

        imv_file = findViewById(R.id.imv_file);

        spi_file = findViewById(R.id.spi_file);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, file_list);
        spi_file.setAdapter(listAdapter);
        spi_file.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Context context = imv_file.getContext();
                id = context.getResources().getIdentifier(spi_file.getSelectedItem().toString(), "drawable", context.getPackageName());
                imv_file.setImageResource(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn_chose = findViewById(R.id.btn_chose);
        btn_chose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Intent intent;
        LetterConvert myLetter = new LetterConvert(getResources(), id);

        LetterData myData = (LetterData) getApplicationContext();
        myData.setHigh(myLetter.ReturnHigh());
        myData.setWidth(myLetter.ReturnWidth());
        myData.setLetterBitmap(myLetter.ReturnBitmap());
        myData.setLetterMatrix(myLetter.ReturnMatrix());
        myData.setFPoint(null);

        switch (view.getId()) {
            case R.id.btn_chose:
                intent = new Intent(ChoseFile.this, ShowLetter.class);
                startActivity(intent);
                break;
            default:
                break;
        }

    }
}
