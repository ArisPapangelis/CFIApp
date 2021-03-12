package edu.auth.cfiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ControlModeActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";

    private double plateWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_mode);

        plateWeight = 0;
    }

    public void confirmControlMeal(View view) {
        Intent intent = new Intent(this, PlottingActivity.class);
        EditText editText = (EditText) findViewById(R.id.controlMealID);
        String message = editText.getText().toString();
        intent.putExtra(MainActivity.EXTRA_PLATE, plateWeight);
        intent.putExtra(MainActivity.EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}