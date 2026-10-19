package com.aidfinder.auth.model;

public class UserProfile {
    private String username;
    private String email;
    private String biologicalSex; // "Female" or "Male"
    private int age;
    private int heightFeet;
    private int heightInches;
    private double weightLbs;
    private String bloodPressure;
    private int heartRate;
    private String activityLevel;

    public UserProfile() {
    }

    public UserProfile(String username, String email, String biologicalSex, int age,
                       int heightFeet, int heightInches, double weightLbs,
                       String bloodPressure, int heartRate, String activityLevel) {
        this.username = username;
        this.email = email;
        this.biologicalSex = biologicalSex;
        this.age = age;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.weightLbs = weightLbs;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.activityLevel = activityLevel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBiologicalSex() {
        return biologicalSex;
    }

    public void setBiologicalSex(String biologicalSex) {
        this.biologicalSex = biologicalSex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getHeightFeet() {
        return heightFeet;
    }

    public void setHeightFeet(int heightFeet) {
        this.heightFeet = heightFeet;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(int heightInches) {
        this.heightInches = heightInches;
    }

    public double getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(double weightLbs) {
        this.weightLbs = weightLbs;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public void updateBasicInfo(String biologicalSex, int age, int heightFeet, int heightInches, double weightLbs) {
        this.biologicalSex = biologicalSex;
        this.age = age;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.weightLbs = weightLbs;
    }

    public void updateVitals(String bloodPressure, int heartRate, String activityLevel) {
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.activityLevel = activityLevel;
    }
}
