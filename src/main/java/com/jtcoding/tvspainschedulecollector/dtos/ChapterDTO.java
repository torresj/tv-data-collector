package com.jtcoding.tvspainschedulecollector.dtos;

import com.jtcoding.tvspainschedulecollector.enums.EventType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChapterDTO {
  private String synopsis;
  private String chapterName;
  private  SerieDTO serieDTO;
}
