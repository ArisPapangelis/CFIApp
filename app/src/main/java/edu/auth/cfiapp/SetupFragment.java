package edu.auth.cfiapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SetupFragment extends Fragment implements View.OnClickListener {

    public static final int NUMBER_OF_CONTROL_MEALS = 3;
    private static final int FILE_REQUEST_CODE = 1;

    SendSchedule SS;

    private String selectedUser;

    private TextView selectedUserTextView, completedControlMealsTextView;
    private EditText numberOfTrainingMeals, stepSizeInGrams;
    private int trainingGoal;   //0 for Reduce food intake, 1 for Increase food intake

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance() {
        return new SetupFragment();
    }

    /*
    Interface used for notifying TrainingFragment that a new training schedule has been created,
    or that a previously created schedule has been deleted.
     */
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
        Button b3 = (Button) v.findViewById(R.id.buttonDeleteMeal);
        b3.setOnClickListener(this);
        Button b4 = (Button) v.findViewById(R.id.buttonDeleteSchedule);
        b4.setOnClickListener(this);

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
            completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of %d", getCompletedControlMeals(), NUMBER_OF_CONTROL_MEALS));
        }
    }

    //Function to get the number of currently completed control meals. Used to update the completed control meals textView.
    private int getCompletedControlMeals() {
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

    /*
    Function to receive the selected user's username from ProfileFragment, and update the selected user textView.
    The completed control meals textView for the selected user is also updated.
     */
    protected void receiveUser(String message) {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);
        completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of %d", getCompletedControlMeals(), NUMBER_OF_CONTROL_MEALS));
    }


    // Called when the user taps any of the buttons.
    @Override
    public void onClick(View view) {
        int id = view.getId();

        //When START CONTROL MEAL is pressed.
        if (id == R.id.buttonControl && !selectedUser.equals("")) {
            if (getCompletedControlMeals() < NUMBER_OF_CONTROL_MEALS) {
                Intent intent = new Intent(getActivity(), ControlModeActivity.class);
                intent.putExtra(MainActivity.EXTRA_USERID, selectedUser);
                startActivity(intent);
            }
            else {
                Toast.makeText(getActivity(), "You have already completed all control meals", Toast.LENGTH_SHORT).show();
            }
        }

        //When DELETE CONTROL MEAL is pressed.
        else if (id == R.id.buttonDeleteMeal && !selectedUser.equals("")) {
            if (getCompletedControlMeals() > 0) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                File path = new File(getActivity().getExternalFilesDir(null) + File.separator + selectedUser + File.separator + "control_meals");
                Uri uri = Uri.parse(path.toString());
                fileIntent.setDataAndType(uri, "text/plain");
                startActivityForResult(Intent.createChooser(fileIntent,"Select file to delete:"), FILE_REQUEST_CODE);
            }
            else {
                Toast.makeText(getActivity(), "There are no control meals to delete", Toast.LENGTH_SHORT).show();
            }
        }

        //When CREATE TRAINING SCHEDULE is pressed.
        else if (id == R.id.buttonCreateSchedule && !selectedUser.equals("")) {
            if (getCompletedControlMeals() >= NUMBER_OF_CONTROL_MEALS) {
                createTrainingSchedule();
            }
            else {
                Toast.makeText(getActivity(), "Please complete all control meals before creating a training schedule", Toast.LENGTH_SHORT).show();
            }
        }

        //WHEN DELETE TRAINING SCHEDULE is pressed.
        else if (id == R.id.buttonDeleteSchedule && !selectedUser.equals("")) {
            File file = new File(getActivity().getExternalFilesDir(null), selectedUser);
            file = new File(file, "training_schedule.csv");
            if (file.isFile()) {
                File finalFile = file;
                new AlertDialog.Builder(getActivity())
                        .setTitle("Warning")
                        .setMessage("Do you really want to delete the training schedule for user " + selectedUser + "?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            boolean delete = finalFile.delete();
                            if (delete) {
                                //Notifying TrainingFragment that the current schedule has been deleted.
                                SS.sendSchedule(-100);
                                Toast.makeText(getActivity(), "Training schedule deleted successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), "Error when trying to delete the training schedule", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, (dialogInterface, whichButton) -> {
                            Toast.makeText(getActivity(), "Training schedule deletion cancelled", Toast.LENGTH_SHORT).show();
                        }).show();
            }
            else {
                Toast.makeText(getActivity(), "Training schedule for user " + selectedUser + " doesn't exist", Toast.LENGTH_SHORT).show();
            }
        }

        //When selecting radio button "Reduce food intake".
        else if (id == R.id.radioReduceFoodIntake) {
            trainingGoal = 0;
        }

        //When selecting radio button "Increase food intake".
        else if (id == R.id.radioIncreaseFoodIntake) {
            trainingGoal = 1;
        }

        else {
            Toast.makeText(getActivity(), "Please select a user first in the Profile tab", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    This function is called when the user tries to delete a control meal. A file explorer window is opened,
    for the user to select the meal they want to delete. A message is shown for successful or unsuccessful deletion of the meal.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedFile = data.getData();
            String mealToDelete = "";
            if (selectedFile != null) {
                String mealPath = selectedFile.getPath();
                mealToDelete = mealPath.split(File.separator)[mealPath.split(File.separator).length-1];
            }
            if (deleteControlMeal(mealToDelete)) {
                Toast.makeText(getActivity(), "Control meal " + mealToDelete + " deleted successfully", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Error when trying to delete " + mealToDelete, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    Function that deletes the selected control meal from the relevant filepath. Both the .txt file with scale measurements, as
    well as the .png file with the meal curve of the meal are deleted. The row with the extracted meal indicators of the meal
    is also deleted from the relevant .csv file.
     */
    private boolean deleteControlMeal(String mealToDelete) {
        File file = new File(getActivity().getExternalFilesDir(null) +
                File.separator + selectedUser + File.separator + "control_meals" + File.separator + mealToDelete);
        File pngFile = new File(getActivity().getExternalFilesDir(null) +
                File.separator + selectedUser + File.separator + "control_meals" + File.separator + mealToDelete.split("\\.")[0] + ".png");


        boolean deleted = file.delete(); //Delete .txt file
        if (deleted) {
            pngFile.delete(); //Delete .png file
            try {
                //Read the file with the extracted control meal indicators
                File indicatorsFile = new File(getActivity().getExternalFilesDir(null) +
                        File.separator + selectedUser + File.separator + "control_meals" + File.separator + "control_meals_indicators.csv");
                BufferedReader csvReader = new BufferedReader(new FileReader(indicatorsFile));

                //All lines are read, and the line corresponding to the file being deleted is found.
                ArrayList <String> lines = new ArrayList<String>(NUMBER_OF_CONTROL_MEALS + 1);
                int lineToDelete = 1;   //Starts from 1 since the first line is a header.
                String currentLine;
                while ((currentLine = csvReader.readLine())!=null) {
                    lines.add(currentLine);
                    String[] indicators = currentLine.split(";");
                    String mealID = mealToDelete.split("\\.")[0];
                    if (indicators[0].equals(mealID) && lines.size()!=1) {
                        lineToDelete = lines.size()-1;
                    }
                }
                csvReader.close();

                //Rewrite the file, excluding the line that matches the meal which was deleted
                FileWriter csvWriter = new FileWriter(indicatorsFile, false);
                for (int i=0; i<lines.size(); i++) {
                    if (i!=lineToDelete) {
                        csvWriter.append(String.format(Locale.US, "%s%n", lines.get(i)));
                    }
                }
                csvWriter.flush();
                csvWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return deleted;
    }

    /*
    This function is called when the user pressed the button to create a new training schedule.
    The training schedule is created based on the completed control meals of the user, as well as the
    selected parameters in the UI. The parameters are: step size in grams, number of training meals and
    training mode (increase or decrease food intake).
     */
    private void createTrainingSchedule() {
        File readPath = new File(getActivity().getExternalFilesDir(null), selectedUser + File.separator + "control_meals");
        readPath = new File(readPath, "control_meals_indicators.csv");

        File writePath = new File(getActivity().getExternalFilesDir(null), selectedUser);
        writePath = new File(writePath, "training_schedule.csv");

        if (readPath.isFile() && !writePath.isFile()) {
            BufferedReader csvReader;
            FileWriter csvWriter;
            try {
                //Read the meal indicators of the already completed control meals.
                csvReader = new BufferedReader(new FileReader(readPath));
                csvReader.readLine(); //Consume first line
                String[][] indicators = new String[NUMBER_OF_CONTROL_MEALS][8];
                for (int i=0; i<NUMBER_OF_CONTROL_MEALS; i++) {
                    String row = csvReader.readLine();
                    if (row != null) {
                        String[] rowIndicators = row.split(";");
                        System.arraycopy(rowIndicators, 0, indicators[i], 0, rowIndicators.length);
                    }
                }
                csvReader.close();

                //Calculate min, max and average of the relevant indicators from the control meals, in order to create the training schedule.
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



                /*
                Write the training schedule to file, based on the given parameters and the read control meal indicators.
                The created .csv file with the training schedule, has as many columns as the parameter "number of training meals".
                The first row contains the names of the meals, starting from 0.
                The second row, contains the goal a_coefficient (food intake deceleration) of the meal curve for the relevant training meal.
                The third row, contains the goal food intake for the relevant training meal.
                The fourth row, contains the number of completed training meals, which is 0 when the schedule is first created.
                 */
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

                    /*
                    The goal a_coefficient is calculated from the average coefficient of the three control meals.
                    The aim is to normalise a possibly abnormal a_coefficient through training,
                    from positive or close to zero, to a healthy coefficient of -0.0005. The a_coefficient is decreased by 0.0001
                    in each training meal, until it is normalised.
                     */
                    for (int i=0; i<mealNum; i++) {
                        a = averageOfA - (i+1) * 0.0001;
                        if (a<-0.0005) a = 0.0005;
                        csvWriter.append(String.format(Locale.US,"%.6f;", a));
                    }
                    csvWriter.append(String.format(Locale.US,"%n"));

                    /*
                    The goal food intake is calculated from the average food intake of the three control meals.
                    The aim is to increase or decrease food intake, depending on the selected training mode,
                    by the parameter "step size in grams" each time. For example, for 5 training meals, with the mode
                    "Increase food intake", a step size number of 10 grams, and an average control meal intake of 300 grams,
                    the row of the .csv file would be 310;320;330;340;350;
                     */
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

                    //Number of completed training meals is 0 at first.
                    csvWriter.append(String.format(Locale.US,"%d",0));
                    csvWriter.flush();
                    csvWriter.close();
                    Toast.makeText(getActivity(), "Training schedule created successfully", Toast.LENGTH_SHORT).show();

                    //Notifying TrainingFragment that a new training schedule has been created.
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