package com.aidfinder.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aidfinder.R;
import com.aidfinder.auth.db.DatabaseHelper;
import com.aidfinder.auth.model.UserProfile;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileGreeting;
    private RadioGroup rgBiologicalSex;
    private RadioButton rbFemale, rbMale;
    private EditText etAge, etHeightFeet, etHeightInches, etWeightLbs;
    private EditText etBloodPressure, etHeartRate, etActivityLevel;
    private DatabaseHelper databaseHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);

        tvProfileGreeting = findViewById(R.id.tvProfileGreeting);
        rgBiologicalSex = findViewById(R.id.rgBiologicalSex);
        rbFemale = findViewById(R.id.rbFemale);
        rbMale = findViewById(R.id.rbMale);
        etAge = findViewById(R.id.etAge);
        etHeightFeet = findViewById(R.id.etHeightFeet);
        etHeightInches = findViewById(R.id.etHeightInches);
        etWeightLbs = findViewById(R.id.etWeightLbs);
        etBloodPressure = findViewById(R.id.etBloodPressure);
        etHeartRate = findViewById(R.id.etHeartRate);
        etActivityLevel = findViewById(R.id.etActivityLevel);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.trim().isEmpty()) {
            username = "Guest";
        }
        tvProfileGreeting.setText(getString(R.string.msg_profile_greeting, username));
        loadUserProfile();

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void loadUserProfile() {
        UserProfile profile = databaseHelper.getUserProfile(username);
        if (profile != null) {
            String sex = profile.getBiologicalSex();
            if ("Male".equalsIgnoreCase(sex)) {
                rbMale.setChecked(true);
            } else if ("Female".equalsIgnoreCase(sex)) {
                rbFemale.setChecked(true);
            }

            if (profile.getAge() > 0) {
                etAge.setText(String.valueOf(profile.getAge()));
            }
            if (profile.getHeightFeet() > 0) {
                etHeightFeet.setText(String.valueOf(profile.getHeightFeet()));
            }
            if (profile.getHeightInches() > 0) {
                etHeightInches.setText(String.valueOf(profile.getHeightInches()));
            }
            if (profile.getWeightLbs() > 0) {
                etWeightLbs.setText(String.valueOf(profile.getWeightLbs()));
            }
            etBloodPressure.setText(profile.getBloodPressure());
            if (profile.getHeartRate() > 0) {
                etHeartRate.setText(String.valueOf(profile.getHeartRate()));
            }
            etActivityLevel.setText(profile.getActivityLevel());
        }
    }

    private void saveProfileData() {
        if (username == null || username.trim().isEmpty()) {
            username = "Guest";
        }

        String biologicalSex = "";
        if (rbMale.isChecked()) {
            biologicalSex = "Male";
        } else if (rbFemale.isChecked()) {
            biologicalSex = "Female";
        }

        String ageStr = etAge.getText().toString().trim();
        String ftStr = etHeightFeet.getText().toString().trim();
        String inStr = etHeightInches.getText().toString().trim();
        String weightLbsStr = etWeightLbs.getText().toString().trim();
        String bloodPressure = etBloodPressure.getText().toString().trim();
        String heartRateStr = etHeartRate.getText().toString().trim();
        String activityLevel = etActivityLevel.getText().toString().trim();

        int age = 0;
        int heightFeet = 0;
        int heightInches = 0;
        double weightLbs = 0.0;
        int heartRate = 0;

        try {
            if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr);
            if (!ftStr.isEmpty()) heightFeet = Integer.parseInt(ftStr);
            if (!inStr.isEmpty()) heightInches = Integer.parseInt(inStr);
            if (!weightLbsStr.isEmpty()) weightLbs = Double.parseDouble(weightLbsStr);
            if (!heartRateStr.isEmpty()) heartRate = Integer.parseInt(heartRateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.err_invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile profile = databaseHelper.getUserProfile(username);
        profile.setUsername(username);
        profile.setBiologicalSex(biologicalSex);
        profile.setAge(age);
        profile.setHeightFeet(heightFeet);
        profile.setHeightInches(heightInches);
        profile.setWeightLbs(weightLbs);
        profile.setBloodPressure(bloodPressure);
        profile.setHeartRate(heartRate);
        profile.setActivityLevel(activityLevel);

        boolean success = databaseHelper.saveUserProfile(profile);
        if (success) {
            Toast.makeText(this, R.string.msg_profile_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
        }
    }
}
