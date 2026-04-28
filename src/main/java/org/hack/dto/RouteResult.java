package org.hack.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RouteResult(
        double distance,
        double time,
        List<PointDto> points,
        List<InstructionInfo> instructions
) {}