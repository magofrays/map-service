package org.hack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hack.dto.PointDto;
import org.hack.dto.RouteResult;
import org.hack.service.GraphHopperService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "GraphHopperController", description = "Controller to work with locations")
public class GraphHopperController {

    private final GraphHopperService graphHopperService;

    @Operation(summary = "Построить пешеходный маршрут между двумя точками")
    @GetMapping("/route")
    public RouteResult getRouteBetweenTwoPoints(
            @Parameter(description = "Широта начальной точки") @RequestParam double fromLat,
            @Parameter(description = "Долгота начальной точки") @RequestParam double fromLon,
            @Parameter(description = "Широта конечной точки") @RequestParam double toLat,
            @Parameter(description = "Долгота конечной точки") @RequestParam double toLon) {

        return graphHopperService.calculateRoute(fromLat, fromLon, toLat, toLon);
    }

    @Operation(summary = "Построить пешеходный маршрут через несколько точек")
    @PostMapping("/route")
    public RouteResult getRouteThroughPoints(
            @Parameter(description = "Список точек маршрута")
            @RequestBody List<PointDto> points) {

        return graphHopperService.calculateRoute(points);
    }
}