package com.jtcoding.tvspainschedulecollector.dtos.tmdb;

import java.time.LocalDate;
import java.util.Locale;

public record Result(
        String original_title,
        String poster_path,
        double popularity,
        String overview,
        String title,
        double vote_average,
        LocalDate release_date
) {
}
