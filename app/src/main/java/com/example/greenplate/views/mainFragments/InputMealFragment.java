package com.example.greenplate.views.mainFragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.example.greenplate.views.ColumnActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.greenplate.R;
import com.example.greenplate.viewmodels.InputMealViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class InputMealFragment extends Fragment {

    private Button storeMealBtn;
    private Button compareCaloriesBtn;

    private TextInputEditText mealNameInput, mealCaloriesInput;
    private TextView calorieGoalText, dailyCalorieText;
    private InputMealViewModel inputMealViewModel;
    private DatabaseReference databaseReference;
    private double calorieGoal = 0.0;
    private long dailyCalorieIntake = 0;
    final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_meal, container, false);

        // Initialize components from both branches
        storeMealBtn = view.findViewById(R.id.storeMealBtn);
        compareCaloriesBtn = view.findViewById(R.id.compareCaloriesBtn);
        mealNameInput = view.findViewById(R.id.mealNameInput);
        mealCaloriesInput = view.findViewById(R.id.mealCaloriesInput);
        calorieGoalText = view.findViewById(R.id.calorieGoalText);
        dailyCalorieText = view.findViewById(R.id.dailyCalorieIntakeText);

        inputMealViewModel = new ViewModelProvider(this).get(InputMealViewModel.class);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Button click listener for creating chart
        compareCaloriesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ColumnActivity.class);
            intent.putExtra("calorieGoal", calorieGoal);
            intent.putExtra("dailyCalorieIntake", dailyCalorieIntake);
            startActivity(intent);
        });
        // Button click listener for storing meals
        storeMealBtn.setOnClickListener(v -> {
            String mealName = mealNameInput.getText().toString().trim();
            String calorieString = mealCaloriesInput.getText().toString().trim();

            if (mealName.isEmpty() || calorieString.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int calories;
            try {
                calories = Integer.parseInt(calorieString);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Calories must be a number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == null) {
                Toast.makeText(getActivity(), "User not signed in", Toast.LENGTH_SHORT).show();
                return;
            }

            inputMealViewModel.storeMeal(userId, mealName, calories);
            mealNameInput.setText("");
            mealCaloriesInput.setText("");

            // Update calorie goal and daily intake immediately
            fetchAndUpdateCalorieData();
        });

        // Initial fetch and display of calorie data
        fetchAndUpdateCalorieData();

        return view;
    }

    private void fetchAndUpdateCalorieData() {
        databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String gender = String.valueOf(snapshot.child("gender").getValue());
                    int age = (Integer.valueOf(String.valueOf(snapshot.child("age").getValue())) != null) ? Integer.valueOf(String.valueOf(snapshot.child("age").getValue())) : 19;
                    int weight = Integer.valueOf(String.valueOf(snapshot.child("weight").getValue()));
                    int height = Integer.valueOf(String.valueOf(snapshot.child("height").getValue()));

                    calorieGoal = calculateBMR(gender, age, weight, height);

                    calorieGoalText.setText("Calculated Calorie Goal: " + calorieGoal);
                    Map<String, Long> meals = (Map<String, Long>) snapshot.child("meals").getValue();

                    if (meals != null) {
                        dailyCalorieIntake = 0;

                        for (String key : meals.keySet()) {
                            dailyCalorieIntake += meals.get(key);
                        }

                        dailyCalorieText.setText("Daily Calorie Intake: " + dailyCalorieIntake);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private double calculateBMR(String gender, int age, int weight, int height) {
        // Place your BMR calculation logic here
        return gender.equals("Male") ? 10 * weight + 6.25 * height - 5 * age + 5 :
                10 * weight + 6.25 * height - 5 * age - 161;

    }
}