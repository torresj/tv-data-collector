package com.jtcoding.tvspainschedulecollector.services;

import com.jtcoding.tvspainschedulecollector.dtos.ChannelDTO;
import java.io.IOException;
import java.util.List;

public interface TVDataCollectorService {
  List<ChannelDTO> processTVData() throws IOException;
}
