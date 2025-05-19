package com.example.myapp.service.impl;

import com.example.myapp.dto.SystemStatisticsDTO;
import com.example.myapp.mapper.StatisticsMapper;
import com.example.myapp.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Autowired
    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public SystemStatisticsDTO getSystemStatistics() {
        SystemStatisticsDTO statistics = new SystemStatisticsDTO();
        statistics.setUserCount(statisticsMapper.countUsers());
        statistics.setEventGraphCount(statisticsMapper.countEventGraphs());
        statistics.setTextStyleCount(statisticsMapper.countTextStyles());
        statistics.setGeneratedTextCount(statisticsMapper.countGeneratedTexts());
        return statistics;
    }
}