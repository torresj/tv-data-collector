package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.respositories.MovieRepository;
import com.jtcoding.tvspainschedulecollector.respositories.SerieRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;
    private final MeterRegistry meterRegistry;

    private final AtomicLong movies = new AtomicLong();
    private final AtomicLong series = new AtomicLong();

    public void setMoviesMetrics(){
        movies.set(movieRepository.count());
        meterRegistry.gauge("movies",movies);
    }

    public void setSeriesMetrics(){
        series.set(serieRepository.count());
        meterRegistry.gauge("series",series);
    }
}
