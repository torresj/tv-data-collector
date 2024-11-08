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
public class ChapterEntity {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  @Column(updatable = false)
  private Long id;

  @Column(nullable = false)
  private long serieId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String synopsis;

  @Column private String chapterName;
}
