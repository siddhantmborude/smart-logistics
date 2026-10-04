package com.logistics.smartlogistics.dto;

import java.util.List;

public record RouteResponse(
        String algorithm,
        List<Long> routeIds,
        List<String> routeNames,
        double totalDistance,
        long executionTimeNanos,
        long nodesExplored
) {}
