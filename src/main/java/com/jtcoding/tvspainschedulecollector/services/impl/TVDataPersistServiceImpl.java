package com.jtcoding.tvspainschedulecollector.services.impl;

import com.jtcoding.tvspainschedulecollector.dtos.ChannelDTO;
import com.jtcoding.tvspainschedulecollector.entities.*;
import com.jtcoding.tvspainschedulecollector.enums.EventType;
import com.jtcoding.tvspainschedulecollector.respositories.*;
import com.jtcoding.tvspainschedulecollector.services.TVDataPersistService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TVDataPersistServiceImpl implements TVDataPersistService {

  private final EventRepository eventRepository;
  private final ChannelRepository channelRepository;
  private final MovieRepository movieRepository;
  private final ChapterRepository chapterRepository;
  private final SerieRepository serieRepository;
  private final SportRepository sportRepository;

  @Override
  public void persistChannelsData(List<ChannelDTO> channels) {
    log.info("[TVDataPersistServiceImpl] cleaning DB");
    channelRepository.deleteAll();
    eventRepository.deleteAll();
    sportRepository.deleteAll();

    log.info("[TVDataPersistServiceImpl] Saving all events for today and tomorrow");
    channels.forEach(
        channel -> {
          var channelEntity =
              channelRepository.save(
                  ChannelEntity.builder()
                      .name(channel.getName())
                      .logoUrl(channel.getLogoUrl())
                      .build());
          channel
              .getEvents()
              .forEach(
                  event -> {
                    if (event.getEventType().equals(EventType.MOVIE)) {
                      var movieInDB = movieRepository.findByName(event.getMovie().getName());
                      var movie =
                          movieInDB
                              .map(
                                  movieEntity ->
                                      movieRepository.save(
                                          MovieEntity.builder()
                                              .name(event.getMovie().getName())
                                              .id(movieEntity.getId())
                                              .classification(event.getMovie().getClassification())
                                              .rate(event.getMovie().getRate())
                                              .synopsis(event.getMovie().getSynopsis())
                                              .interpreters(event.getMovie().getInterpreters())
                                              .imageUrl(event.getMovie().getImageUrl())
                                              .director(event.getMovie().getDirector())
                                              .build()))
                              .orElseGet(
                                  () ->
                                      movieRepository.save(
                                          MovieEntity.builder()
                                              .name(event.getMovie().getName())
                                              .classification(event.getMovie().getClassification())
                                              .rate(event.getMovie().getRate())
                                              .synopsis(event.getMovie().getSynopsis())
                                              .interpreters(event.getMovie().getInterpreters())
                                              .imageUrl(event.getMovie().getImageUrl())
                                              .director(event.getMovie().getDirector())
                                              .build()));
                      eventRepository.save(
                          EventEntity.builder()
                              .channelId(channelEntity.getId())
                              .eventType(event.getEventType())
                              .contentId(movie.getId())
                              .endEvent(event.getEnd())
                              .startEvent(event.getStart())
                              .duration(event.getDuration())
                              .build());
                    } else if(event.getEventType().equals(EventType.SERIE)) {
                      var serieInDB =
                          serieRepository.findByName(event.getSerie().getSerieDTO().getName());
                      if (serieInDB.isPresent()) {
                        var serie =
                            serieRepository.save(
                                SerieEntity.builder()
                                    .id(serieInDB.get().getId())
                                    .classification(
                                        event.getSerie().getSerieDTO().getClassification())
                                    .director(event.getSerie().getSerieDTO().getDirector())
                                    .imageUrl(event.getSerie().getSerieDTO().getImageUrl())
                                    .interpreters(event.getSerie().getSerieDTO().getInterpreters())
                                    .name(event.getSerie().getSerieDTO().getName())
                                    .rate(event.getSerie().getSerieDTO().getRate())
                                    .build());
                        var chapterInDB =
                            chapterRepository.findBySerieIdAndChapterName(
                                serie.getId(), event.getSerie().getChapterName());
                        var chapter =
                            chapterInDB
                                .map(
                                    chapterEntity ->
                                        chapterRepository.save(
                                            ChapterEntity.builder()
                                                .id(chapterEntity.getId())
                                                .chapterName(event.getSerie().getChapterName())
                                                .synopsis(event.getSerie().getSynopsis())
                                                .serieId(serie.getId())
                                                .build()))
                                .orElseGet(
                                    () ->
                                        chapterRepository.save(
                                            ChapterEntity.builder()
                                                .chapterName(event.getSerie().getChapterName())
                                                .synopsis(event.getSerie().getSynopsis())
                                                .serieId(serie.getId())
                                                .build()));

                        eventRepository.save(
                            EventEntity.builder()
                                .channelId(channelEntity.getId())
                                .eventType(event.getEventType())
                                .endEvent(event.getEnd())
                                .startEvent(event.getStart())
                                .duration(event.getDuration())
                                .contentId(chapter.getId())
                                .build());

                      } else {
                        var serie =
                            serieRepository.save(
                                SerieEntity.builder()
                                    .classification(
                                        event.getSerie().getSerieDTO().getClassification())
                                    .director(event.getSerie().getSerieDTO().getDirector())
                                    .imageUrl(event.getSerie().getSerieDTO().getImageUrl())
                                    .interpreters(event.getSerie().getSerieDTO().getInterpreters())
                                    .name(event.getSerie().getSerieDTO().getName())
                                    .rate(event.getSerie().getSerieDTO().getRate())
                                    .build());
                        var chapterInDB =
                            chapterRepository.findBySerieIdAndChapterName(
                                serie.getId(), event.getSerie().getChapterName());
                        var chapter =
                            chapterInDB
                                .map(
                                    chapterEntity ->
                                        chapterRepository.save(
                                            ChapterEntity.builder()
                                                .id(chapterEntity.getId())
                                                .chapterName(event.getSerie().getChapterName())
                                                .synopsis(event.getSerie().getSynopsis())
                                                .serieId(serie.getId())
                                                .build()))
                                .orElseGet(
                                    () ->
                                        chapterRepository.save(
                                            ChapterEntity.builder()
                                                .chapterName(event.getSerie().getChapterName())
                                                .synopsis(event.getSerie().getSynopsis())
                                                .serieId(serie.getId())
                                                .build()));

                        eventRepository.save(
                            EventEntity.builder()
                                .channelId(channelEntity.getId())
                                .eventType(event.getEventType())
                                .endEvent(event.getEnd())
                                .startEvent(event.getStart())
                                .duration(event.getDuration())
                                .contentId(chapter.getId())
                                .build());
                      }
                    }else{
                        var sport =
                                sportRepository.save(
                                        SportEntity.builder()
                                                .synopsis(event.getSport().getSynopsis())
                                                .classification(event.getSport().getClassification())
                                                .name(event.getSport().getName())
                                                .build());
                      eventRepository.save(
                              EventEntity.builder()
                                      .channelId(channelEntity.getId())
                                      .eventType(event.getEventType())
                                      .endEvent(event.getEnd())
                                      .startEvent(event.getStart())
                                      .duration(event.getDuration())
                                      .contentId(sport.getId())
                                      .build());
                    }
                  });
        });

    log.info("[TVDataPersistServiceImpl] Data saved");
  }
}
