package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.dtos.tmdb.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface TMDBApiClient {
    @GetExchange("/search/movie")
    Page searchMovie(@RequestParam Map<String, String> params);

    @GetExchange("/search/tv")
    Page searchTV(@RequestParam Map<String, String> params);
}
