package edu.auth.cfiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ControlModeActivity extends AppCompatActivity {

    private String selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_mode);

        Intent intent = getIntent();
        selectedUser = intent.getStringExtra(MainActivity.EXTRA_USERID);
    }

    //Called when the user presses the START MEAL button. The relevant activity is launched only if a meal ID has been entered.
    public void confirmControlMeal(View view) {
        Intent intent = new Intent(this, PlottingActivity.class);
        EditText editText = (EditText) findViewById(R.id.controlMealID);
        String mealID = editText.getText().toString();
        if (!mealID.equals("")) {
            intent.putExtra(MainActivity.EXTRA_PLATE, 0);
            intent.putExtra(MainActivity.EXTRA_MEALID, mealID);
            intent.putExtra(MainActivity.EXTRA_USERID, selectedUser);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Please enter a meal ID first", Toast.LENGTH_SHORT).show();
        }

    }
}