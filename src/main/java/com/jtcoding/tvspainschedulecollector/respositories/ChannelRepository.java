package com.jtcoding.tvspainschedulecollector.respositories;

import com.jtcoding.tvspainschedulecollector.entities.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {}
