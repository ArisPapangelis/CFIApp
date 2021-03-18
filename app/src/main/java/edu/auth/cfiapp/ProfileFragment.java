package edu.auth.cfiapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }


    SendUser SU;

    private String selectedUser;
    private EditText userID;
    private Button b1, b2;
    private EditText ageEditText, heightEditText, sexEditText, weightEditText, notesEditText;
    private TextView bmiTextView, selectedUserTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedUser = "";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);


        b1 = (Button) v.findViewById(R.id.buttonSelectUser);
        b1.setOnClickListener(this);
        b2 = (Button) v.findViewById(R.id.buttonSaveUserInfo);
        b2.setOnClickListener(this);

        userID = (EditText) v.findViewById(R.id.userID);
        ageEditText = (EditText) v.findViewById(R.id.editTextAge);
        heightEditText = (EditText) v.findViewById(R.id.editTextHeight);
        sexEditText = (EditText) v.findViewById(R.id.editTextSex);
        weightEditText = (EditText) v.findViewById(R.id.editTextWeight);
        notesEditText = (EditText) v.findViewById(R.id.editTextNotes);

        bmiTextView = (TextView) v.findViewById(R.id.textViewBMI);
        selectedUserTextView = (TextView) v.findViewById((R.id.textViewSelectedUserProfile));

        return v;
    }

    interface SendUser {
        void sendData(String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            SU = (SendUser) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonSelectUser) {
            selectUser();
        } else if (id == R.id.buttonSaveUserInfo) {
            saveUserInfo();
        }

    }

    private void saveUserInfo() {
        String user = userID.getText().toString();
        File path = new File(getActivity().getExternalFilesDir(null), user);

        String age = ageEditText.getText().toString();
        String sex = sexEditText.getText().toString();
        String height = heightEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        //String bmi = bmiTextView.getText().toString();
        String notes = notesEditText.getText().toString();

        FileWriter csvWriter = null;
        try {
            if (!age.equals("") && !sex.equals("") && !height.equals("") && !weight.equals("") && !notes.equals("") && !user.equals("")) {
                if (!path.isDirectory()){
                    path.mkdirs();
                    File mealsPath = new File(path, "control_meals");
                    mealsPath.mkdirs();
                    mealsPath = new File(path, "training_meals");
                    mealsPath.mkdirs();
                }
                path = new File(path, user + "_info.csv");
                csvWriter = new FileWriter(path);
                csvWriter.append(String.format(Locale.US,"Age;Sex;Height;Weight;BMI;Notes%n"));
                csvWriter.append(String.format(Locale.US,"%d;%s;%.2f;%.2f;%.2f;%s",
                        Integer.parseInt(age), sex, Float.parseFloat(height), Float.parseFloat(weight), calculateBMI(height, weight), notes));
                csvWriter.flush();
                csvWriter.close();
                Toast.makeText(getActivity(), "User info saved successfully", Toast.LENGTH_SHORT).show();
                selectUser();
            } else {
                Toast.makeText(getActivity(), "Please enter all of the user's info", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float calculateBMI(String height, String weight) {
        float h = Float.parseFloat(height);
        float w = Float.parseFloat(weight);
        return w / (h * h);
    }

    private void selectUser() {
        String user = userID.getText().toString();
        File path = new File(getActivity().getExternalFilesDir(null), user);
        path = new File(path, user + "_info.csv");
        if (path.isFile() && !user.equals("")) {
            selectedUser = user;
            Toast.makeText(getActivity(), "Selected user " + selectedUser, Toast.LENGTH_SHORT).show();
            SU.sendData(selectedUser);
            selectedUserTextView.setText("The currently selected user is: " + selectedUser);
            selectedUserTextView.setTextColor(Color.BLUE);

            BufferedReader csvReader = null;
            try {
                csvReader = new BufferedReader(new FileReader(path));
                csvReader.readLine(); //Consume first line
                String row;
                while ((row = csvReader.readLine()) != null) {
                    String[] info = row.split(";");
                    ageEditText.setText(info[0]);
                    sexEditText.setText(info[1]);
                    heightEditText.setText(info[2]);
                    weightEditText.setText(info[3]);
                    bmiTextView.setText(info[4]);
                    notesEditText.setText(info[5]);
                }
                csvReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getActivity(), "User doesn't exist", Toast.LENGTH_SHORT).show();
        }

    }
}

