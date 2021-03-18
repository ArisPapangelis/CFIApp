package edu.auth.cfiapp;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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
    private Button b1,b2,b3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedUser="";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);


        b1 = (Button) v.findViewById(R.id.buttonCreateUser);
        b1.setOnClickListener(this);
        b2 = (Button) v.findViewById(R.id.buttonSelectUser);
        b2.setOnClickListener(this);
        b3 = (Button) v.findViewById(R.id.buttonSaveUserInfo);
        b3.setOnClickListener(this);

        userID = (EditText)  v.findViewById(R.id.userID);

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
        Intent intent;

        if (id == R.id.buttonSelectUser) {
            selectedUser = userID.getText().toString();
            Toast.makeText(getActivity(), "Selected user " + selectedUser, Toast.LENGTH_SHORT).show();
            SU.sendData(selectedUser);
        }

    }
}