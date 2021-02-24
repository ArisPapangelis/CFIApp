package edu.auth.cfiapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.atomax.android.skaleutils.SkaleHelper;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PlottingActivity extends AppCompatActivity implements SkaleHelper.Listener {

    public static final String EXTRA_MESSAGE = "edu.auth.cfiapp.ENDMEAL";

    // Get the Intent that started this activity and extract the string
    private Intent intent;
    private String message;

    private static final int REQUEST_BT_ENABLE = 2;
    private static final int REQUEST_BT_PERMISSION = 1;

    private SkaleHelper mSkaleHelper;

    private TextView weightTextView;
    private TextView batteryTextView;
    private ImageView mealView;

    private List <Double> time = Collections.synchronizedList(new ArrayList<Double>(1000));
    private List <Double> weight = Collections.synchronizedList(new ArrayList<Double>(1000));

    private long startTime;
    private long previousTime;

    final Handler handler = new Handler();
    Runnable extractCFIRunnable = new Runnable() {
        @Override
        public void run() {
            new ExtractCFI(3000,message).execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);

        // Get the Intent that started this activity and extract the string
        intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        mSkaleHelper = new SkaleHelper(this);
        mSkaleHelper.setListener(this);

        weightTextView = (TextView) findViewById(R.id.weightTextView);
        batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        mealView = (ImageView) findViewById(R.id.mealView);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Timer extractCFITimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(extractCFIRunnable);
            }
        };

        extractCFITimer.schedule(task, 20 * 1000,5 * 1000);

        //for (int i=150; i<3396; i=i+150){
          //  new ExtractCFI(i,message).execute();
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSkaleHelper.isBluetoothEnable()){
            boolean hasPermission = SkaleHelper.hasPermission(this);
            if(hasPermission){
                mSkaleHelper.resume();
                Log.i("PlottingActivity","finding skale...");
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(extractCFIRunnable);
    }


    public void endMeal(View view){
        Toast.makeText(PlottingActivity.this, "Meal finished", Toast.LENGTH_LONG).show();
        //String message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
        Intent indicatorIntent = new Intent(PlottingActivity.this, IndicatorsActivity.class);
        indicatorIntent.putExtra(EXTRA_MESSAGE, message);
        startActivity(indicatorIntent);
        this.finish();
    }

    @Override
    public void onButtonClicked(int id) {
        Toast.makeText(this, "button " + id + " is clicked", Toast.LENGTH_SHORT).show();
        if(id == 1){
            mSkaleHelper.tare();
        }else{
            // TODO invoke some function here for square button
        }

    }

    @Override
    public void onWeightUpdate(float w) {
        weightTextView.setText(String.format("%1.1f g", w));
        if (System.currentTimeMillis() - previousTime > 150) {
            synchronized (time) {
                time.add((double) (System.currentTimeMillis() - startTime) / 1000);
                weight.add((double) w);
            }
            Log.i("PlottingActivity", String.valueOf(weight));
            Log.i("PlottingActivity", String.valueOf(time));
            previousTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onBindRequest() {
        Log.i("PlottingActivity","New skale found, pairing with it.");
    }

    @Override
    public void onBond() {
        Log.i("PlottingActivity", "Pairing done, connecting...");
    }

    @Override
    public void onConnectResult(boolean success) {
        if(success){
            Log.i("PlottingActivity", "Connected");
            startTime =  System.currentTimeMillis();
            previousTime = startTime;
        }
    }

    @Override
    public void onDisconnected() {
        Log.i("PlottingActivity", "Disconnected");
    }

    @Override
    public void onBatteryLevelUpdate(int level) {
        batteryTextView.setText(String.format("battery: %02d", level));
    }

    private final class ExtractCFI extends AsyncTask<Void, Void, Bitmap> {
        int i;
        String message;
        ExtractCFI(int i, String message) {
            this.i = i;
            this.message=message;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Python py = Python.getInstance();
            PyObject module = py.getModule("extract_cfi");
            try {
                Double[] t;
                Double[] w;
                synchronized (time) {
                    t = time.toArray(new Double[time.size()]);
                    w= weight.toArray(new Double[weight.size()]);
                }
                byte[] bytes = module.callAttr("extract_cfi", t, w, false, 1, i, message).toJava(byte[].class);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                //System.out.println(i);
                return bitmap;

            } catch (PyException e) {
                //Toast.makeText(PlottingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("PlottingActivity", e.getMessage());
            }

            return null;
        }

        protected void onPostExecute(Bitmap bitmap) {
            mealView.setImageBitmap(bitmap);
            //if (i >3250){
                //Toast.makeText(PlottingActivity.this, "Meal finished", Toast.LENGTH_LONG).show();
            //}

        }

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