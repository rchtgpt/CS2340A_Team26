package com.example.greenplate.viewmodels;

import androidx.lifecycle.ViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.greenplate.models.Meal;

public class InputMealViewModel extends ViewModel {
    private DatabaseReference mDatabase;

    public InputMealViewModel() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void storeMeal(String mealName, int calories) {
        Meal newMeal = new Meal(mealName, calories);
        String mealId = mDatabase.child("meals").push().getKey();
        mDatabase.child("meals").child(mealId).setValue(newMeal)
                .addOnSuccessListener(aVoid -> {
                    // Handle successful data storage on button clikc
                })
                .addOnFailureListener(e -> {
                    // Handle the error if submission is not successsful.
                });
    }
}
