package com.jtcoding.tvspainschedulecollector.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChannelDTO {
  private String name;
  private String logoUrl;
  private List<EventDTO> events;
}
