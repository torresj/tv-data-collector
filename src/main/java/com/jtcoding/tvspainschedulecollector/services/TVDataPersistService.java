package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.dtos.ChannelDTO;

import java.util.List;

public interface TVDataPersistService {
    void persistChannelsData(List<ChannelDTO> channels);
}
