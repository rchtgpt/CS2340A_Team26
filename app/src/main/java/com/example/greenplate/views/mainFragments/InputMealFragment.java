package com.example.greenplate.views.mainFragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.greenplate.R;
import com.example.greenplate.viewmodels.InputMealViewModel;


public class InputMealFragment extends Fragment {
    private InputMealViewModel mViewModel;
    private TextInputEditText nameEditText;
    private TextInputEditText caloriesEditText;
    private Button storeMealButton;

    public static InputMealFragment newInstance() {
        return new InputMealFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_input_meal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        mViewModel = new ViewModelProvider(getActivity()).get(InputMealViewModel.class);

        // Initialize UI components
        nameEditText = view.findViewById(R.id.mealNameInput);
        caloriesEditText = view.findViewById(R.id.mealCaloriesInput);
        storeMealButton = view.findViewById(R.id.storeMealBtn);

        storeMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mealName = nameEditText.getText().toString().trim();
                String calorieString = caloriesEditText.getText().toString().trim();
                if (!calorieString.isEmpty()) {
                    int calories = Integer.parseInt(calorieString);
                    mViewModel.storeMeal(mealName, calories);

                    // After submission, reset the input fields or show a message to the user
                    nameEditText.setText("");
                    caloriesEditText.setText("");
                }
            }
        });
    }
}