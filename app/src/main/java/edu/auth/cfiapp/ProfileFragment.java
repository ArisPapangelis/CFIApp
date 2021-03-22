package edu.auth.cfiapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    SendUser SU;

    private String selectedUser;
    private EditText userID;
    private EditText ageEditText, heightEditText, sexEditText, weightEditText, notesEditText;
    private TextView bmiTextView, selectedUserTextView;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    interface SendUser {
        void sendUser(String message);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        try {
            SU = (SendUser) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Error in retrieving data. Please try again");
        }
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
        View v = inflater.inflate(R.layout.fragment_profile, container, false);


        Button b1 = (Button) v.findViewById(R.id.buttonSelectUser);
        b1.setOnClickListener(this);
        Button b2 = (Button) v.findViewById(R.id.buttonSaveUserInfo);
        b2.setOnClickListener(this);
        Button b3 = (Button) v.findViewById(R.id.buttonDeleteUser);
        b3.setOnClickListener(this);

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


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.buttonSelectUser) {
            selectUser();
        } else if (id == R.id.buttonSaveUserInfo) {
            saveUserInfo();
        }
        else {
            deleteUser();
        }

    }

    private void deleteUser() {
        String user = userID.getText().toString();
        File path = new File(getActivity().getExternalFilesDir(null), user);
        if (!user.equals("") && path.isDirectory()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Warning")
                    .setMessage("Are you sure you want to delete the user " + selectedUser + "? The user's info," +
                            " as well as all meal .txt and .png files, meal indicator files and training schedule files will be deleted.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        boolean delete = deleteRecursive(path);
                        if (delete) {
                            Toast.makeText(getActivity(), "User " + selectedUser + " deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getActivity(), "Error when trying to delete the user", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialogInterface, whichButton) -> {
                        Toast.makeText(getActivity(), "User deletion cancelled", Toast.LENGTH_SHORT).show();
                    }).show();
        }
        else {
            Toast.makeText(getActivity(), "User doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();
    }

    private void saveUserInfo() {
        String user = userID.getText().toString();
        File path = new File(getActivity().getExternalFilesDir(null), user);

        String age = ageEditText.getText().toString();
        String sex = sexEditText.getText().toString();
        String height = heightEditText.getText().toString();
        String weight = weightEditText.getText().toString();
        String notes = notesEditText.getText().toString();

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
                FileWriter csvWriter = new FileWriter(path);
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
            SU.sendUser(selectedUser);
            selectedUserTextView.setText("The currently selected user is: " + selectedUser);
            selectedUserTextView.setTextColor(Color.BLUE);

            try {
                BufferedReader csvReader = new BufferedReader(new FileReader(path));
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
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getActivity(), "User doesn't exist", Toast.LENGTH_SHORT).show();
        }
    }
}
