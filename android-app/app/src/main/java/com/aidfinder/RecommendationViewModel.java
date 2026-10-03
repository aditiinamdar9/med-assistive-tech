package com.aidfinder;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aidfinder.catalog.CatalogRepository;
import com.aidfinder.catalog.Product;
import com.aidfinder.net.ApiClient;
import com.aidfinder.net.dto.CatalogItemDto;
import com.aidfinder.net.dto.RecommendRequest;
import com.aidfinder.net.dto.RecommendResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Holds the search state. Living in a ViewModel means results survive a screen
 * rotation instead of vanishing and re-charging you for another API call.
 */
public class RecommendationViewModel extends AndroidViewModel {

    private static final String TAG = "RecommendationVM";

    public enum State { IDLE, LOADING, SUCCESS, EMPTY, OFFLINE, ERROR }

    private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
    private final MutableLiveData<List<RecommendationResult>> results =
            new MutableLiveData<>(new ArrayList<>());

    public RecommendationViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<State> getState() { return state; }
    public LiveData<List<RecommendationResult>> getResults() { return results; }

    public void search(String userText) {
        CatalogRepository catalog = CatalogRepository.get(getApplication());

        List<CatalogItemDto> payload = new ArrayList<>();
        for (Product p : catalog.all()) {
            payload.add(new CatalogItemDto(p.id, p.name, p.category, p.tags, p.description));
        }

        state.setValue(State.LOADING);

        ApiClient.get().recommend(new RecommendRequest(userText, payload))
                .enqueue(new Callback<RecommendResponse>() {

            @Override
            public void onResponse(@NonNull Call<RecommendResponse> call,
                                   @NonNull Response<RecommendResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Service returned " + response.code());
                    state.setValue(State.ERROR);
                    return;
                }

                List<RecommendationResult> matched = new ArrayList<>();
                List<RecommendResponse.Recommendation> recs = response.body().recommendations;

                if (recs != null) {
                    for (RecommendResponse.Recommendation r : recs) {
                        Product p = catalog.findById(r.id);
                        if (p != null) {
                            matched.add(new RecommendationResult(p, r.why));
                        } else {
                            // The model named something that is not in our catalog.
                            // Drop it silently - the user never sees a fake product.
                            Log.w(TAG, "Dropped unknown product id: " + r.id);
                        }
                    }
                }

                results.setValue(matched);
                state.setValue(matched.isEmpty() ? State.EMPTY : State.SUCCESS);
            }

            @Override
            public void onFailure(@NonNull Call<RecommendResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Request failed", t);
                state.setValue(t instanceof IOException ? State.OFFLINE : State.ERROR);
            }
        });
    }
}
