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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrainingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrainingFragment extends Fragment implements View.OnClickListener{

    public TrainingFragment() {
        // Required empty public constructor
    }


    public static TrainingFragment newInstance(String param1, String param2) {
        TrainingFragment fragment = new TrainingFragment();
        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

         */
        return fragment;
    }

    private String selectedUser;

    private Button trainingButton;
    private TextView selectedUserTextView;

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

        trainingButton= (Button) v.findViewById(R.id.buttonTrain);
        trainingButton.setOnClickListener(this);

        selectedUserTextView = (TextView) v.findViewById(R.id.textViewSelectedUserTraining);

        return v;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id==R.id.buttonTrain){
            intent = new Intent(getActivity(), TrainingModeActivity.class);
            startActivity(intent);
        }

    }

    protected void receiveData(String message)
    {
        selectedUser = message;
        selectedUserTextView.setText("The currently selected user is: " + selectedUser);
        selectedUserTextView.setTextColor(Color.BLUE);
    }
}