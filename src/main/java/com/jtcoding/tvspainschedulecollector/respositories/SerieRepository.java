package com.jtcoding.tvspainschedulecollector.respositories;

import com.jtcoding.tvspainschedulecollector.entities.SerieEntity;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerieRepository extends CrudRepository<SerieEntity, Long> {
  Optional<SerieEntity> findByName(String name);
}
