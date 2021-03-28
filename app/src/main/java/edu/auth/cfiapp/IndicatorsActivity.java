package edu.auth.cfiapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class IndicatorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicators);

        Intent intent = getIntent();
        String mealID = intent.getStringExtra(PlottingActivity.EXTRA_MEALID);
        String userID = intent.getStringExtra(PlottingActivity.EXTRA_USERID);
        double plateWeight = intent.getDoubleExtra(PlottingActivity.EXTRA_PLATE, 0);
        double[] time = intent.getDoubleArrayExtra(PlottingActivity.EXTRA_TIME);
        double[] weight = intent.getDoubleArrayExtra(PlottingActivity.EXTRA_WEIGHT);
        double aCoefficient = intent.getDoubleExtra(PlottingActivity.EXTRA_ACOEFF, 0);

        TextView meal = (TextView) findViewById(R.id.mealID);
        meal.setText("MEAL INDICATORS FOR MEAL: " + mealID);
        TextView user = (TextView) findViewById(R.id.user);
        user.setText("USER: " + userID);

        Log.i("IndicatorsActivity", Arrays.toString(time));
        Log.i("IndicatorsActivity", Arrays.toString(weight));

        extractMealIndicators(mealID, userID, time, weight, plateWeight, aCoefficient);
    }


    private void extractMealIndicators(String mealID, String userID, double[] time, double[] weight, double plateWeight, double aCoefficient) {

        ImageView mealCurve = (ImageView) findViewById(R.id.mealCurveImageView);

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
            public void run() {
                try {
                    byte[] bytes = module.callAttr("extract_cfi", time, weight, true, 1, mealID, plateWeight, aCoefficient, true).toJava(byte[].class);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mealCurve.setImageBitmap(bitmap);
                        }
                    });

                    double[] results = module.callAttr("extract_cfi", time, weight, true, 1, mealID, 0, aCoefficient, false).toJava(double[].class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            a.setText("Food intake deceleration (a) = " + String.format("%.5f", results[0]) + " g/s^2");
                            a.setTextColor(Color.BLUE);
                            if (results[0] > 0){
                                eatingStyle.setText("Accelerated");
                                eatingStyle.setTextColor(Color.RED);
                                tip.setText("Your accelerated eating rate is faster than normal. Consider training " +
                                        "away this eating behaviour through the application's training mode.");
                            } else if (results[0] > -0.0005) {
                                eatingStyle.setText("Linear");
                                eatingStyle.setTextColor(Color.RED);
                                tip.setText("Your linear eating rate is faster than normal. Consider training " +
                                        "away this eating behaviour through the application's training mode.");
                            } else {
                                eatingStyle.setText("Decelerated");
                                eatingStyle.setTextColor(Color.GREEN);
                                tip.setText("Your decelerated eating rate matches normal dietary behaviour. You can use " +
                                        "the application's training mode to see what a food intake reference curve looks like.");
                            }

                            b.setText("Initial food intake rate (b) = " + String.format("%.5f", results[1]) + " g/s");
                            b.setTextColor(Color.BLUE);
                            totalFoodIntake.setText("Total food intake = " + String.format("%.1f", results[2]) + " grams");
                            totalFoodIntake.setTextColor(Color.BLUE);
                            averageFoodIntakeRate.setText("Average food intake rate = " + String.format("%.5f", results[3]) + " g/s");
                            averageFoodIntakeRate.setTextColor(Color.BLUE);
                            averageBiteSize.setText("Average bite size = " + String.format("%.5f", results[4]) + " grams");
                            averageBiteSize.setTextColor(Color.BLUE);
                            biteSizeStD.setText("Bite size StD = " + String.format("%.1f", results[5]) + " grams");
                            biteSizeStD.setTextColor(Color.BLUE);
                            biteFrequency.setText("Bite frequency = " + String.format("%.3f", results[6]) + " bites/min");
                            biteFrequency.setTextColor(Color.BLUE);
                        }
                    });

                    //Commit the extracted meal indicators to file
                    writeMealIndicatorsToFile(userID,mealID, plateWeight, results);

                } catch (PyException e) {
                    Toast.makeText(IndicatorsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    private void writeMealIndicatorsToFile(String userID, String mealID, double plateWeight, double[] results) {
        File writePath = new File(getApplicationContext().getExternalFilesDir(null), userID);
        if (plateWeight==0) {
            writePath = new File(writePath, "control_meals");
        }
        else {
            writePath = new File(writePath, "training_meals");
        }
        if (!writePath.isDirectory()) {
            writePath.mkdirs();
        }

        try {
            //Write .csv file with the meal indicators extracted from the meal
            FileWriter csvWriter;
            File file = new File(writePath, "control_meals_indicators.csv");
            if (plateWeight!=0) file = new File(writePath, "training_meals_indicators.csv");
            if (!file.isFile()) {
                csvWriter = new FileWriter(file, false);
                csvWriter.append(String.format(Locale.US, "Meal_ID;a_coefficient;b_coefficient;Total_food_intake(grams);Average_food_intake_rate(grams/s);" +
                        "Average_bite_size(grams);Bite_size_standard_deviation(grams);Bite_frequency(bites/min);%n"));
            }
            else {
                csvWriter = new FileWriter(file, true);
            }

            csvWriter.append(mealID).append(";");
            for (double result : results) {
                csvWriter.append(String.format(Locale.US, "%.6f;", result));
            }
            csvWriter.append(String.format(Locale.US,"%n"));
            csvWriter.flush();
            csvWriter.close();

            if (plateWeight!=0) {
                incrementTrainingSchedule(userID);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void incrementTrainingSchedule(String userID) {
        File file = new File(getApplicationContext().getExternalFilesDir(null), userID);
        file = new File(file, "training_schedule.csv");
        try {
            //Read the parameters of the created training schedule
            BufferedReader csvReader = new BufferedReader(new FileReader(file));
            String[] lines = new String[4];
            for (int i=0; i<lines.length; i++) {
                lines[i] = csvReader.readLine();
            }
            csvReader.close();

            int incrementedNumberOfMeals = -1;
            if (lines[lines.length-1] != null){
                incrementedNumberOfMeals = Integer.parseInt(lines[lines.length-1]) + 1;
            }

            //Rewrite the last line of training_schedule.csv now that the training meal has been completed
            FileWriter csvWriter = new FileWriter(file);
            for (int i=0; i<lines.length-1; i++) {
                csvWriter.append(String.format(Locale.US, "%s%n", lines[i]));
            }
            csvWriter.append(String.format(Locale.US, "%d", incrementedNumberOfMeals));
            csvWriter.flush();
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}