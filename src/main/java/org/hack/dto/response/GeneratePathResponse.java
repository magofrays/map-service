package org.hack.dto.response;

import org.hack.dto.LocationWithOrder;
import org.hack.dto.RouteResult;

import java.util.List;

public record GeneratePathResponse(
        String id,
        int locationsAmount,
        double overallTime,
        List<LocationWithOrder> locations,
        RouteResult routeResult
) {
}