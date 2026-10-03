package com.aidfinder;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aidfinder.databinding.ActivityMainBinding;
import com.aidfinder.ui.RecommendationAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding views;
    private RecommendationViewModel model;
    private final RecommendationAdapter adapter = new RecommendationAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        views.results.setLayoutManager(new LinearLayoutManager(this));
        views.results.setAdapter(adapter);

        model = new ViewModelProvider(this).get(RecommendationViewModel.class);
        model.getState().observe(this, this::render);
        model.getResults().observe(this, adapter::submit);

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
        // Make sure screen readers announce it rather than leaving it silent
        views.message.announceForAccessibility(text);
    }
}
