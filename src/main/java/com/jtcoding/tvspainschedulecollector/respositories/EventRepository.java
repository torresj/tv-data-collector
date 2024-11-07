package com.jtcoding.tvspainschedulecollector.respositories;

import com.jtcoding.tvspainschedulecollector.entities.EventEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long> {}
