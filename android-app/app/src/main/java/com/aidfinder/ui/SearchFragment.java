package com.aidfinder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aidfinder.R;
import com.aidfinder.RecommendationViewModel;
import com.aidfinder.auth.UserSession;
import com.aidfinder.auth.db.DatabaseHelper;
import com.aidfinder.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding views;
    private RecommendationViewModel model;
    private RecommendationAdapter adapter;

    public SearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        views = FragmentSearchBinding.inflate(inflater, container, false);
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String username = UserSession.getUsername(requireContext());
        adapter = new RecommendationAdapter(new DatabaseHelper(requireContext()), username);

        views.results.setLayoutManager(new LinearLayoutManager(requireContext()));
        views.results.setAdapter(adapter);

        model = new ViewModelProvider(this).get(RecommendationViewModel.class);
        model.getState().observe(getViewLifecycleOwner(), this::render);
        model.getResults().observe(getViewLifecycleOwner(), adapter::submit);

        views.findButton.setOnClickListener(v -> {
            String text = views.storyInput.getText() == null
                    ? "" : views.storyInput.getText().toString().trim();

            if (text.isEmpty()) {
                showMessage(getString(R.string.empty_input));
                return;
            }
            model.search(text);
        });
    }

    private void render(@NonNull RecommendationViewModel.State state) {
        boolean loading = state == RecommendationViewModel.State.LOADING;

        views.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        views.findButton.setEnabled(!loading);
        views.findButton.setText(loading ? R.string.searching : R.string.find_button);
        views.message.setVisibility(View.GONE);
        views.results.setVisibility(View.GONE);

        switch (state) {
            case SUCCESS:
                views.results.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                showMessage(getString(R.string.no_matches));
                break;
            case OFFLINE:
                showMessage(getString(R.string.offline));
                break;
            case ERROR:
                showMessage(getString(R.string.service_down));
                break;
            default:
                break;
        }
    }

    private void showMessage(String text) {
        views.message.setText(text);
        views.message.setVisibility(View.VISIBLE);
        views.message.announceForAccessibility(text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        views = null;
    }
}
