package com.aidfinder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aidfinder.MainActivity;
import com.aidfinder.R;

public class HomeFragment extends Fragment {

    private String username = "Saanvi";

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String username) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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

        TextView tvHomeWelcome = view.findViewById(R.id.tvHomeWelcome);
        Button btnQuickSearch = view.findViewById(R.id.btnQuickSearch);
        Button btnQuickProfile = view.findViewById(R.id.btnQuickProfile);

        tvHomeWelcome.setText(getString(R.string.msg_welcome, username));

        btnQuickSearch.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.navigation_search);
            }
        });

        btnQuickProfile.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.navigation_profile);
            }
        });
    }
}
