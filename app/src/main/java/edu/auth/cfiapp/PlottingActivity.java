package edu.auth.cfiapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class PlottingActivity extends AppCompatActivity implements SkaleHelper.Listener {

    public static final String EXTRA_MEALID = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";
    public static final String EXTRA_USERID = "edu.auth.cfiapp.USER";
    public static final String EXTRA_TIME = "edu.auth.cfiapp.TIME";
    public static final String EXTRA_WEIGHT = "edu.auth.cfiapp.WEIGHT";
    public static final String EXTRA_ACOEFF = "edu.auth.cfiapp.ACOEFF";



    private static final int REQUEST_BT_ENABLE = 2;
    private static final int REQUEST_BT_PERMISSION = 1;


    private String mealID;
    private double plateWeight;
    private String selectedUser;
    private double aCoefficient;

    private SkaleHelper mSkaleHelper;
    private TextView weightTextView;
    private TextView batteryTextView;
    private ImageView mealView;


    private ArrayList <Double> time = new ArrayList<Double>(1000);
    private ArrayList <Double> weight = new ArrayList<Double>(1000);
    private final Object mutex = new Object();

    private long startTime;
    private long previousTime;

    final Handler handler = new Handler();
    Runnable extractCFIRunnable = new Runnable() {
        @Override
        public void run() {
            new ExtractCFI().execute();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);

        // Get the Intent that started this activity and extract the string
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mealID = intent.getStringExtra(MainActivity.EXTRA_MEALID);
        plateWeight = intent.getDoubleExtra(MainActivity.EXTRA_PLATE, 0);
        selectedUser = intent.getStringExtra(MainActivity.EXTRA_USERID);
        aCoefficient = getACoefficient();

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
        indicatorIntent.putExtra(EXTRA_MEALID, mealID);
        indicatorIntent.putExtra(EXTRA_PLATE, plateWeight);
        indicatorIntent.putExtra(EXTRA_USERID, selectedUser);
        indicatorIntent.putExtra(EXTRA_ACOEFF, aCoefficient);

        double[] t;
        double[] w;
        synchronized(mutex) {
            t = toPrimitive(time.toArray(new Double[time.size()]));
            w = toPrimitive(weight.toArray(new Double[weight.size()]));
            indicatorIntent.putExtra(EXTRA_TIME, t);
            indicatorIntent.putExtra(EXTRA_WEIGHT, w);
        }
        writeMealToFile(t,w,mealID);
        startActivity(indicatorIntent);
        this.finish();
    }

    private void writeMealToFile(double[] t, double[] w, String mealID) {
        File path = new File(getApplicationContext().getExternalFilesDir(null), selectedUser);
        if (plateWeight == 0) {
            path = new File(path, "control_meals");
        }
        else {
            path = new File(path, "training_meals");
        }
        if (!path.isDirectory()){
            path.mkdirs();
        }

        try {
            //Write .txt file with time and weight measurements
            File file = new File(path, mealID + ".txt");
            FileOutputStream out;
            file.createNewFile();
            out = new FileOutputStream(file,false);
            out.write(String.format(Locale.US,"#Samples: %d%n", t.length).getBytes());
            out.write(String.format(Locale.US,"#Time: %.3f secs%n", (float) t[t.length-1]).getBytes());
            out.write(String.format(Locale.US,"#Plate weight: %.1f grams%n", plateWeight).getBytes());
            for (int i=0; i < t.length; i++){
                out.write(String.format(Locale.US,"%.3f:%.1f%n", (float)t[i], (float)w[i]).getBytes());
            }
            out.close();

            //Write .png file with the picture of the completed meal
            file = new File(path, mealID + ".png");
            file.createNewFile();
            out = new FileOutputStream(file,false);
            Bitmap bmp=((BitmapDrawable) mealView.getDrawable()).getBitmap();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

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
            synchronized (mutex) {
                time.add((double) (System.currentTimeMillis() - startTime) / 1000);
                weight.add((double) w);
            }
            Log.i("PlottingActivity", String.valueOf(weight));
            Log.i("PlottingActivity", String.valueOf(time));
            previousTime = System.currentTimeMillis();
        }
        else if (previousTime==startTime){
            //First timestamp
            synchronized (mutex) {
                time.add((double) 0);
                weight.add((double) w);
            }
            Log.i("PlottingActivity", String.valueOf(weight));
            Log.i("PlottingActivity", String.valueOf(time));
            previousTime = System.currentTimeMillis();
        }

        weightTextView.setText(String.format(Locale.US,"%1.1f g", w));
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
            Toast.makeText(this, "Press the END MEAL button when you have finished your meal", Toast.LENGTH_LONG).show();
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
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Python py = Python.getInstance();
            PyObject module = py.getModule("extract_cfi");
            try {
                Double[] t;
                Double[] w;
                synchronized (mutex) {
                    t = time.toArray(new Double[time.size()]);
                    w= weight.toArray(new Double[weight.size()]);
                }
                byte[] bytes = module.callAttr("extract_cfi", t, w, false, 1, mealID, plateWeight,aCoefficient, false).toJava(byte[].class);
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
        }

    }

    private double getACoefficient() {
        File readPath = new File(getApplicationContext().getExternalFilesDir(null), selectedUser);
        readPath = new File(readPath, "training_schedule.csv");
        if (readPath.isFile()) {
            BufferedReader csvReader;
            try {
                //Read the goal food intake for the current meal of the training schedule
                csvReader = new BufferedReader(new FileReader(readPath));
                csvReader.readLine(); //Consume first line
                String secondLine = csvReader.readLine();
                csvReader.readLine(); //Consume third line
                String mealNumber = csvReader.readLine().split(";")[0];
                csvReader.close();
                if (secondLine != null && mealNumber != null) {
                    String[] allACoefficients = secondLine.split(";");
                    return Double.parseDouble(allACoefficients[Integer.parseInt(mealNumber)]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
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