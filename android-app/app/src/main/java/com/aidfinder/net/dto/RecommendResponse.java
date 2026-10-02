package com.aidfinder.net.dto;

import java.util.List;

public class RecommendResponse {
    public List<Recommendation> recommendations;
    public String disclaimer;

    public static class Recommendation {
        public String id;
        public String why;
    }
}
