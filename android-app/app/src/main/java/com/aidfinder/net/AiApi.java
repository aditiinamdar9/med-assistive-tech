package com.aidfinder.net;

import com.aidfinder.net.dto.RecommendRequest;
import com.aidfinder.net.dto.RecommendResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/** The whole contract with the Python service. Two endpoints, nothing else. */
public interface AiApi {

    @POST("recommend")
    Call<RecommendResponse> recommend(@Body RecommendRequest request);

    @GET("health")
    Call<Void> health();
}
