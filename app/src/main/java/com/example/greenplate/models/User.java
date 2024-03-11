package com.example.greenplate.models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private Map<String, Integer> meals;

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.meals = new HashMap<>();
    }

    // Getter for firstName
    public String getFirstName() {
        return firstName;
    }

    // Setter for firstName
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter for lastName
    public String getLastName() {
        return lastName;
    }

    // Setter for lastName
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Integer> getMeals() {
        return meals;
    }

    public void setMeals(Map<String, Integer> meals) {
        this.meals = meals;
    }

    public void addMeal(String mealName, Integer calories) {
        this.meals.put(mealName, calories);
    }
}
