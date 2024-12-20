package com.jtcoding.tvspainschedulecollector.entities;

import com.jtcoding.tvspainschedulecollector.enums.EventType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class EventEntity {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;

  @Column(nullable = false)
  private long channelId;

  @Column(nullable = false)
  private long contentId;

  @Column(nullable = false)
  private EventType eventType;

  @Column private LocalDateTime startEvent;

  @Column private LocalDateTime endEvent;

  @Column private long duration;
}
