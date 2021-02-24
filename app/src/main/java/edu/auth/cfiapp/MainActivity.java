package edu.auth.cfiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;




public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "edu.auth.cfiapp.CONFIRM";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    // Called when the user taps the Confirm button
    public void confirmMeal(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, PlottingActivity.class);
        //Intent intent = new Intent(this, SkaleActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }

}