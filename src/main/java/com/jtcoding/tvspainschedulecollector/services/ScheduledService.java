package com.jtcoding.tvspainschedulecollector.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class ScheduledService {

    private final TVDataCollectorService tvDataCollectorService;
    private final TVDataPersistService tvDataPersistService;
    private final MonitoringService monitoringService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledTask() throws IOException {
        log.info("[Scheduled task] Starting data collector process");
        monitoringService.setMoviesMetrics();
        monitoringService.setSeriesMetrics();
        var channels = tvDataCollectorService.processTVData();
        log.info("[Scheduled task] Data collector process finished");
        log.info("[Scheduled task] Saving data");
        tvDataPersistService.persistChannelsData(channels);
        log.info("[Scheduled task] Finishing Scheduled task");
    }
}
