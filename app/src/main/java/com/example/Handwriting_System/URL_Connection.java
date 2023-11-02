package com.example.Handwriting_System;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class URL_Connection extends AppCompatActivity {

    private Button bt_enter;
    private EditText edt_URL;
    private String URL="http://192.168.43.77";

    private StringBuffer stringBuffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_connection);

        bt_enter = findViewById(R.id.bt_enter);
        edt_URL = findViewById(R.id.edt_URL5);

        bt_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
                    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if(!edt_URL.getText().toString().equals(""))
                    URL=edt_URL.getText().toString();
                httpURLConnectionGet();
            }
        });

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case 1:{
                    Toast.makeText(URL_Connection.this, "成功連結："+ URL, Toast.LENGTH_LONG).show();
                    LetterConvert myLetter = new LetterConvert(stringBuffer.toString());
                    stringBuffer = null;

                    LetterData myData = (LetterData) getApplicationContext();
                    myData.setHigh(myLetter.ReturnHigh());
                    myData.setWidth(myLetter.ReturnWidth());
                    myData.setLetterBitmap(myLetter.ReturnBitmap());
                    myData.setLetterMatrix(myLetter.ReturnMatrix());

                    Intent intent = new Intent(URL_Connection.this , ShowLetter.class);
//                    intent.putExtra("enableConvert",false);
                    startActivity(intent);
                    break;
                }
                case 2:{
                    Toast.makeText(URL_Connection.this, "成功失敗："+ URL, Toast.LENGTH_LONG).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void httpURLConnectionGet() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;
                Message msg = new Message();

                try {
                    URL url = new URL(URL);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();


                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        Log.d("HttpURLConnection", "success");
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String readLine;
                        stringBuffer = new StringBuffer();
                        int i=0;
                        while ((readLine = bufferedReader.readLine()) != null) {
                            stringBuffer.append(readLine);
                        }
                        inputStream.close();
                        bufferedReader.close();

                        msg.what=1;
                        handler.sendMessage(msg);
                    }else{
                        msg.what=2;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();
    }
}
