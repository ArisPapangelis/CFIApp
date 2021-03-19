package edu.auth.cfiapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment implements View.OnClickListener {

    private String selectedUser;

    private TextView selectedUserTextView, completedControlMealsTextView;

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
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
        View v =  inflater.inflate(R.layout.fragment_setup, container, false);

        Button b1 = (Button) v.findViewById(R.id.buttonControl);
        b1.setOnClickListener(this);
        Button b2 = (Button) v.findViewById(R.id.buttonCreateSchedule);
        b2.setOnClickListener(this);

        selectedUserTextView = (TextView) v.findViewById(R.id.textViewSelectedUserSetup);
        completedControlMealsTextView = (TextView) v.findViewById(R.id.textViewCompletedControlMeals);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!selectedUser.equals("")){
            completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of 3", getCompletedMeals()));
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

    protected void receiveData(String message)
    {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);
        completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of 3", getCompletedMeals()));
    }


    // Called when the user taps the START CONTROL MEAL button
    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id==R.id.buttonControl && !selectedUser.equals("")) {
            if (getCompletedMeals()<3) {
                intent = new Intent(getActivity(), ControlModeActivity.class);
                intent.putExtra(MainActivity.EXTRA_USER, selectedUser);
                startActivity(intent);
            }
            else {
                Toast.makeText(getActivity(), "You have already completed 3 control meals", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.buttonCreateSchedule && !selectedUser.equals("")) {
            if (getCompletedMeals()>=3) {
                createTrainingSchedule();
            }
            else {
                Toast.makeText(getActivity(), "Please complete 3 control meals before creating a training schedule", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getActivity(), "Please select a user first in the Profile tab", Toast.LENGTH_SHORT).show();
        }




    }

    private void createTrainingSchedule() {


    }
}