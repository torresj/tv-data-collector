package com.jtcoding.tvspainschedulecollector.services.impl;

import com.jtcoding.tvspainschedulecollector.dtos.*;
import com.jtcoding.tvspainschedulecollector.dtos.tmdb.Result;
import com.jtcoding.tvspainschedulecollector.enums.EventType;
import com.jtcoding.tvspainschedulecollector.respositories.ChapterRepository;
import com.jtcoding.tvspainschedulecollector.respositories.MovieRepository;
import com.jtcoding.tvspainschedulecollector.respositories.SerieRepository;
import com.jtcoding.tvspainschedulecollector.services.TMDBApiClient;
import com.jtcoding.tvspainschedulecollector.services.TVDataCollectorService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TVGuiaDataCollectorService implements TVDataCollectorService {

    private TMDBApiClient tmdbApiClient;
    private MovieRepository movieRepository;
    private SerieRepository serieRepository;
    private ChapterRepository chapterRepository;

    private static final String TV_GUIA_URL = "https://www.tvguia.es";

    private static final String TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/original";

    private static final Map<String, String> HEADERS =
            Map.ofEntries(
                    new AbstractMap.SimpleEntry<>("authority", "www.tvguia.es"),
                    new AbstractMap.SimpleEntry<>(
                            "accept",
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"),
                    new AbstractMap.SimpleEntry<>("accept-language", "es-ES,es;q=0.9,en;q=0.8"),
                    new AbstractMap.SimpleEntry<>("cache-control", "max-age=0"),
                    new AbstractMap.SimpleEntry<>(
                            "sec-ch-ua",
                            "\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\""),
                    new AbstractMap.SimpleEntry<>("ec-ch-ua-mobile", "?0"),
                    new AbstractMap.SimpleEntry<>("sec-ch-ua-platform", "\"Linux\""),
                    new AbstractMap.SimpleEntry<>("sec-fetch-dest", "document"),
                    new AbstractMap.SimpleEntry<>("sec-fetch-mode", "navigate"),
                    new AbstractMap.SimpleEntry<>("sec-fetch-site", "none"),
                    new AbstractMap.SimpleEntry<>("sec-fetch-user", "?1"),
                    new AbstractMap.SimpleEntry<>("upgrade-insecure-requests", "1"),
                    new AbstractMap.SimpleEntry<>(
                            "user-agent",
                            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"));

    @Override
    public List<ChannelDTO> processTVData() throws IOException {
        log.info("[TVGuiaDataCollector] Calling TVGuia to get channels data");
        var channels = new ArrayList<ChannelDTO>();

        // Getting list of channels
        var channelsPath = channelsPath();

        // Getting channel data
        for (String path : channelsPath) {
            addChannelData(path, channels);
        }
        return channels;
    }

    private List<String> channelsPath() throws IOException {
        var webPage = Jsoup.connect(TV_GUIA_URL).headers(HEADERS).get();

        Elements divChannels = webPage.select(".program-grid > div");

        return divChannels.stream()
                .map(
                        divChannel ->
                                divChannel
                                        .getElementsByClass("tvlogo")
                                        .get(0)
                                        .getElementsByTag("a")
                                        .get(0)
                                        .attr("href"))
                .toList();
    }

    private void addChannelData(String channelPath, List<ChannelDTO> channels) throws IOException {
        try {
            channels.add(getChannel(channelPath));
        } catch (HttpStatusException e) {
            log.error("Error getting channel data for " + channelPath);
        }
    }

    private ChannelDTO getChannel(String channelPath) throws IOException {
        // Today Events
        log.info("[TVGuiaDataCollector] Calling " + channelPath);
        var channelPage = Jsoup.connect(TV_GUIA_URL + channelPath).headers(HEADERS).get();
        String logoUrl = channelPage.getElementById("logo").child(0).attr("src");
        String name = channelPage.getElementById("title").child(0).text().substring(13);
        ChannelDTO channelDTO = new ChannelDTO(name, logoUrl, new ArrayList<>());

        log.info("[TVGuiaDataCollector] getting events for channel " + name);
        channelDTO.getEvents().addAll(getEvents(channelPage, false));

        try {
            // Tomorrow
            String channelTomorrowPath = channelPath.replace("tv", "tv-manana");
            log.info("[TVGuiaDataCollector] Calling " + channelTomorrowPath);
            var channelPageTomorrow =
                    Jsoup.connect(TV_GUIA_URL + channelTomorrowPath).headers(HEADERS).get();
            log.info("[TVGuiaDataCollector] getting tomorrow events for channel " + name);
            channelDTO.getEvents().addAll(getEvents(channelPageTomorrow, true));
        } catch (HttpStatusException | SSLHandshakeException e) {
            log.warn("Error getting events for tomorrow in channel " + name);
        }

        log.info("[TVGuiaDataCollector] Events added to channel " + name);
        return channelDTO;
    }

    private List<EventDTO> getEvents(Document channelPage, boolean isTomorrow) throws IOException {
        var events = new ArrayList<EventDTO>();
        channelPage.getElementsByClass("channel-programs-title").stream()
                .filter(element -> !element.getElementsByClass("text_bg_peliculas").isEmpty())
                .map(element -> element.getElementsByTag("a"))
                .forEach(
                        element -> {
                            try {
                                String path = element.attr("href");
                                events.add(getMovieEvent(path, isTomorrow));
                            } catch (Exception e) {
                                log.warn("Ignoring event " + element.text());
                            }
                        });

        channelPage.getElementsByClass("channel-programs-title").stream()
                .filter(element -> !element.getElementsByClass("text_bg_series").isEmpty())
                .map(element -> element.getElementsByTag("a"))
                .forEach(
                        element -> {
                            try {
                                String path = element.attr("href");
                                events.add(getSerieEvent(path, isTomorrow));
                            } catch (Exception e) {
                                log.warn("Ignoring event " + element.text());
                            }
                        });

        channelPage.getElementsByClass("channel-programs-title").stream()
                .filter(element -> !element.getElementsByClass("text_bg_deportes").isEmpty())
                .map(element -> element.getElementsByTag("a"))
                .forEach(
                        element -> {
                            try {
                                String path = element.attr("href");
                                events.add(getSportEvent(path, isTomorrow));
                            } catch (Exception e) {
                                log.warn("Ignoring event " + element.text());
                            }
                        });

        return events;
    }

    private EventDTO getSportEvent(String eventPath, boolean isTomorrow) throws IOException {
        var eventPage = Jsoup.connect(TV_GUIA_URL + eventPath).headers(HEADERS).get();
        String name = eventPage.getElementsByClass("program-title").get(0).text();
        String eventHours = eventPage.getElementsByClass("program-hour").get(0).text();
        var hours = eventHours.replace(" ", "").split("-");
        String startStringTime = hours[0];
        String endStringTime = hours.length > 1 ? hours[1] : hours[0];
        LocalTime startTime =
                parseTime(startStringTime.length() < 5 ? "0" + startStringTime : startStringTime);
        LocalTime endTime = parseTime(endStringTime.length() < 5 ? "0" + endStringTime : endStringTime);
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime end =
                LocalDateTime.of(
                        startTime.isAfter(endTime) ? LocalDate.now().plusDays(1) : LocalDate.now(), endTime);
        return EventDTO.builder()
                .start(isTomorrow ? start.plusDays(1) : start)
                .end(isTomorrow ? end.plusDays(1) : end)
                .duration(start.until(end, ChronoUnit.MINUTES))
                .eventType(EventType.SPORT)
                .sport(getSport(eventPage, name))
                .build();
    }

    private EventDTO getMovieEvent(String eventPath, boolean isTomorrow) throws IOException {
        var eventPage = Jsoup.connect(TV_GUIA_URL + eventPath).headers(HEADERS).get();
        String name = eventPage.getElementsByClass("program-title").get(0).text();
        String eventHours = eventPage.getElementsByClass("program-hour").get(0).text();
        Integer year = yearExtractor(eventPage.getElementsByTag("p").get(0).text());
        var hours = eventHours.replace(" ", "").split("-");
        String startStringTime = hours[0];
        String endStringTime = hours.length > 1 ? hours[1] : hours[0];
        LocalTime startTime =
                parseTime(startStringTime.length() < 5 ? "0" + startStringTime : startStringTime);
        LocalTime endTime = parseTime(endStringTime.length() < 5 ? "0" + endStringTime : endStringTime);
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime end =
                LocalDateTime.of(
                        startTime.isAfter(endTime) ? LocalDate.now().plusDays(1) : LocalDate.now(), endTime);
        return EventDTO.builder()
                .start(isTomorrow ? start.plusDays(1) : start)
                .end(isTomorrow ? end.plusDays(1) : end)
                .duration(start.until(end, ChronoUnit.MINUTES))
                .eventType(EventType.MOVIE)
                .movie(getMovie(eventPage, name, year))
                .build();
    }

    private SportDTO getSport(Document eventPage, String name) {
        String classification = eventPage.getElementsByTag("p").get(0).text();
        String synopsis = "";
        try {
            synopsis = eventPage.getElementsByTag("p").get(1).text();
        } catch (Exception e) {
            log.warn("No synopsis for " + name + " in TVGuia");
        }
        return SportDTO.builder()
                .name(name)
                .classification(classification)
                .synopsis(synopsis)
                .build();

    }

    private MovieDTO getMovie(Document eventPage, String name, Integer year) {
        var movieEntity = movieRepository.findByName(name);
        var tmdbData = getDataFromTMDB(name, EventType.MOVIE, year);
        var rate = tmdbData != null ? tmdbData.vote_average() : null;
        var tmdbSynopsis = tmdbData != null ? tmdbData.overview() : null;
        var tmdbPoster = tmdbData != null && tmdbData.poster_path() != null ? TMDB_POSTER_BASE_URL + tmdbData.poster_path() : null;
        if (movieEntity.isPresent()) {
            return MovieDTO.builder()
                    .name(name)
                    .classification(movieEntity.get().getClassification())
                    .interpreters(movieEntity.get().getInterpreters())
                    .director(movieEntity.get().getDirector())
                    .imageUrl(movieEntity.get().getImageUrl())
                    .synopsis(movieEntity.get().getSynopsis())
                    .rate(movieEntity.get().getRate() == null ? rate : movieEntity.get().getRate())
                    .build();
        } else {
            String classification = eventPage.getElementsByTag("p").get(0).text();
            String synopsis = "";
            try {
                synopsis = eventPage.getElementsByTag("p").get(1).text();
            } catch (Exception e) {
                log.warn("No synopsis for " + name + " in TVGuia");
            }
            String director = "";
            String interpreters = "";
            try {
                director = eventPage.getElementsByClass("program-area-2").get(0).textNodes().get(0).text();
            } catch (Exception e) {
                log.warn("No director for  " + name);
            }
            try {
                interpreters =
                        eventPage.getElementsByClass("program-area-2").get(0).textNodes().get(1).text();
            } catch (Exception e) {
                log.warn("No interpreters for " + name);
            }
            String imageUrl = "";
            try {
                imageUrl = eventPage.getElementsByAttributeValue("title", name).get(0).attr("src");
            } catch (Exception e) {
                log.warn("No image for " + name + " in TVGuia");
            }

            return MovieDTO.builder()
                    .name(name)
                    .classification(classification)
                    .interpreters(interpreters)
                    .director(director)
                    .imageUrl(tmdbPoster != null ? tmdbPoster : imageUrl)
                    .synopsis(tmdbSynopsis != null ? tmdbSynopsis : synopsis)
                    .rate(rate)
                    .build();
        }
    }

    private EventDTO getSerieEvent(String eventPath, boolean isTomorrow) throws IOException {
        var eventPage = Jsoup.connect(TV_GUIA_URL + eventPath).headers(HEADERS).get();
        String name = eventPage.getElementsByClass("program-title").get(0).text();
        String chapter = eventPage.getElementsByClass("program-element-bold").text();
        String eventHours = eventPage.getElementsByClass("program-hour").get(0).text();
        var hours = eventHours.replace(" ", "").split("-");
        String startStringTime = hours[0];
        String endStringTime = hours.length > 1 ? hours[1] : hours[0];
        LocalTime startTime =
                parseTime(startStringTime.length() < 5 ? "0" + startStringTime : startStringTime);
        LocalTime endTime = parseTime(endStringTime.length() < 5 ? "0" + endStringTime : endStringTime);
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime end =
                LocalDateTime.of(
                        startTime.isAfter(endTime) ? LocalDate.now().plusDays(1) : LocalDate.now(), endTime);
        return EventDTO.builder()
                .start(isTomorrow ? start.plusDays(1) : start)
                .end(isTomorrow ? end.plusDays(1) : end)
                .duration(start.until(end, ChronoUnit.MINUTES))
                .eventType(EventType.SERIE)
                .serie(getChapter(eventPage, name, chapter))
                .build();
    }

    private ChapterDTO getChapter(Document eventPage, String name, String chapterName) {
        var serie = serieRepository.findByName(name);
        var tmdbData = getDataFromTMDB(name, EventType.SERIE, null);
        var rate = tmdbData != null ? tmdbData.vote_average() : null;
        var tmdbPoster = tmdbData != null && tmdbData.poster_path() != null ? TMDB_POSTER_BASE_URL + tmdbData.poster_path() : null;
        if (serie.isPresent()) {
            var chapter = chapterRepository.findBySerieIdAndChapterName(serie.get().getId(), chapterName);
            if (chapter.isPresent()) {
                return ChapterDTO.builder()
                        .serieDTO(
                                SerieDTO.builder()
                                        .classification(serie.get().getClassification())
                                        .rate(rate)
                                        .director(serie.get().getDirector())
                                        .imageUrl(serie.get().getImageUrl())
                                        .interpreters(serie.get().getInterpreters())
                                        .name(name)
                                        .build())
                        .chapterName(chapterName)
                        .synopsis(chapter.get().getSynopsis())
                        .build();
            } else {
                String synopsis = "";
                try {
                    synopsis = eventPage.getElementsByTag("p").get(1).text();
                    if (serie.get().getClassification().equals(synopsis)
                            && eventPage.getElementsByTag("p").size() >= 3) {
                        synopsis = eventPage.getElementsByTag("p").get(2).text();
                    }
                } catch (Exception e) {
                    log.warn("No synopsis for " + name);
                }
                return ChapterDTO.builder()
                        .serieDTO(
                                SerieDTO.builder()
                                        .classification(serie.get().getClassification())
                                        .rate(rate)
                                        .director(serie.get().getDirector())
                                        .imageUrl(serie.get().getImageUrl())
                                        .interpreters(serie.get().getInterpreters())
                                        .name(name)
                                        .build())
                        .chapterName(chapterName)
                        .synopsis(synopsis)
                        .build();
            }
        } else {
            String classification = eventPage.getElementsByClass("program-area-1").get(0).text();
            String synopsis = "";
            try {
                synopsis = eventPage.getElementsByTag("p").get(1).text();
                if (classification.equals(synopsis) && eventPage.getElementsByTag("p").size() >= 3) {
                    synopsis = eventPage.getElementsByTag("p").get(2).text();
                }
            } catch (Exception e) {
                log.warn("No synopsis for " + name);
            }
            String imageUrl = "";
            try {
                imageUrl = eventPage.getElementsByAttributeValue("title", name).get(0).attr("src");
            } catch (Exception e) {
                log.warn("No image for " + name);
            }
            return ChapterDTO.builder()
                    .serieDTO(
                            SerieDTO.builder()
                                    .classification(classification)
                                    .rate(rate)
                                    .imageUrl(tmdbPoster != null ? tmdbPoster : imageUrl)
                                    .name(name)
                                    .build())
                    .chapterName(chapterName)
                    .synopsis(synopsis)
                    .build();
        }
    }

    private LocalTime parseTime(String timeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(timeString, dateTimeFormatter);
    }

    private Result getDataFromTMDB(String name, EventType eventType, Integer year) {
        try {
            TimeUnit.MILLISECONDS.sleep(100);

            if (eventType.equals(EventType.MOVIE)) {
                var results = year == null ?
                        tmdbApiClient.searchMovie(Map.of("query", name, "language", "es-ES"))
                        : tmdbApiClient.searchMovie(Map.of("query", name, "language", "es-ES", "year", year.toString()));
                if (results != null && results.total_results() == 0 && name.contains(":")) {
                    results = year == null ?
                            tmdbApiClient.searchMovie(Map.of("query", name.split(":")[1], "language", "es-ES"))
                            : tmdbApiClient.searchMovie(Map.of("query", name.split(":")[1], "language", "es-ES", "year", year.toString()));
                }
                if (results != null && results.total_results() == 0 && name.contains("III")) {
                    results = year == null ?
                            tmdbApiClient.searchMovie(
                                    Map.of("query", name.replace("III", "3"), "language", "es-ES"))
                            : tmdbApiClient.searchMovie(Map.of("query", name.replace("III", "3"), "language", "es-ES", "year", year.toString()));
                } else if (results != null && results.total_results() == 0 && name.contains("II")) {
                    results = year == null ?
                            tmdbApiClient.searchMovie(
                                    Map.of("query", name.replace("II", "2"), "language", "es-ES"))
                            : tmdbApiClient.searchMovie(Map.of("query", name.replace("II", "2"), "language", "es-ES", "year", year.toString()));
                } else if (results != null && results.total_results() == 0 && name.contains("IV")) {
                    results = year == null ?
                            tmdbApiClient.searchMovie(
                                    Map.of("query", name.replace("IV", "4"), "language", "es-ES"))
                            : tmdbApiClient.searchMovie(Map.of("query", name.replace("IV", "4"), "language", "es-ES", "year", year.toString()));
                } else if (results != null && results.total_results() == 0 && name.contains("V")) {
                    results = year == null ?
                            tmdbApiClient.searchMovie(
                                    Map.of("query", name.replace("V", "5"), "language", "es-ES"))
                            : tmdbApiClient.searchMovie(Map.of("query", name.replace("V", "5"), "language", "es-ES", "year", year.toString()));
                }
                return results != null && results.total_results() > 0 ? year == null ? results.results().get(0) : filterResultByYear(results.results(), year) : null;
            } else {
                var results = tmdbApiClient.searchTV(Map.of("query", name, "language", "es-ES"));
                return results != null && results.total_results() > 0 ? results.results().get(0) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Integer yearExtractor(String input) {
        // Define a regular expression pattern to match the year (4 digits)
        Pattern pattern = Pattern.compile("\\b\\d{4}\\b");

        // Create a Matcher and find the first match
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Extract the matched year
            String yearString = matcher.group();
            return Integer.parseInt(yearString);
        } else {
            log.warn("Year not found");
            return null;
        }
    }

    private Result filterResultByYear(List<Result> results, int year) {
        return results.stream()
                .filter(result -> year == result.release_date().getYear())
                .findFirst()
                .orElse(null);
    }
}
