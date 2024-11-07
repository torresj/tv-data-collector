package com.jtcoding.tvspainschedulecollector.dtos;

import com.jtcoding.tvspainschedulecollector.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventDTO {
  private MovieDTO movie;
  private ChapterDTO serie;
  private SportDTO sport;
  private EventType eventType;
  private LocalDateTime start;
  private LocalDateTime end;
  private long duration;
}
