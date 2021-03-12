package edu.auth.cfiapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.atomax.android.skaleutils.SkaleHelper;


public class MainActivity extends AppCompatActivity{

    public static final String EXTRA_MESSAGE = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    // Called when the user taps the TEST or TRAIN button
    public void confirmMeal(View view) {

        if (view.getId()==R.id.buttonTrain){
            Intent intent = new Intent(this, TrainingModeActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ControlModeActivity.class);
            startActivity(intent);
        }

    }

}
