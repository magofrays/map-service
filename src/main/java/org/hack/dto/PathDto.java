package org.hack.dto;

import lombok.Builder;

import java.util.List;


@Builder
public record PathDto (
    String id,
    int locationsAmount,
    String title,
    String taskId,
    List<LocationWithOrder> locations,
    RouteResult routeResult,
    String userId
){}
