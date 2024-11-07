package com.jtcoding.tvspainschedulecollector.respositories;

import com.jtcoding.tvspainschedulecollector.entities.ChapterEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends CrudRepository<ChapterEntity, Long> {
  Optional<ChapterEntity> findBySerieIdAndChapterName(long serieId, String chapterName);
}
