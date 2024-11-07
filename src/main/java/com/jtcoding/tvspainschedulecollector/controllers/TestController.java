package com.jtcoding.tvspainschedulecollector.controllers;

import com.jtcoding.tvspainschedulecollector.dtos.ChannelDTO;
import com.jtcoding.tvspainschedulecollector.dtos.tmdb.Page;
import com.jtcoding.tvspainschedulecollector.entities.MovieEntity;
import com.jtcoding.tvspainschedulecollector.enums.EventType;
import com.jtcoding.tvspainschedulecollector.respositories.MovieRepository;
import com.jtcoding.tvspainschedulecollector.services.MonitoringService;
import com.jtcoding.tvspainschedulecollector.services.TMDBApiClient;
import com.jtcoding.tvspainschedulecollector.services.TVDataCollectorService;
import com.jtcoding.tvspainschedulecollector.services.TVDataPersistService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class TestController {
  private final TVDataCollectorService tvDataCollectorService;
  private final TVDataPersistService tvDataPersistService;
  private final TMDBApiClient tmdbApiClient;
  private final MovieRepository movieRepository;
  private final MonitoringService monitoringService;

  @GetMapping("/test/channels")
  public ResponseEntity<List<ChannelDTO>> getChannels() throws IOException {
    log.info("[Get channels] Starting data collector process");
    var channels = tvDataCollectorService.processTVData();
    log.info("[Get channels] Data collector process finished");
    log.info("[Get channels] Saving data");
    tvDataPersistService.persistChannelsData(channels);
    monitoringService.setMoviesMetrics();
    monitoringService.setSeriesMetrics();
    log.info("[Get channels] Finishing task");
    return ResponseEntity.ok(channels);
  }

  @GetMapping("/test/tmdb/search")
  public ResponseEntity<Page> getTMDBInfo(
      @RequestParam String query, @RequestParam EventType type) {
    return ResponseEntity.ok(
        type.equals(EventType.MOVIE)
            ? tmdbApiClient.searchMovie(Map.of("query", query, "language", "es-ES"))
            : tmdbApiClient.searchTV(Map.of("query", query, "language", "es-ES")));
  }

  @GetMapping("/test/metrics")
  public ResponseEntity<String> metrics() throws IOException {
    movieRepository.save(MovieEntity.builder()
            .name("test")
            .id(1234L)
            .classification("")
            .rate(1.0)
            .synopsis("")
            .interpreters("")
            .imageUrl("")
            .director("")
            .build());
    movieRepository.save(MovieEntity.builder()
            .name("test")
            .id(2234L)
            .classification("")
            .rate(1.0)
            .synopsis("")
            .interpreters("")
            .imageUrl("")
            .director("")
            .build());
    monitoringService.setMoviesMetrics();
    return ResponseEntity.ok("OK");
  }
}
