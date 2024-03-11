package com.example.greenplate.models;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String height;
    private String weight;
    private String gender;

    public User(String firstName, String lastName, String email, String height, String weight, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
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

    public String getHeight() {return height;}

    public void setHeight(String height) {this.height = height;}

    public String getWeight() {return weight;}

    public void setWeight(String weight) {this.weight = weight;}

    public String getGender() {return gender;}

    public void setGender(String gender) {this.gender = gender;}



}