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

    //All the extra messages that need to be passed to IndicatorsActivity at the end of PlottingActivity's lifecycle.
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

    /*
    Arraylists that contain the time and weight values of the scale weight timeseries. A mutex is used for synchronization when
    modifying the arraylists.
     */
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

        // Get the Intent that started this activity and extract the extra messages.
        Intent intent = getIntent();
        mealID = intent.getStringExtra(MainActivity.EXTRA_MEALID);
        plateWeight = intent.getDoubleExtra(MainActivity.EXTRA_PLATE, 0);
        selectedUser = intent.getStringExtra(MainActivity.EXTRA_USERID);
        aCoefficient = getACoefficient();   //The goal a coefficient if it is a training meal. The value is -1 if not.

        //Needed for connecting to the smart scale.
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

        //Checking if bluetooth is enabled, and if the scale has been given permission to connect.
        if(mSkaleHelper.isBluetoothEnable()){
            boolean hasPermission = SkaleHelper.hasPermission(this);
            if(hasPermission){
                mSkaleHelper.resume();
                Log.i("PlottingActivity","Finding skale...");
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

        //On pausing the activity, the scale stops communicating with the application, and the launched timer task is also stopped.
        mSkaleHelper.pause();
        handler.removeCallbacks(extractCFIRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
    This function is called when the user presses the button "END MEAL". It passes all the relevant parameters to the next activity,
    IndicatorsActivity, as extra messages, and calls the function to write the time and weight timeseries of the meal to a .txt file
    for future reference.
     */
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

    /*
    This function writes the time and weight timeseries of the finished meal to a .txt file. If the weight of the plate has been passed as
    0 from the previous activity, it is a control meal, else it is a training meal.
     */
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
            /*
            Write .txt file with time and weight measurements. The format is:
            #Samples: Number_of_samples
            #Time: Duration_of_meal
            #Plate weight: Weight_of_the_plate (0 if control meal)
            Time_0:Weight_0
            Time_1:Weight_1
            ...............
             */
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

            //Write .png file with the picture of the completed meal.
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

    /*
    Function to convert a Double object array to a double primitive array.
     */
    private double[] toPrimitive(Double[] array){
        double[] primitiveArray = new double[array.length];
        for (int i=0; i<array.length; i++){
            primitiveArray[i] = array[i].doubleValue();
        }
        return primitiveArray;
    }

    /*
    This function is called when any of the 2 buttons of the scale is clicked. No functionality is implemented on each button
    press for our use case.
     */
    @Override
    public void onButtonClicked(int id) {
        Toast.makeText(this, "button " + id + " is clicked", Toast.LENGTH_SHORT).show();
        /*
        if(id == 1){
            mSkaleHelper.tare();
        }
         */
    }

    /*
    This function is called when the weight of the scale is updated. The polling rate is not constant, rather, a new weight value
    is given whenever the scale is ready. In order to approximate a 5 hz polling rate, at least 150 ms are required to have passed
    from the previous committed sample. Drift is mostly eliminated, since some samples are between 150 and 200 ms, while some are above 200 ms.
    Long term, the result is like having a constant 5 hz polling rate. So, whenever at least 150 ms have passed from the previous sample,
    the current time since the start of the meal and the current weight on the scale are committed to the time and weight timeseries.
    The UI is also updated with the current weight of the scale.
     */
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

    /*
    This function is called when the scale first connects to the phone. The asynchronous task which plots the CFI curve in real time is
    started on a timer in this function. The task is first called after 10 seconds have passed from the start of the meal, so that the
    first bites have already been made. The task is repeated every 5 seconds, which means that the CFI curve is updated every 5 seconds.
    The reason 5 seconds were selected, is because it is a good compromise between accuracy and performance.
     */
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

    /*
    This function is called when the battery level of the scale is updated, multiple times per seconds.
    The UI is updated accordingly with the current battery level.
     */
    @Override
    public void onBatteryLevelUpdate(int level) {
        batteryTextView.setText(String.format("battery: %02d", level));
    }

    /*
    This class in the asynchronous task which calls the python function that extracts the CFI curve based on the current time and
    weight measurements.
     */
    private final class ExtractCFI extends AsyncTask<Void, Void, Bitmap> {
        /*
        In the doInBackground function, the python module extract_cfi is called, with the time and weight timeseries being given as arguments,
        as well as the meal ID, the weight of the plate, and the goal a_coefficient. The function runs in a different thread from the
        main UI thread, so as not to bog down the application, since the python function needs 1 or 2 seconds to return its' result.
        The returned result is the bitmap of the CFI curve, up the the current point in time. In the case of a training meal, the
        reference CFI curve the user should follow is also shown on the returned bitmap.
         */
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
                return bitmap;

            } catch (PyException e) {
                Log.e("PlottingActivity", e.getMessage());
            }
            return null;
        }

        /*
        In the onPostExecute function, the UI of the application is updated with the resulting bitmap returned from the
        extract_cfi python function.
         */
        protected void onPostExecute(Bitmap bitmap) {
            mealView.setImageBitmap(bitmap);
        }

    }

    /*
    This function is called in order to get the goal a_coefficient for the current training meal. The value of the coefficient
    is read from the second line of the training schedule, based on which out of all training meals is the one currently being made.
    The number of the training meal is found from the fourth line of the training schedule.
    In case the meal is a control meal and not a training meal, a value of -1 is returned.
     */
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

    /*
    This function handles the Bluetooth permission request for the scale.
     */
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