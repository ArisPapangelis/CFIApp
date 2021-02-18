package edu.auth.cfiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class IndicatorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicators);

        Intent intent = getIntent();
        String mealID = intent.getStringExtra(PlottingActivity.EXTRA_MESSAGE);

        runPythonThread(mealID);

    }

    private void runPythonThread(String mealID) {
        TextView a = (TextView) findViewById(R.id.a);
        TextView b = (TextView) findViewById(R.id.b);
        TextView totalFoodIntake = (TextView) findViewById(R.id.totalFoodIntake);
        TextView averageFoodIntakeRate = (TextView) findViewById(R.id.averageFoodIntakeRate);
        TextView biteSizeStD = (TextView) findViewById(R.id.biteSizeStD);
        TextView biteFrequency = (TextView) findViewById(R.id.biteFrequency);

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject module = py.getModule("extract_cfi");
        new Thread(new Runnable() {
            public void run()  {
                try {
                    double[] results = module.callAttr("extract_cfi", "1", 10, true, 1, -1, mealID).toJava(double[].class);
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                a.setText("a = " + String.format("%.5f",results[0]) + " g/s^2");
                                b.setText("b = " + String.format("%.5f",results[1]) + " g/s");
                                totalFoodIntake.setText("Total food intake = " + String.format("%.3f",results[2]) + " grams");
                                averageFoodIntakeRate.setText("Average food intake rate = " + String.format("%.5f",results[3]) + " g/s");
                                biteSizeStD.setText("Bite size StD = " + String.format("%.3f",results[4]) + " grams");
                                biteFrequency.setText("Bite frequency = " + String.format("%.3f",results[5]) + " bites/min");
                            }
                        });
                        Thread.sleep(50);
                    } catch (InterruptedException  e) {
                        e.printStackTrace();
                    }

                } catch (PyException e) {
                    Toast.makeText(IndicatorsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }).start();
    }
}