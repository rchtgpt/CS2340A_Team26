package com.example.greenplate.views;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.greenplate.viewmodels.ProfileViewModel;
import com.example.greenplate.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    private DatabaseReference dbUserEmail, dbUserHeight, dbUserWeight, dbUserGender;
    private ProfileViewModel mViewModel;
    private TextInputEditText uEmail;
    private TextInputEditText uHeight;
    private TextInputEditText uWeight;
    private TextInputEditText uGender;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        /*Logic for Inflating EditText values*/
        final String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        uEmail = (TextInputEditText) v.findViewById(R.id.userEmail);
        uHeight = (TextInputEditText) v.findViewById(R.id.height);
        uWeight = (TextInputEditText) v.findViewById(R.id.weight);
        uGender = (TextInputEditText) v.findViewById(R.id.gender);

        dbUserEmail = FirebaseDatabase.getInstance("https://greenplate-d518c-default-rtdb.firebaseio.com/").getReference().child("users").child(user_id).child("email");
        dbUserHeight = FirebaseDatabase.getInstance("https://greenplate-d518c-default-rtdb.firebaseio.com/").getReference().child("users").child(user_id).child("height");
        dbUserWeight = FirebaseDatabase.getInstance("https://greenplate-d518c-default-rtdb.firebaseio.com/").getReference().child("users").child(user_id).child("weight");
        dbUserGender = FirebaseDatabase.getInstance("https://greenplate-d518c-default-rtdb.firebaseio.com/").getReference().child("users").child(user_id).child("gender");

        Button saveInfoBtn = v.findViewById(R.id.saveInfoBtn);

        saveInfoBtn.setOnClickListener(v_2 -> {
                // Save the updated values to the database
                System.out.println(uEmail.getText().toString());
                System.out.println(uHeight.getText().toString());
                System.out.println(uWeight.getText().toString());
                System.out.println(uGender.getText().toString());
                System.out.println(dbUserEmail);
                System.out.println(dbUserHeight);
                System.out.println(dbUserWeight);
                System.out.println(dbUserGender);
                dbUserEmail.setValue(uEmail.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Profile Fragment", e.toString());
                    }
                });
                dbUserHeight.setValue(uHeight.getText().toString());
                dbUserWeight.setValue(uWeight.getText().toString());
                dbUserGender.setValue(uGender.getText().toString());
        });

        Button signOutBtn = v.findViewById(R.id.signOutBtn);

        signOutBtn.setOnClickListener(v_1 -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }






}