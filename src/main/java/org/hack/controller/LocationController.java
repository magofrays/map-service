package org.hack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hack.dto.LocationDto;
import org.hack.dto.request.GeneratePathRequest;
import org.hack.dto.request.LocationSearchRequest;
import org.hack.dto.response.GeneratePathResponse;
import org.hack.dto.response.LocationSearchResponse;
import org.hack.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Location Service", description = "API для работы с локациями и маршрутами")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Поиск локаций с фильтрацией",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/locations/search")
    public ResponseEntity<?> searchLocations(@RequestBody LocationSearchRequest request,
                                             @AuthenticationPrincipal Jwt userToken) {
        try {
            LocationSearchResponse response = locationService.searchLocations(request, userToken.getClaim("sub"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    @Operation(summary = "Создать новую локацию",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/locations")
    public ResponseEntity<?> createLocation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные локации",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"title\": \"Эрмитаж\", \"description\": \"Главный музей\", \"coordinates\": [59.9343, 30.3351], \"tags\": [\"музей\", \"искусство\"]}"
                    ))
            )
            @RequestBody @Validated LocationDto location,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt userToken) {
        try {
            LocationDto created = locationService.create(location, userToken.getClaim("sub"));
            return ResponseEntity.ok(Map.of("location", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    @Operation(summary = "Обновить локацию",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PatchMapping("/locations/")
    public ResponseEntity<?> updateLocation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            value = "{\"locationId\": \"uuid-here\", \"title\": \"Обновлено\", \"description\": \"Новое описание\", \"coordinates\": [59.9343, 30.3351], \"tags\": [\"музей\"]}"
                    ))
            )
            @RequestBody @Validated LocationDto location,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt userToken) {
        try {
            LocationDto updated = locationService.update(location, userToken.getClaim("sub"));
            return ResponseEntity.ok(Map.of("location", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    @Operation(summary = "Удалить локацию",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/locations/{location_id}")
    public ResponseEntity<?> deleteLocation(
            @Parameter(description = "ID локации", required = true)
            @PathVariable("location_id") String locationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt userToken) {
        try {
            locationService.delete(locationId, userToken.getClaim("sub"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("error_message", e.getMessage()));
        }
    }

    @Operation(summary = "Сгенерировать маршрут по локациям",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/generate_path")
    public ResponseEntity<?> generatePath(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            value = "{\"locations\": [{\"location\": {\"locationId\": \"uuid-1\"}, \"order\": 0}, {\"location\": {\"locationId\": \"uuid-2\"}, \"order\": 1}], \"taskId\": \"task-123\"}"
                    ))
            )
            @RequestBody GeneratePathRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt userToken) {
        try {
            GeneratePathResponse response = locationService.generatePath(request, userToken.getClaim("sub"));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error_message", e.getMessage()));
        }
    }
    @Operation(summary = "Массовое сохранение локаций (дамп)")
    @PostMapping("/json-dump")
    public ResponseEntity<?> jsonDump(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            value = """
                                [
                                    {
                                        "title": "Эрмитаж",
                                        "description": "Главный музей Санкт-Петербурга",
                                        "coordinates": [59.9343, 30.3351],
                                        "tags": ["музей", "искусство", "история"]
                                    },
                                    {
                                        "title": "Петропавловская крепость",
                                        "description": "Историческая крепость",
                                        "coordinates": [59.9500, 30.3167],
                                        "tags": ["крепость", "история", "музей"]
                                    }
                                ]
                                """
                    ))
            )
            @RequestBody @Validated List<LocationDto> locations
    ) {
        return ResponseEntity.ok(locationService.saveDump(locations));
    }
}