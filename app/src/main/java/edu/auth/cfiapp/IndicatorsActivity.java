package edu.auth.cfiapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.Arrays;

public class IndicatorsActivity extends AppCompatActivity {

    //private double plateWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicators);

        Intent intent = getIntent();
        String mealID = intent.getStringExtra(PlottingActivity.EXTRA_MEALID);

        TextView meal = (TextView) findViewById(R.id.mealID);
        meal.setText("MEAL INDICATORS FOR MEAL: " + mealID);

        //plateWeight = intent.getDoubleExtra(PlottingActivity.Extra_PLATE);
        double[] time= intent.getDoubleArrayExtra(PlottingActivity.EXTRA_TIME);
        double[] weight= intent.getDoubleArrayExtra(PlottingActivity.EXTRA_WEIGHT);


        Log.i("IndicatorsActivity", Arrays.toString(time));
        Log.i("IndicatorsActivity", Arrays.toString(weight));


        extractMealIndicators(mealID, time, weight);

    }

    private void extractMealIndicators(String mealID, double[] time, double[] weight) {

        TextView a = (TextView) findViewById(R.id.a);
        TextView b = (TextView) findViewById(R.id.b);
        TextView totalFoodIntake = (TextView) findViewById(R.id.totalFoodIntake);
        TextView averageFoodIntakeRate = (TextView) findViewById(R.id.averageFoodIntakeRate);
        TextView averageBiteSize = (TextView) findViewById(R.id.averageBiteSize);
        TextView biteSizeStD = (TextView) findViewById(R.id.biteSizeStD);
        TextView biteFrequency = (TextView) findViewById(R.id.biteFrequency);
        TextView eatingStyle = (TextView) findViewById(R.id.eatingStyle);
        TextView tip = (TextView) findViewById(R.id.tipText);

        Python py = Python.getInstance();
        PyObject module = py.getModule("extract_cfi");
        new Thread(new Runnable() {
            public void run()  {
                try {
                    double[] results = module.callAttr("extract_cfi", time, weight, true, 1, mealID, 0).toJava(double[].class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            a.setText("a = " + String.format("%.5f",results[0]) + " g/s^2");
                            a.setTextColor(Color.BLUE);
                            if (results[0] > -0.0005) {
                                eatingStyle.setText("Linear");
                                eatingStyle.setTextColor(Color.RED);
                                tip.setText("You have a higher risk of developing disordered eating. Consider training " +
                                        "away this eating behaviour through the application's training mode.");
                            } else {
                                eatingStyle.setText("Decelerated");
                                eatingStyle.setTextColor(Color.GREEN);
                                tip.setText("You have a lower risk of developing disordered eating. You can use " +
                                        "the application's training mode to see what a food intake reference curve looks like.");
                            }

                            b.setText("b = " + String.format("%.5f",results[1]) + " g/s");
                            b.setTextColor(Color.BLUE);
                            totalFoodIntake.setText("Total food intake = " + String.format("%.1f",results[2]) + " grams");
                            totalFoodIntake.setTextColor(Color.BLUE);
                            averageFoodIntakeRate.setText("Average food intake rate = " + String.format("%.5f",results[3]) + " g/s");
                            averageFoodIntakeRate.setTextColor(Color.BLUE);
                            averageBiteSize.setText("Average bite size = " + String.format("%.5f",results[4]) + " grams");
                            averageBiteSize.setTextColor(Color.BLUE);
                            biteSizeStD.setText("Bite size StD = " + String.format("%.1f",results[5]) + " grams");
                            biteSizeStD.setTextColor(Color.BLUE);
                            biteFrequency.setText("Bite frequency = " + String.format("%.3f",results[6]) + " bites/min");
                            biteFrequency.setTextColor(Color.BLUE);
                        }
                    });

                } catch (PyException e) {
                    Toast.makeText(IndicatorsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }
}