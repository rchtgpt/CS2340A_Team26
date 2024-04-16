package com.example.greenplate.views;

import static com.example.greenplate.viewmodels.RecipeViewModel.hasAllIngredients;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.greenplate.R;
import com.example.greenplate.models.Ingredient;
import com.example.greenplate.models.Recipe;
import com.example.greenplate.models.SingletonFirebase;
import com.example.greenplate.viewmodels.IngredientViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RecipeDetailActivity extends AppCompatActivity {

    private Button cookButton;
    private Recipe currentRecipe;
    private Map<String, Integer> userPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Button backButton = findViewById(R.id.backButton);
        cookButton = findViewById(R.id.cookButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize the userPantry map
        userPantry = new HashMap<>();

        // Load pantry data
        loadPantryData();

        // Try to get the complete Recipe object from the intent
        if (getIntent().hasExtra("RECIPE_OBJECT")) {
            currentRecipe = (Recipe) getIntent().getSerializableExtra("RECIPE_OBJECT");
            Log.d(TAG, currentRecipe.getTitle() + " " + currentRecipe.getIngredients() + " " + currentRecipe.getQuantity() + " " + currentRecipe.getIngredientQuantities());
            if (currentRecipe != null) {
                Log.d(TAG, "Recipe received: " + currentRecipe.getTitle());
            } else {
                Log.d(TAG, "Recipe object is null");
            }
        }

        if (currentRecipe == null) {
            Log.d(TAG, "No Recipe Data Available");
            showMessage("Error: No recipe data found.");
            finish();
            return;
        }

        // Display recipe details
        displayRecipeDetails(currentRecipe);

        cookButton.setOnClickListener(v -> {
            Log.d(TAG, "Cook button clicked.");
            if (userPantry != null && !userPantry.isEmpty()) {
                cookRecipe(currentRecipe);
            } else {
                Log.d(TAG, "Cannot cook: Pantry data is not available.");
                showMessage("Cannot cook: Pantry data is not available.");
            }
        });
    }

    private void displayRecipeDetails(Recipe recipe) {
        TextView titleTextView = findViewById(R.id.detailTitleTextView);
        TextView ingredientsTextView = findViewById(R.id.detailIngredientsTextView);
        TextView quantityTextView = findViewById(R.id.detailQuantityTextView);

        titleTextView.setText(recipe.getTitle());
        ingredientsTextView.setText(TextUtils.join(", ", recipe.getIngredients()));
        quantityTextView.setText(recipe.getQuantity());
    }


    private static final String TAG = "CookRecipeActivity";

    private void cookRecipe(Recipe recipe) {
        // Check if we have all ingredients in the required quantities
        Log.d(TAG, "1234");
        if (!hasAllIngredients(recipe, userPantry)) {
            showMessage("Not enough ingredients to cook this recipe.");
            return;
        }

        // Initialize the totalCalories as an AtomicInteger to be able to modify it inside the ValueEventListener
        AtomicInteger totalCalories = new AtomicInteger(0);

        // Create a CountDownLatch with the size of the ingredient quantities map
        CountDownLatch latch = new CountDownLatch(recipe.getIngredientQuantities().size());
        Log.d(TAG, recipe.getIngredientQuantities().toString());

//        Log.d("Yahaan", "123");

        for (Map.Entry<String, Integer> entry : recipe.getIngredientQuantities().entrySet()) {
            Log.d("Yahaan", "123");
            String ingredientName = entry.getKey();
            int requiredQuantity = entry.getValue();

            // Fetch the actual caloriesPerServing from the pantry
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("Ingredient name", ingredientName);
            DatabaseReference ingredientRef = SingletonFirebase.getInstance().getDatabaseReference()
                    .child("users").child(userId).child("pantry").child(ingredientName);


            ingredientRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.w(TAG, "PANTRIE " + ingredientName);
                        Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                        if (ingredient != null) {
                            // Multiply the calories per serving with the quantity required for the recipe
                            int calories = ingredient.getCaloriesPerServing() * requiredQuantity;
                            totalCalories.addAndGet(calories);

                            // Update the pantry with the new quantity
                            int newQuantity = userPantry.getOrDefault(ingredientName, 0) - requiredQuantity;
                            userPantry.put(ingredientName, newQuantity); // Store the updated quantity in the map
                            updateIngredientQuantityInPantry(ingredientName, newQuantity);
                            Log.w(TAG, "FOUNDDDD" + ingredientName);
                        }
                    } else {
                        Log.w(TAG, "Ingredient not found in pantry: " + ingredientName);
                    }
                    latch.countDown(); // Decrement the count of the latch
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to fetch ingredient: " + ingredientName, databaseError.toException());
                    latch.countDown(); // Decrement the count of the latch even on failure
                }
            });
        }

        // Wait for all data to be fetched
        try {
            latch.await(); // Wait for the latch to count down to zero
            // Now all data is fetched, and you can proceed to update meal history and meals
            addRecipeToMealHistory(recipe);
            updateUserMeals(recipe.getTitle(), totalCalories.get());
            updateCalorieCount(totalCalories.get());
            showMessage("Recipe cooked successfully!");
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted exception", e);
            Thread.currentThread().interrupt();
        }
    }

    private void updateUserMeals(String recipeTitle, int calories) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference userMealsRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("users").child(userId).child("meals").child(currentDate);

        // Remove push() to prevent generating a unique ID, set the value directly
        userMealsRef.child(recipeTitle).setValue(calories)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User meals updated successfully with title and calories."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating user meals: " + e.getMessage()));
    }



// Helper methods (will need to be implemented)

    private void updateIngredientQuantityInPantry(String ingredientName, int newQuantity) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference pantryRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("users").child(userId).child("pantry").child(ingredientName).child("quantity");

        pantryRef.setValue(newQuantity)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Pantry updated successfully for ingredient: " + ingredientName))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating pantry for ingredient: " + ingredientName + ": " + e.getMessage()));
    }


    private void addRecipeToMealHistory(Recipe recipe) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mealHistoryRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("users").child(userId).child("mealHistory");

        String mealId = mealHistoryRef.push().getKey();
        if (mealId != null) {
            mealHistoryRef.child(mealId).setValue(recipe)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Meal history updated successfully."))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating meal history: " + e.getMessage()));
        }
    }


    private void loadPantryData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference pantryRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("users").child(userId).child("pantry");

        pantryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPantry.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ingredient ingredient = snapshot.getValue(Ingredient.class);
                    if (ingredient != null) {
                        userPantry.put(ingredient.getName(), (int) ingredient.getQuantity());
                    }
                }
                Log.d(TAG, "Pantry data loaded: " + userPantry);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load pantry data: " + databaseError.getMessage());
            }
        });
    }



    private void updateCalorieCount(int totalCalories) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference calorieRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("users").child(userId).child("dailyCalories");

        calorieRef.setValue(totalCalories)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Calories updated successfully."))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating calories: " + e.getMessage()));
    }

    private void fetchIngredientDetails(String ingredientName, Consumer<Ingredient> callback) {
        DatabaseReference ingredientRef = SingletonFirebase.getInstance().getDatabaseReference()
                .child("ingredients").child(ingredientName);

        ingredientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                    if (ingredient != null) {
                        callback.accept(ingredient);
                        Log.d(TAG, "Ingredient fetched: " + ingredient.getName());
                    } else {
                        Log.d(TAG, "Ingredient data not converted.");
                        callback.accept(null);
                    }
                } else {
                    Log.d(TAG, "Ingredient not found in Firebase.");
                    callback.accept(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch ingredient details: " + databaseError.getMessage());
                callback.accept(null);
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
