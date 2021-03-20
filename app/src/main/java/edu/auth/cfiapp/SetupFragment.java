package edu.auth.cfiapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

public class SetupFragment extends Fragment implements View.OnClickListener {

    public static final int NUMBER_OF_CONTROL_MEALS = 3;

    SendSchedule SS;

    private String selectedUser;

    private TextView selectedUserTextView, completedControlMealsTextView;
    private EditText numberOfTrainingMeals, stepSizeInGrams;
    private int trainingGoal;

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    interface SendSchedule {
        void sendSchedule(int message);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            SS = (SendSchedule) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedUser = "";
        trainingGoal = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_setup, container, false);

        Button b1 = (Button) v.findViewById(R.id.buttonControl);
        b1.setOnClickListener(this);
        Button b2 = (Button) v.findViewById(R.id.buttonCreateSchedule);
        b2.setOnClickListener(this);
        RadioButton rb1 = (RadioButton) v.findViewById(R.id.radioReduceFoodIntake);
        rb1.setOnClickListener(this);
        RadioButton rb2 = (RadioButton) v.findViewById(R.id.radioIncreaseFoodIntake);
        rb2.setOnClickListener(this);


        selectedUserTextView = (TextView) v.findViewById(R.id.textViewSelectedUserSetup);
        completedControlMealsTextView = (TextView) v.findViewById(R.id.textViewCompletedControlMeals);
        numberOfTrainingMeals = (EditText) v.findViewById(R.id.editTextNumberOfMeals);
        stepSizeInGrams = (EditText) v.findViewById(R.id.editTextStepSize);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!selectedUser.equals("")){
            completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of %d", getCompletedMeals(), NUMBER_OF_CONTROL_MEALS));
        }
    }

    private int getCompletedMeals() {
        try {
            File path = new File(getActivity().getExternalFilesDir(null), selectedUser + File.separator + "control_meals");
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    // We want to find only .txt files
                    return name.endsWith(".txt");
                }
            };

            File[] files = path.listFiles(filter);
            return files.length;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    protected void receiveUser(String message)
    {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);
        completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of %d", getCompletedMeals(), NUMBER_OF_CONTROL_MEALS));
    }


    // Called when the user taps the START CONTROL MEAL button
    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id == R.id.buttonControl && !selectedUser.equals("")) {
            if (getCompletedMeals() < NUMBER_OF_CONTROL_MEALS) {
                intent = new Intent(getActivity(), ControlModeActivity.class);
                intent.putExtra(MainActivity.EXTRA_USER, selectedUser);
                startActivity(intent);
            }
            else {
                Toast.makeText(getActivity(), "You have already completed all control meals", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.buttonCreateSchedule && !selectedUser.equals("")) {
            if (getCompletedMeals() >= NUMBER_OF_CONTROL_MEALS) {
                createTrainingSchedule();
            }
            else {
                Toast.makeText(getActivity(), "Please complete all control meals before creating a training schedule", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.radioReduceFoodIntake) {
            trainingGoal = 0;
        }
        else if (id == R.id.radioIncreaseFoodIntake) {
            trainingGoal = 1;
        }
        else {
            Toast.makeText(getActivity(), "Please select a user first in the Profile tab", Toast.LENGTH_SHORT).show();
        }
    }

    private void createTrainingSchedule() {
        File readPath = new File(getActivity().getExternalFilesDir(null), selectedUser + File.separator + "control_meals");
        readPath = new File(readPath, "control_meals_indicators.csv");

        File writePath = new File(getActivity().getExternalFilesDir(null), selectedUser);
        writePath = new File(writePath, "training_schedule.csv");

        if (readPath.isFile() && !writePath.isFile()) {
            BufferedReader csvReader = null;
            FileWriter csvWriter = null;
            try {
                //Read the meal indicators of the already completed control meals
                csvReader = new BufferedReader(new FileReader(readPath));
                csvReader.readLine(); //Consume first line
                String[][] indicators = new String[NUMBER_OF_CONTROL_MEALS][8];
                for (int i=0; i<NUMBER_OF_CONTROL_MEALS; i++) {
                    String row = csvReader.readLine();
                    if (row != null) {
                        String[] rowIndicators = row.split(";");
                        System.arraycopy(rowIndicators, 0, indicators[i], 0, rowIndicators.length);
                        /*
                        for (int j=0; j<rowIndicators.length; j++) {
                            indicators[i][j] = rowIndicators[j];
                        }
                        */
                    }
                }
                csvReader.close();

                //Calculate min, max and average of the relevant indicators from the control meals, in order to create the training schedule
                double averageOfA=0;
                double minOfA=100000;
                double maxOfA=-10;
                float averageOfFoodIntake=0;
                float minOfFoodIntake=100000;
                float maxOfFoodIntake=-10;
                double a;
                float totalFoodIntake;
                for (int i=0; i<NUMBER_OF_CONTROL_MEALS; i++) {
                    a = Double.parseDouble(indicators[i][1]);
                    totalFoodIntake = Float.parseFloat(indicators[i][3]);
                    if (a < minOfA) minOfA = a;
                    if (a > maxOfA) maxOfA = a;
                    if (totalFoodIntake < minOfFoodIntake) minOfFoodIntake = totalFoodIntake;
                    if (totalFoodIntake > maxOfFoodIntake) maxOfFoodIntake = totalFoodIntake;
                    averageOfA += a;
                    averageOfFoodIntake += totalFoodIntake;
                }
                averageOfA = averageOfA / NUMBER_OF_CONTROL_MEALS;
                averageOfFoodIntake = averageOfFoodIntake / NUMBER_OF_CONTROL_MEALS;



                //Write the training schedule to file, based on the given parameters and the read control meal indicators
                String numberOfMeals = numberOfTrainingMeals.getText().toString();
                String stepSize = stepSizeInGrams.getText().toString();
                if (!numberOfMeals.equals("") && !numberOfMeals.equals("0") && !stepSize.equals("") && !stepSize.equals("0") && trainingGoal!=-1) {
                    csvWriter = new FileWriter(writePath);
                    int mealNum = Integer.parseInt(numberOfMeals);
                    float stepSizeNum = Float.parseFloat(stepSize);
                    for (int i=0; i<mealNum; i++) {
                        csvWriter.append(String.format(Locale.US,"Meal_%d;", i));
                    }
                    csvWriter.append(String.format(Locale.US,"%n"));
                    for (int i=0; i<mealNum; i++) {
                        a = maxOfA - (i+1) * 0.0001;
                        csvWriter.append(String.format(Locale.US,"%.6f;", a));
                    }
                    csvWriter.append(String.format(Locale.US,"%n"));
                    for (int i=0; i<mealNum; i++) {
                        if (trainingGoal == 0) {
                            //Reduce food intake
                            totalFoodIntake = averageOfFoodIntake - stepSizeNum * (i+1);
                        }
                        else {
                            //Increase food intake
                            totalFoodIntake = averageOfFoodIntake + stepSizeNum * (i+1);
                        }
                        csvWriter.append(String.format(Locale.US,"%.2f;", totalFoodIntake));
                    }
                    csvWriter.append(String.format(Locale.US,"%n"));
                    csvWriter.append(String.format(Locale.US,"%d",0));
                    csvWriter.flush();
                    csvWriter.close();
                    Toast.makeText(getActivity(), "Training schedule created successfully", Toast.LENGTH_SHORT).show();

                    SS.sendSchedule(mealNum);
                }
                else {
                    Toast.makeText(getActivity(), "Please complete all text fields with a valid value to create a training schedule", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (writePath.isFile()) {
            Toast.makeText(getActivity(), "Training schedule already exists, please delete the current schedule to create another one", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), "No control meal indicators found, please repeat the control meals", Toast.LENGTH_SHORT).show();
        }
    }

}