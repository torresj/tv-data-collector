package com.jtcoding.tvspainschedulecollector.dtos.tmdb;

import java.util.List;

public record Page(int page, int total_results, int total_pages, List<Result> results) {}
