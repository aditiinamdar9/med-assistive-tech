package com.aidfinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aidfinder.R;
import com.aidfinder.RecommendationResult;
import com.aidfinder.auth.db.DatabaseHelper;
import com.aidfinder.auth.model.Favorite;
import com.aidfinder.auth.model.UserProfile;
import com.aidfinder.catalog.CatalogRepository;
import com.aidfinder.catalog.Product;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvProfileGreeting;
    private TextView tvSummarySex, tvSummaryAge, tvSummaryHeight, tvSummaryWeight;
    private TextView tvSummaryBP, tvSummaryHeartRate, tvSummaryActivity;

    private LinearLayout layoutSummaryMode, layoutEditMode;
    private RadioGroup rgBiologicalSex;
    private RadioButton rbFemale, rbMale;
    private EditText etAge, etHeightFeet, etHeightInches, etWeightLbs;
    private EditText etBloodPressure, etHeartRate, etActivityLevel;

    private RecyclerView rvFavorites;
    private TextView tvNoFavorites;
    private RecommendationAdapter favoritesAdapter;

    private DatabaseHelper databaseHelper;
    private String username = "Saanvi";

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String username) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String argUser = getArguments().getString("USERNAME");
            if (argUser != null && !argUser.trim().isEmpty()) {
                username = argUser;
            }
        }
        if (username == null || username.trim().isEmpty() || "Guest".equals(username)) {
            String sessionUser = com.aidfinder.auth.UserSession.getUsername(requireContext());
            if (!sessionUser.isEmpty()) {
                username = sessionUser;
            }
        }

        databaseHelper = new DatabaseHelper(requireContext());

        tvProfileGreeting = view.findViewById(R.id.tvProfileGreeting);
        layoutSummaryMode = view.findViewById(R.id.layoutSummaryMode);
        layoutEditMode = view.findViewById(R.id.layoutEditMode);

        tvSummarySex = view.findViewById(R.id.tvSummarySex);
        tvSummaryAge = view.findViewById(R.id.tvSummaryAge);
        tvSummaryHeight = view.findViewById(R.id.tvSummaryHeight);
        tvSummaryWeight = view.findViewById(R.id.tvSummaryWeight);
        tvSummaryBP = view.findViewById(R.id.tvSummaryBP);
        tvSummaryHeartRate = view.findViewById(R.id.tvSummaryHeartRate);
        tvSummaryActivity = view.findViewById(R.id.tvSummaryActivity);

        rgBiologicalSex = view.findViewById(R.id.rgBiologicalSex);
        rbFemale = view.findViewById(R.id.rbFemale);
        rbMale = view.findViewById(R.id.rbMale);
        etAge = view.findViewById(R.id.etAge);
        etHeightFeet = view.findViewById(R.id.etHeightFeet);
        etHeightInches = view.findViewById(R.id.etHeightInches);
        etWeightLbs = view.findViewById(R.id.etWeightLbs);
        etBloodPressure = view.findViewById(R.id.etBloodPressure);
        etHeartRate = view.findViewById(R.id.etHeartRate);
        etActivityLevel = view.findViewById(R.id.etActivityLevel);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        Button btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        Button btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);

        View cardBasicInfo = view.findViewById(R.id.cardBasicInfo);
        View cardVitalsInfo = view.findViewById(R.id.cardVitalsInfo);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        tvNoFavorites = view.findViewById(R.id.tvNoFavorites);

        tvProfileGreeting.setText(getString(R.string.msg_profile_greeting, username));

        loadAndDisplayProfileData();
        setUpFavorites();

        View.OnClickListener startEditListener = v -> showEditMode(true);
        btnEditProfile.setOnClickListener(startEditListener);
        cardBasicInfo.setOnClickListener(startEditListener);
        cardVitalsInfo.setOnClickListener(startEditListener);

        btnCancelEdit.setOnClickListener(v -> showEditMode(false));
        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        btnLogoutProfile.setOnClickListener(v -> {
            com.aidfinder.auth.UserSession.clearSession(requireContext());
            android.content.Intent intent = new android.content.Intent(requireContext(), com.aidfinder.auth.LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void loadAndDisplayProfileData() {
        UserProfile profile = databaseHelper.getUserProfile(username);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUsername(username);
        }

        String sex = profile.getBiologicalSex();
        if ("Male".equalsIgnoreCase(sex)) {
            rbMale.setChecked(true);
            tvSummarySex.setText("Biological Sex: Male");
        } else if ("Female".equalsIgnoreCase(sex)) {
            rbFemale.setChecked(true);
            tvSummarySex.setText("Biological Sex: Female");
        } else {
            tvSummarySex.setText("Biological Sex: Not specified");
        }

        if (profile.getAge() > 0) {
            etAge.setText(String.valueOf(profile.getAge()));
            tvSummaryAge.setText("Age: " + profile.getAge() + " years");
        } else {
            tvSummaryAge.setText("Age: Not set");
        }

        if (profile.getHeightFeet() > 0 || profile.getHeightInches() > 0) {
            etHeightFeet.setText(profile.getHeightFeet() > 0 ? String.valueOf(profile.getHeightFeet()) : "");
            etHeightInches.setText(profile.getHeightInches() > 0 ? String.valueOf(profile.getHeightInches()) : "");
            tvSummaryHeight.setText("Height: " + profile.getHeightFeet() + " ft " + profile.getHeightInches() + " in");
        } else {
            tvSummaryHeight.setText("Height: Not set");
        }

        if (profile.getWeightLbs() > 0) {
            etWeightLbs.setText(String.valueOf(profile.getWeightLbs()));
            tvSummaryWeight.setText("Weight: " + profile.getWeightLbs() + " lbs");
        } else {
            tvSummaryWeight.setText("Weight: Not set");
        }

        if (profile.getBloodPressure() != null && !profile.getBloodPressure().isEmpty()) {
            etBloodPressure.setText(profile.getBloodPressure());
            tvSummaryBP.setText("Blood Pressure: " + profile.getBloodPressure() + " mmHg");
        } else {
            tvSummaryBP.setText("Blood Pressure: Not set");
        }

        if (profile.getHeartRate() > 0) {
            etHeartRate.setText(String.valueOf(profile.getHeartRate()));
            tvSummaryHeartRate.setText("Heart Rate: " + profile.getHeartRate() + " bpm");
        } else {
            tvSummaryHeartRate.setText("Heart Rate: Not set");
        }

        if (profile.getActivityLevel() != null && !profile.getActivityLevel().isEmpty()) {
            etActivityLevel.setText(profile.getActivityLevel());
            tvSummaryActivity.setText("Activity Level: " + profile.getActivityLevel());
        } else {
            tvSummaryActivity.setText("Activity Level: Not set");
        }
    }

    private void setUpFavorites() {
        favoritesAdapter = new RecommendationAdapter(databaseHelper, username);
        favoritesAdapter.setOnFavoriteChanged(this::loadFavorites);
        favoritesAdapter.setOnItemClickListener(item -> startActivity(
                DeviceJournalActivity.newIntent(requireContext(), item.product.id, item.product.name)));

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(favoritesAdapter);

        loadFavorites();
    }

    private void loadFavorites() {
        CatalogRepository catalog = CatalogRepository.get(requireContext());
        List<Favorite> favorites = databaseHelper.getFavorites(username);

        List<RecommendationResult> results = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Product product = catalog.findById(favorite.getProductId());
            if (product == null) {
                // Catalog changed since this was favorited - show what we saved
                // instead of losing the entry.
                product = new Product();
                product.id = favorite.getProductId();
                product.name = favorite.getProductName();
                product.description = "";
            }
            results.add(new RecommendationResult(product, favorite.getWhy()));
        }

        favoritesAdapter.submit(results);
        tvNoFavorites.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showEditMode(boolean enableEdit) {
        if (enableEdit) {
            layoutSummaryMode.setVisibility(View.GONE);
            layoutEditMode.setVisibility(View.VISIBLE);
        } else {
            layoutEditMode.setVisibility(View.GONE);
            layoutSummaryMode.setVisibility(View.VISIBLE);
        }
    }

    private void saveProfileData() {
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
            Toast.makeText(requireContext(), R.string.err_invalid_number, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), R.string.msg_profile_saved, Toast.LENGTH_SHORT).show();
            loadAndDisplayProfileData();
            showEditMode(false);
        } else {
            Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
        }
    }
}
