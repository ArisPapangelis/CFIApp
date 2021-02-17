package com.example.cfiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PlottingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView7);
        textView.setText(message);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject module = py.getModule("extract_cfi");
        ImageView mealView = (ImageView) findViewById(R.id.mealView);



        new Thread(new Runnable() {
            public void run()  {
                try {
                    //for (int i=150; i<3396; i=i+150) {
                    byte[] bytes = module.callAttr("extract_cfi", "1", 10, false, 1, 3396).toJava(byte[].class);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mealView.setImageBitmap(bitmap);
                    //Thread.sleep(2000);
                    // }

                } catch (PyException e) {
                    Toast.makeText(PlottingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }).start();


    }
}