package com.example.Handwriting_System;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button bt_URLconnect, bt_choosefile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bt_URLconnect = findViewById(R.id.bt_URLconnect);
        bt_URLconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, URL_Connection.class);
                startActivity(intent);
            }
        });

        bt_choosefile = findViewById(R.id.bt_choosefile);
        bt_choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChoseFile.class);
                startActivity(intent);
            }
        });


    }

}
