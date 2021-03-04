package edu.auth.cfiapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PlottingActivity extends AppCompatActivity implements SkaleHelper.Listener {

    public static final String EXTRA_MEALID = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_TIME = "edu.auth.cfiapp.TIME";
    public static final String EXTRA_WEIGHT = "edu.auth.cfiapp.WEIGHT";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";


    // Get the Intent that started this activity and extract the string
    private Intent intent;
    private String message;

    private static final int REQUEST_BT_ENABLE = 2;
    private static final int REQUEST_BT_PERMISSION = 1;

    private SkaleHelper mSkaleHelper;

    private TextView weightTextView;
    private TextView batteryTextView;
    private ImageView mealView;
    private double plateWeight;

    private List <Double> time = Collections.synchronizedList(new ArrayList<Double>(1000));
    private List <Double> weight = Collections.synchronizedList(new ArrayList<Double>(1000));

    private long startTime;
    private long previousTime;

    final Handler handler = new Handler();
    Runnable extractCFIRunnable = new Runnable() {
        @Override
        public void run() {
            new ExtractCFI(message).execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);

        // Get the Intent that started this activity and extract the string
        intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        plateWeight = intent.getDoubleExtra(MainActivity.EXTRA_PLATE, 0);

        mSkaleHelper = new SkaleHelper(this);
        mSkaleHelper.setListener(this);

        weightTextView = (TextView) findViewById(R.id.weightTextView);
        batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        mealView = (ImageView) findViewById(R.id.mealView);
    }

    @Override
    protected void onStart(){
        super.onStart();

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
        handler.removeCallbacks(extractCFIRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void endMeal(View view){
        Toast.makeText(PlottingActivity.this, "Meal finished", Toast.LENGTH_LONG).show();

        Intent indicatorIntent = new Intent(PlottingActivity.this, IndicatorsActivity.class);
        indicatorIntent.putExtra(EXTRA_MEALID, message);
        indicatorIntent.putExtra(EXTRA_PLATE, plateWeight);
        double[] t;
        double[] w;
        synchronized(time) {
            t = toPrimitive(time.toArray(new Double[time.size()]));
            w = toPrimitive(weight.toArray(new Double[weight.size()]));
            indicatorIntent.putExtra(EXTRA_TIME, t);
            indicatorIntent.putExtra(EXTRA_WEIGHT, w);
        }
        writeMealToFile(t,w,message);
        startActivity(indicatorIntent);
        this.finish();
    }

    private void writeMealToFile(double[] t, double[] w, String mealID) {
        File path = new File(getApplicationContext().getExternalFilesDir(null), "Meals");
        //File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "CFIApp_Meals");

        if (!path.isDirectory()){
            path.mkdirs();
        }
        File file = new File(path, mealID + ".txt");
        FileOutputStream out;

        try {
            file.createNewFile();
            out = new FileOutputStream(file,false);
            out.write(String.format("#Samples: %d%n", t.length).getBytes());
            out.write(String.format("#Time: %.3f secs%n", (float) t[t.length-1]).getBytes());
            out.write(String.format("#Plate weight: %.1f grams%n", plateWeight).getBytes());
            for (int i=0; i < t.length; i++){
                out.write(String.format("%.3f:%.1f%n", (float)t[i], (float)w[i]).getBytes());
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private double[] toPrimitive(Double[] array){
        double[] primitiveArray = new double[array.length];
        for (int i=0; i<array.length; i++){
            primitiveArray[i] = array[i].doubleValue();
        }
        return primitiveArray;
    }

    @Override
    public void onButtonClicked(int id) {
        Toast.makeText(this, "button " + id + " is clicked", Toast.LENGTH_SHORT).show();
        /*
        if(id == 1){
            mSkaleHelper.tare();
        }
         */
    }

    @Override
    public void onWeightUpdate(float w) {
        if (System.currentTimeMillis() - previousTime > 150) {
            synchronized (time) {
                time.add((double) (System.currentTimeMillis() - startTime) / 1000);
                weight.add((double) w);
            }
            Log.i("PlottingActivity", String.valueOf(weight));
            Log.i("PlottingActivity", String.valueOf(time));
            previousTime = System.currentTimeMillis();
        }
        else if (previousTime==startTime){
            //First timestamp
            synchronized (time) {
                time.add((double) 0);
                weight.add((double) w);
            }
            Log.i("PlottingActivity", String.valueOf(weight));
            Log.i("PlottingActivity", String.valueOf(time));
            previousTime = System.currentTimeMillis();
        }

        weightTextView.setText(String.format("%1.1f g", w));
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
            Timer extractCFITimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    handler.post(extractCFIRunnable);
                }
            };
            extractCFITimer.schedule(task, 10 * 1000,5 * 1000);

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
        String message;
        ExtractCFI(String message) {
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
                byte[] bytes = module.callAttr("extract_cfi", t, w, false, 1, message, plateWeight, false).toJava(byte[].class);
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