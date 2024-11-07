package com.jtcoding.tvspainschedulecollector.dtos;

import com.jtcoding.tvspainschedulecollector.enums.EventType;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SerieDTO {
  private String name;
  private String classification;
  private String director;
  private String interpreters;
  private Double rate;
  private String imageUrl;
}
