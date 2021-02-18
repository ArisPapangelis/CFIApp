package com.example.cfiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;



public class PlottingActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.cfiapp.CONFIRM";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        //TextView textView = findViewById(R.id.textView7);
        //textView.setText(message);
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        for (int i=150; i<3396; i=i+150){
            new ExtractCFI(i, message).execute();
        }

        // runPythonThread(message);


    }

    private final class ExtractCFI extends AsyncTask<String, Void, Bitmap> {
        int i;
        String message;
        ExtractCFI(int i, String message) {
            // list all the parameters like in normal class define
            this.i = i;
            this.message = message;
        }

        @Override
        protected Bitmap doInBackground(String... message) {
            Python py = Python.getInstance();
            PyObject module = py.getModule("extract_cfi");
            try {
                byte[] bytes = module.callAttr("extract_cfi", "1", 10, false, 1, i, message).toJava(byte[].class);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                System.out.println(i);
                return bitmap;

            } catch (PyException e) {
                Toast.makeText(PlottingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            return null;
        }

        protected void onPostExecute(Bitmap bitmap) {
            ImageView mealView = (ImageView) findViewById(R.id.mealView);
            mealView.setImageBitmap(bitmap);
            if (i >3250){
                Toast.makeText(PlottingActivity.this, "Meal finished", Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent indicatorIntent = new Intent(PlottingActivity.this, IndicatorsActivity.class);
                indicatorIntent.putExtra(EXTRA_MESSAGE, message);
                startActivity(indicatorIntent);
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        Intent indicatorIntent = new Intent(this, IndicatorsActivity.class);
        indicatorIntent.putExtra(EXTRA_MESSAGE, message);
        startActivity(indicatorIntent);
    }



    private void runPythonThread(String message) {
        ImageView mealView = (ImageView) findViewById(R.id.mealView);

        Python py = Python.getInstance();
        PyObject module = py.getModule("extract_cfi");
        Thread T  = new Thread(new Runnable() {
            public void run()  {
                for (int i=150; i<3396; i=i+150) {
                    try {
                        byte[] bytes = module.callAttr("extract_cfi", "1", 10, false, 1, i, message).toJava(byte[].class);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        System.out.println(i);
                        try {
                            int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mealView.setImageBitmap(bitmap);
                                    if (finalI >3250){
                                        Toast.makeText(PlottingActivity.this, "Meal finished", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            Thread.sleep(50);

                        } catch (InterruptedException  e) {
                            e.printStackTrace();
                        }

                    } catch (PyException e) {
                        Toast.makeText(PlottingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PlottingActivity.this.finish();
            }
        });

        T.start();

    }
}