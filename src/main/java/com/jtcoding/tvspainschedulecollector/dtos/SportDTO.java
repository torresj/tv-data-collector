package com.jtcoding.tvspainschedulecollector.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SportDTO {
  private String name;
  private String synopsis;
  private String classification;
}
