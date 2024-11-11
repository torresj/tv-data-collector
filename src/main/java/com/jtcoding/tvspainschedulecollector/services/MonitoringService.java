package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.respositories.MovieRepository;
import com.jtcoding.tvspainschedulecollector.respositories.SerieRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MonitoringService {
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;
    private final MeterRegistry meterRegistry;

    public void setMoviesMetrics(){
        long movies = movieRepository.count();
        log.info("Movies: {}", movies);
        meterRegistry.gauge("movies",movies);
    }

    public void setSeriesMetrics(){
        long series = serieRepository.count();
        log.info("Serie: {}", series);
        meterRegistry.gauge("series",series);
    }
}
