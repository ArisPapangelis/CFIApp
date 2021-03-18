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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment implements View.OnClickListener {

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance() {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    private String selectedUser;

    private TextView selectedUserTextView, completedControlMealsTextView;

    final Handler controlMealsHandler = new Handler();
    Runnable updateControlMealsRunnable = new Runnable() {
        @Override
        public void run() {
            //clock.setText(convertTime());
            // 50 millis to give the ui thread time to breath. Adjust according to your own experience
            controlMealsHandler.postDelayed(updateControlMealsRunnable, 1000);
        }
    };

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

        //controlMealsHandler.post(updateControlMealsRunnable);

        Button b = (Button) v.findViewById(R.id.buttonControl);
        b.setOnClickListener(this);

        selectedUserTextView = (TextView) v.findViewById(R.id.textViewSelectedUserSetup);
        completedControlMealsTextView = (TextView) v.findViewById(R.id.textViewCompletedControlMeals);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        completedControlMealsTextView.setText(String.format("Your number of completed meals is: %d out of 3", getCompletedMeals()));
    }

    private int getCompletedMeals() {

        return 1;
    }

    protected void receiveData(String message)
    {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);
    }


    // Called when the user taps the TEST or TRAIN button
    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id==R.id.buttonControl && !selectedUser.equals("")) {
            intent = new Intent(getActivity(), ControlModeActivity.class);
            intent.putExtra(MainActivity.EXTRA_USER, selectedUser);
            startActivity(intent);
        }
        else {
            Toast.makeText(getActivity(), "Please select a user first in the Profile tab", Toast.LENGTH_SHORT).show();
        }


    }
}