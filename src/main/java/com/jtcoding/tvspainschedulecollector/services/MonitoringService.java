package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.respositories.MovieRepository;
import com.jtcoding.tvspainschedulecollector.respositories.SerieRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@AllArgsConstructor
@Slf4j
public class MonitoringService {
    private final MovieRepository movieRepository;
    private final SerieRepository serieRepository;
    private final MeterRegistry meterRegistry;

    public void setMoviesMetrics(){
        meterRegistry.gauge("movies",new AtomicLong(movieRepository.count()));
    }

    public void setSeriesMetrics(){
        meterRegistry.gauge("series",new AtomicLong(serieRepository.count()));
    }
}
