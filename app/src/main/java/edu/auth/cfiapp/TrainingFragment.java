package edu.auth.cfiapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TrainingFragment extends Fragment implements View.OnClickListener{

    private String selectedUser;

    private TextView selectedUserTextView, completedTrainingMealsTextView;


    public TrainingFragment() {
        // Required empty public constructor
    }

    public static TrainingFragment newInstance(String param1, String param2) {
        return new TrainingFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedUser = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_training, container, false);

        Button trainingButton = (Button) v.findViewById(R.id.buttonTrain);
        trainingButton.setOnClickListener(this);

        selectedUserTextView = (TextView) v.findViewById(R.id.textViewSelectedUserTraining);
        completedTrainingMealsTextView = (TextView) v.findViewById(R.id.textViewCompletedTrainingMeals);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        File readPath = new File(getActivity().getExternalFilesDir(null), selectedUser);
        readPath = new File(readPath, "training_schedule.csv");
        if (!selectedUser.equals("") && readPath.isFile()){
            completedTrainingMealsTextView.setText(String.format("Training schedule found! %d out of %d training meals completed", getNumberOfMeals(true), getNumberOfMeals(false)));
            completedTrainingMealsTextView.setTextColor(Color.GREEN);
        }
    }

    private int getNumberOfMeals(boolean completed) {
        File readPath = new File(getActivity().getExternalFilesDir(null), selectedUser);
        readPath = new File(readPath, "training_schedule.csv");
        BufferedReader csvReader = null;
        try {
            //Read the meal indicators of the already completed control meals
            csvReader = new BufferedReader(new FileReader(readPath));
            int totalNumberOfMeals = csvReader.readLine().split(";").length; //Consume first line
            csvReader.readLine(); //Consume second line
            csvReader.readLine(); //Consume third line
            int numberOfCompletedMeals = Integer.parseInt(csvReader.readLine().split(";")[0]);
            csvReader.close();
            if (completed) {
                return numberOfCompletedMeals;
            }
            else {
                return totalNumberOfMeals;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        File schedule = new File(getActivity().getExternalFilesDir(null), selectedUser);
        schedule = new File(schedule, "training_schedule.csv");
        if (id==R.id.buttonTrain && !selectedUser.equals("")){
            if (schedule.isFile()) {
                intent = new Intent(getActivity(), TrainingModeActivity.class);
                intent.putExtra(MainActivity.EXTRA_USER, selectedUser);
                startActivity(intent);
            }
            else {
                Toast.makeText(getActivity(), "Please create a training schedule for the currently selected user", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Please select a user first in the Profile tab", Toast.LENGTH_SHORT).show();
        }
    }

    protected void receiveUser(String message)
    {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);

        File readPath = new File(getActivity().getExternalFilesDir(null), selectedUser);
        readPath = new File(readPath, "training_schedule.csv");
        if (!selectedUser.equals("") && readPath.isFile()){
            completedTrainingMealsTextView.setText(String.format("Training schedule found! %d out of %d training meals completed", getNumberOfMeals(true), getNumberOfMeals(false)));
            completedTrainingMealsTextView.setTextColor(Color.GREEN);
        }
    }

    public void receiveSchedule(int message) {
        completedTrainingMealsTextView.setText(String.format("Training schedule found! 0 out of %d training meals completed", message));
    }
}