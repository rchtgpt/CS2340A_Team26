package com.example.greenplate.views.mainFragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.example.greenplate.R;
import com.example.greenplate.viewmodels.InputMealViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class InputMealFragment extends Fragment {

    private Button storeMealBtn;
    private TextInputEditText mealNameInput, mealCaloriesInput;
    private InputMealViewModel inputMealViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_meal, container, false);

        storeMealBtn = view.findViewById(R.id.storeMealBtn);
        mealNameInput = view.findViewById(R.id.mealNameInput);
        mealCaloriesInput = view.findViewById(R.id.mealCaloriesInput);

        inputMealViewModel = new ViewModelProvider(this).get(InputMealViewModel.class);

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

            String userId = getCurrentUserId();
            if (userId == null) {
                Toast.makeText(getActivity(), "User not signed in", Toast.LENGTH_SHORT).show();
                return;
            }

            inputMealViewModel.storeMeal(userId, mealName, calories);
            mealNameInput.setText("");
            mealCaloriesInput.setText("");
        });

        return view;
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            return null;
        }
    }
}
