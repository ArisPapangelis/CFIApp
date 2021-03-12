package edu.auth.cfiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.atomax.android.skaleutils.SkaleHelper;

public class TrainingModeActivity extends AppCompatActivity implements SkaleHelper.Listener {

    private static final int REQUEST_BT_ENABLE = 2;
    private static final int REQUEST_BT_PERMISSION = 1;

    private SkaleHelper mSkaleHelper;
    private TextView mWeightTextView;
    private double plateWeight;
    private double currentScaleWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_mode);


        mSkaleHelper = new SkaleHelper(this);
        mSkaleHelper.setListener(this);
        mWeightTextView = (TextView) findViewById(R.id.mWeightTextView);
        plateWeight = 0;
        currentScaleWeight = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSkaleHelper.isBluetoothEnable()){
            boolean hasPermission = SkaleHelper.hasPermission(this);
            if(hasPermission){
                mSkaleHelper.resume();
            }else{
                SkaleHelper.requestBluetoothPermission(this, REQUEST_BT_PERMISSION);
            }
        }else{
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, REQUEST_BT_ENABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSkaleHelper.pause();
        //mSkaleHelper.disconnect();
    }

    // Called when the user taps the TRAIN button
    public void confirmTrainingMeal(View view) {
        Intent intent = new Intent(this, PlottingActivity.class);
        EditText editText = (EditText) findViewById(R.id.trainingMealID);
        String message = editText.getText().toString();

        if (plateWeight > 5 && currentScaleWeight > 50){
            intent.putExtra(MainActivity.EXTRA_PLATE, plateWeight);
            intent.putExtra(MainActivity.EXTRA_MESSAGE, message);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Sample the weight of the plate first, " +
                    "and then fill the plate to start the meal", Toast.LENGTH_SHORT).show();
        }

    }

    //Called when the user taps the SAMPLE button
    public void samplePlateWeight(View view){
        if (currentScaleWeight < 5){
            Toast.makeText(this, "No plate detected, place it on the scale", Toast.LENGTH_SHORT).show();
        } else{
            plateWeight = currentScaleWeight;
            Toast.makeText(this, "Plate weight sampled successfully", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onButtonClicked(int id) {
        Toast.makeText(this, "button " + id + " is clicked", Toast.LENGTH_SHORT).show();
        if (id == 1) {
            mSkaleHelper.tare();
        }
    }

    @Override
    public void onWeightUpdate(float weight) {
        mWeightTextView.setText(String.format("%1.1f g", weight));
        currentScaleWeight = weight;
    }


    @Override
    public void onBindRequest() {
        Log.i("MainActivity","New skale found, pairing with it.");
    }

    @Override
    public void onBond() {
        Log.i("MainActivity", "Pairing done, connecting...");
    }

    @Override
    public void onConnectResult(boolean success) {
        if(success){
            Log.i("MainActivity", "Connected");
            Toast.makeText(this, "Scale connected successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDisconnected() {
        Log.i("MainActivity", "Disconnected");
    }

    @Override
    public void onBatteryLevelUpdate(int level) {
        Log.i("MainActivity", String.format("battery: %02d", level));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_BT_PERMISSION) {

            boolean result = SkaleHelper.checkPermissionRequest(requestCode, permissions, grantResults);

            if(result){
                mSkaleHelper.resume();
            }else{
                Toast.makeText(this, "No bluetooth permission", Toast.LENGTH_SHORT).show();
            }

            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}